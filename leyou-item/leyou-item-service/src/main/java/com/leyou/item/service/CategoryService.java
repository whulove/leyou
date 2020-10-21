package com.leyou.item.service;


import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

   // http://api.leyou.com/api/item/category/list?pid=0
    public List<Category> queryCategoriesByPid(Long pid){
        Category record = new Category();
        record.setParentId(pid);
        return this.categoryMapper.select(record);
    }

    //根据bid 查询  分类   数据回显
    public List<Category> queryCategoryByBid(Long bid) {
        List<Long> cids = categoryMapper.selectCidsByBid(bid);
        List<Category> categories = new ArrayList<>();
        for (Long cid : cids) {
            Category category = categoryMapper.selectByPrimaryKey(cid);
            categories.add(category);
        }
        return categories;
}


    //根据cids  查询    Category中拓展查询名称的功能
    public List<String> queryNamesByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        List<String> names = new ArrayList<>();
        for (Category category : list) {
            names.add(category.getName());
        }

        return names;

    }

    //新增
    public void saveCategory(Category category) {
        //若本节点插入到数据库中
        //此category的父节点的isParent 设置为true
        //1. 首先id 为null
        category.setId(null);
        //2.保存
        categoryMapper.insert(category);
        //3.修改父节点
        Category parent = new Category();
        parent.setId(category.getParentId());
        parent.setIsParent(true);
        this.categoryMapper.updateByPrimaryKeySelective(parent);
    }
    //删除
    public void deleteCategory(Long id) {
        Category category  = categoryMapper.selectByPrimaryKey(id);
        if (category.getIsParent()){        //是父节点
            //1.查找所有的叶子节点  底层节点
            List<Category> list = new ArrayList<>();
            queryAllLeafNode(category, list);

            //2.查找所有子节点
            List<Category> list2 =  new ArrayList<>();
            queryAllNode(category,list2);

            //3.删除tb_category中的数据, 使用list2
            for (Category c : list2) {
                categoryMapper.delete(c);
            }

            //4, 维护中间b表
            for (Category c : list) {
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(c.getId());
            }
        }else{                      //不是父节点
            //1.查询此节点的父节点的子节点个数 => 查询还有几个兄弟
            Example example = new Example(Category.class);
            example.createCriteria().andEqualTo("parentId",category.getParentId());
            List<Category> list = categoryMapper.selectByExample(example);
            if (list.size() != 1){
                //有兄弟, 直接删除自己
                this.categoryMapper.deleteByPrimaryKey(category.getId());

                //维护中间表
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            }else {
                //没有兄弟 先删除自己
                this.categoryMapper.deleteByPrimaryKey(category.getId());
                Category parent = new Category();
                parent.setParentId(category.getParentId());
                parent.setIsParent(false);
                this.categoryMapper.updateByPrimaryKeySelective(parent);
                //维护中间表
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            }
        }
    }
    //查询本节点下所有子节点
    private void queryAllNode(Category category, List<Category> node) {
        node.add(category);
        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId", category.getParentId());
        List<Category> list = categoryMapper.selectByExample(example);
        for (Category category1 :list){
            queryAllNode(category1, node);
        }
    }


    //查询本节点所包含的所有叶子节点, 用于维护tb_category_brand中间表
    private void queryAllLeafNode(Category category, List<Category> leafNode){
        if (!category.getIsParent()){
            leafNode.add(category);
        }
        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId", category.getParentId());
        List<Category> list = categoryMapper.selectByExample(example);

        for (Category category1 : list) {
            queryAllLeafNode(category1,leafNode);
        }
    }

    //更新
    public void updateCategory(Category category) {
        this.categoryMapper.updateByPrimaryKeySelective(category);
    }


    public List<Category> queryAllByCid3(Long id) {
        Category c3 = categoryMapper.selectByPrimaryKey(id);
        Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        return Arrays.asList(c1,c2,c3);
    }
}
