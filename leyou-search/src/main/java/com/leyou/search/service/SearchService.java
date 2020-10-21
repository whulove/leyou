package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.search.GoodsRespository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;

import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.util.LongsRef;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specClient;
    @Autowired
    private GoodsRespository goodsRespository;

    private  static final ObjectMapper MAPPER = new ObjectMapper();

    //完成基本查询条件
    public SearchResult search(SearchRequest searchRequest) {
        //判断查询条件是否为空
        if(StringUtils.isBlank(searchRequest.getKey())){
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //添加查询条件, 对key进行全文检索查询
      // MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND);
       BoolQueryBuilder basicQuery = buildBasicQuery(searchRequest);
        queryBuilder.withQuery(basicQuery);

        //添加分页, 行号从0开始
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() -1, searchRequest.getSize()));

        //排序
        String sortBy = searchRequest.getSortBy();
        Boolean desc = searchRequest.getDescending();
        if (StringUtils.isNotBlank(sortBy)){
            //如果不为空
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }

        //添加结果集过滤 id, subTitle, skus
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"}, null));

        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        //执行查询获取结果集
       AggregatedPage<Goods> goodsPage =  (AggregatedPage<Goods>)this.goodsRespository.search(queryBuilder.build());

       //解析聚合结果集
        List<Map<String,Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));

        List<Map<String,Object>> specs = null;
        if (!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            specs = getParamAggName((Long)categories.get(0).get("id"), basicQuery);
        }

        //返回分页结果集
        return  new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(),goodsPage.getContent(),categories,brands,specs);

    }

    //规格参数聚合结果集
    private List<Map<String, Object>> getParamAggName(Long id, QueryBuilder basicQuery) {
        //查询聚合结果集
        List<SpecParam> params = specClient.queryParams(null, id, null, true);
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本的查询条件
        queryBuilder.withQuery(basicQuery);
        //添加聚合
        params.forEach(param ->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+ ".keyword"));
        });
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        //执行查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) goodsRespository.search(queryBuilder.build());
        //初始化聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();
        //获取所有的规格参数结果集   Map<ParamName, aggregation>
        Map<String, Aggregation> paramAggregationMap = goodsPage.getAggregations().asMap();

        for (Map.Entry<String, Aggregation> entry : paramAggregationMap.entrySet()) {
            //每一个规格参数的聚合结果集,对应一个map
            Map<String, Object> map = new HashMap<>();
            //设置字段
            map.put("k", entry.getKey());
            //解析每一个聚合的桶
            StringTerms terms = (StringTerms) entry.getValue();
            List<Object> options =  terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            //设置options字段
            map.put("options",options);
            paramMapList.add(map);
        }
        return paramMapList;

    }
    //构建boolQueryBuilder
    private BoolQueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        //初始化bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本查询条件  小米手机
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        //添加过滤
        for (Map.Entry<String, String> entry : searchRequest.getFilter().entrySet()) {
            String key = entry.getKey();
            if (StringUtils.equals(key,"品牌")){
                key = "brandId";
            }else if (StringUtils.equals(key,"分类")){
                key = "cid3";
            }else{
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return boolQueryBuilder;
    }

    //解析品牌结果集
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        //处理聚合结果集
        LongTerms terms = (LongTerms) aggregation;
        //获取所有的品牌id桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        ///定义一个品牌集合,搜集所有的品牌对象
        List<Brand> brands = new ArrayList<>();
        //解析所有id的桶, 查询品牌
        buckets.forEach(bucket -> {
            Brand brand = brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
        return brands;
    }

   //解析分类结果集
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        //处理聚合结果集
        LongTerms terms = (LongTerms) aggregation;
        //获取所有的分类id桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        ///定义一个分类集合,搜集所有的分类对象
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = new ArrayList<>();

        //解析所有id的桶,查询分类id
        buckets.forEach(bucket -> {
            cids.add(bucket.getKeyAsNumber().longValue());
        });
        List<String> names = categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
             Map<String, Object> map = new HashMap<>();
             map.put("id", cids.get(i));
             map.put("name",names.get(i));
             categories.add(map);
        }
        return categories;
    }

    //构建goods对象
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        //根据品牌id查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //根据cid1, cid2 ,cid3查询对应的分类名称
        List<String> names = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        //根据spuId查询所有的sku
        List<Sku> skus = goodsClient.querySkusBySpuId(spu.getId());
        //初始化价格集合
        List<Long> prices = new ArrayList<>();
        //初始化skuMapList, 每一个map相当于一个sku, map中的取值: id,title, image , price
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image",StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuMapList.add(skuMap);
        });

        //根据cid3 ,查询所有的搜索规格参数
        List<SpecParam>  params = specClient.queryParams(null, spu.getCid3(),null,true);
        //查询spuDetail ,获取规格参数值 genericSpec, SpecialSpec
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        //获取通用规格参数
        Map<Long,Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>(){});
        //获取特殊规格参数
        Map<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {});

        //定义mao接受,  规格参数名,  规格参数值
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param ->{
            //判断是否是通用参数
            if(param.getGeneric()){
                //通用规格参数
                String value = genericSpecMap.get(param.getId()).toString();
                if (param.getNumeric()){
                    //如果是数值类型判断落在那个区间
                    value = chooseSegment(value, param);
                }
                //把参数名和值放入结果集中
                specs.put(param.getName(),value);
            }else{
                List<Object> value =  specialSpecMap.get(param.getId());
                specs.put(param.getName(), value);
            }
        });


        //把一些简单的字段复制给goodsz对象, id, subTitle, brandId, cid ,createTime
        BeanUtils.copyProperties(spu,goods);
        goods.setAll(spu.getTitle()+ " " + brand.getName()+ " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));       //把对象输出成字符串
        goods.setSpecs(specs);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其他";
        //保存数值段
        for (String segment : p.getSegments().split(",") ) {
            String[] segs = segment.split("-");
            //获取取值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            //判断是否在范围内
            if (val>=begin && val < end){
                if (segs.length  == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    public void saveIndex(Long spuId) throws IOException {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = this.buildGoods(spu);
        goodsRespository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRespository.deleteById(spuId);
    }
}
