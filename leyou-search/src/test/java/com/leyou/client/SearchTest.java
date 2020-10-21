package com.leyou.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.GoodsRespository;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;



@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchTest {

    @Autowired
    private GoodsRespository goodsRespository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;


    @Test
    public void test(){
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;

        do{
            //分批查询
            PageResult<SpuBo> result = goodsClient.querySpuByPage(null,true,page,rows);
            List<SpuBo> items = result.getItems();

            // List<SpuBo> => List<Goods>
            List<Goods> goodsList =  items.stream().map(spuBo -> {
                try {
                    return searchService.buildGoods(spuBo);
                }catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            goodsRespository.saveAll(goodsList);
            page++;
            rows = items.size();
        }while (rows == 100);
    }

}
