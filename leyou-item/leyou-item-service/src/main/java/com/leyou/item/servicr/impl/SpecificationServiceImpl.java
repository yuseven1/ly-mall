package com.leyou.item.servicr.impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.servicr.SpecificationService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据分类id查询参数组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> querySpecGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.specGroupMapper.select(record);
    }

    @Override
    public List<SpecParam> querySpecParamsByGid(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.specParamMapper.select(record);
    }

    @Override
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> specGroups = this.querySpecGroupsByCid(cid);
        specGroups.forEach(specGroup -> {
            List<SpecParam> specParams = this.querySpecParamsByGid(specGroup.getId(), null, null, null);
            specGroup.setParams(specParams);
        });

        return specGroups;

    }
}
