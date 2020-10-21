package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    //根据查询条件分页并排序查询品牌信息
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria  = example.createCriteria();

        //根据name模糊查询,或根据首字母查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter",key.toUpperCase()); /*这里变成大写,因为数据库里是大写*/
        }

        //添加分页条件
        PageHelper.startPage(page,rows);
        //排序
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + " "+ (desc ? "desc" : "asc"));
        }
        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装成pageinfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装成分页结果集并返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //先增加brand
        this.brandMapper.insertSelective(brand);
        //增加中间表
        cids.forEach(cid->
                this.brandMapper.insertCategoryAndBrand(cid,brand.getId()));
    }

    //修改
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        brandMapper.updateByPrimaryKey(brand);
        brandMapper.deleteByBrandIdInCategoryBrand(brand.getId());
        for (Long cid : cids) {
            brandMapper.insertCategoryAndBrand(cid, brand.getId());

        }
    }
    //删除
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        //删除品牌信息
        brandMapper.deleteByPrimaryKey(id);
        //维护中间表
        brandMapper.deleteByBrandIdInCategoryBrand(id);
    }

    //根据 分类cid查询品牌列表            商品增加或回显查找
    @Transactional
    public List<Brand> queryBrandsByCid(Long cid){
        return this.brandMapper.selectBrandsByCid(cid);
    }

    //根据id查询品牌
    public Brand queryBrandById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }
}
