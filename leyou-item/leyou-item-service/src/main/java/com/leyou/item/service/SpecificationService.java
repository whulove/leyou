package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    //根据分类id查询规格参数组
    public List<SpecGroup> queryGroupsByCid(Long cid){
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return specGroupMapper.select(record);

    }
    //根据组id查询规格参数
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return specParamMapper.select(record);
    }
    ///新增规格模板的分组
    public void saveSpecGroup(SpecGroup specGroup) {
        specGroupMapper.insertSelective(specGroup);
    }
    //删除分组
    public void deleteSpecGroup(Long id) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(id);
        specParamMapper.delete(specParam);
        specGroupMapper.deleteByPrimaryKey(id);
    }
    //修改分组
    public void updateSpecGroup(SpecGroup specGroup) {
        specGroupMapper.updateByPrimaryKeySelective(specGroup);
    }
    //新增规格模板中某一组的规格参数
    public void saveSpecParam(SpecParam specParam) {
        specParamMapper.insertSelective(specParam);
    }
    //修改规格参数中某一个分组的某个规格参数
    public void updateSpecParam(SpecParam specParam) {
        specParamMapper.updateByPrimaryKey(specParam);
    }
    //s删除规格参数中某一个分组的某个规格参数
    public void deleteSpecParam(Long paramId) {
        specParamMapper.deleteByPrimaryKey(paramId);
    }
    //查询cid查询规格参数组
    public List<SpecGroup> queryGroupWithParam(Long cid) {
        List<SpecGroup> groups = queryGroupsByCid(cid);
        groups.forEach(group ->{
            List<SpecParam> params =queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }

}
