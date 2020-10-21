package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {
    //新增商品商品分类和中间表数据
    @Insert("insert into tb_category_brand(category_id,brand_id) value (#{cid}, #{bid})")
    int insertCategoryAndBrand(@Param("cid") Long cid, @Param("bid")Long bid);

    //删除brand id 删除中间表相关数据
    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{bid}")
    void deleteByBrandIdInCategoryBrand(@Param("bid") Long bid);


    @Select("select b.* from tb_brand b inner join tb_category_brand cb on b.id = cb.brand_id where cb.category_id=#{cid}")
    List<Brand> selectBrandsByCid(Long cid);
}
