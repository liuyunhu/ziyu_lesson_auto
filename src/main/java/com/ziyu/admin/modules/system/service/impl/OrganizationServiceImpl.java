package com.ziyu.admin.modules.system.service.impl;

import com.ziyu.admin.core.utils.Constants;
import com.ziyu.admin.modules.system.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.ziyu.admin.modules.base.service.impl.BaseService;
import com.ziyu.admin.modules.system.dto.TreeDto;
import com.ziyu.admin.modules.system.mapper.OrganizationMapper;
import com.ziyu.admin.modules.system.po.Organization;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationServiceImpl extends BaseService<Organization> implements OrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public void createOrganization(Organization organization) {
        //Organization parent = organizationMapper.selectOne(new Organization().setParentId(organization.getParentId()));
        Organization parent = organizationMapper.selectByPrimaryKey(organization.getParentId());
        organization.setParentIds(parent.makeSelfAsParentIds());
        organization.setAvailable(true);
        organizationMapper.insertSelective(organization);
    }

    @Override
    public List<TreeDto> queryOrgTree(Long pId) {
        if (StringUtils.isEmpty(pId)) {
            pId = Constants.ORG_ROOT_ID;
        }
        List<TreeDto> tds = new ArrayList<>();
        Weekend example = Weekend.of(Organization.class);
        WeekendCriteria<Organization, Object> criteria = example.weekendCriteria();
        criteria.andEqualTo(Organization::getParentId, pId);
        organizationMapper.selectByExample(example).forEach(o -> tds.add(new TreeDto(o.getId(), o.getParentId(), o.getName(), Boolean.FALSE.equals(o.getLeaf()), o)));
        return tds;
    }

    @Override
    public List<Organization> queryAllWithExclude(Organization exclude) {
        Weekend weekend = Weekend.of(Organization.class);
        WeekendCriteria<Organization, Object> criteria = weekend.weekendCriteria();
        criteria.andNotEqualTo(Organization::getId, exclude.getId()).andNotLike(Organization::getParentIds, exclude.makeSelfAsParentIds() + "%");
        return organizationMapper.selectByExample(weekend);
    }

    @Override
    @Transactional
    public void move(Organization source, Organization target) {
        organizationMapper.updateByPrimaryKeySelective(target);
        organizationMapper.updateSalefParentIds(source.makeSelfAsParentIds());
    }
}
