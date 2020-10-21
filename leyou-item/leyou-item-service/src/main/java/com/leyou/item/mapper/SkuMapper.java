package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.base.insert.InsertSelectiveMapper;

public interface SkuMapper extends Mapper<Sku> , InsertSelectiveMapper<Sku> {
}
