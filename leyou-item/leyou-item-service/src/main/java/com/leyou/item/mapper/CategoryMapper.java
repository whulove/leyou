package com.leyou.item.mapper;


import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category> , SelectByIdListMapper<Category,Long> {

    @Select("select Category_id from tb_category_brand where brand_id = #{bid}")
    List<Long> selectCidsByBid(@Param("bid") Long bid);

    @Delete("delete from tb_category_brand where category_id = #{cid}")
    void deleteByCategoryIdInCategoryBrand(@Param("cid")Long id);
}
