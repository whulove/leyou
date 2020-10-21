package com.leyou.goods.service;


import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    public Map<String, Object> loadData(Long spuId) {

        Map<String, Object> model = new HashMap<>();
        //根据spuId查询spu
        Spu spu = goodsClient.querySpuById(spuId);

        //查询spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);

        //查询sku集合
        List<Sku> skus = goodsClient.querySkusBySpuId(spuId);

        //查询分类cid1,cid2,cid3
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = Arrays.asList(spu.getCid1(),spu.getCid2(), spu.getCid3());
        List<String> names = categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }

        //根据brandId查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        // 查询规格参数组
        List<SpecGroup> groups = specClient.queryGroupWithParam(spu.getCid3());

        //特殊参数
        List<SpecParam> params = specClient.queryParams(null, spu.getCid3(), null, false);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param ->{
            paramMap.put(param.getId(), param.getName());
        });

        //查询特殊规格参数对应的名称

        model.put("categories", categories);    //分类
        model.put("brand", brand);              //品牌
        model.put("spu", spu);                        //spu
        model.put("skus", skus);                        //sku
        model.put("spuDetail", spuDetail);
        model.put("groups", groups);
        model.put("paramMap", paramMap);

        return model;
    }
}
