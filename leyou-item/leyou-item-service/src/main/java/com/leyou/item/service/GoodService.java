package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoodService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    //分页结果集
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //1. 模糊查询  搜索条件
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%"+key+"%");
        }
        //2.是否添加上下架条件
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        //3.分页条件
        PageHelper.startPage(page,rows);
        //4 执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu ->{
            SpuBo spuBo = new SpuBo();
            //copy共同属性到 新的对象中
            BeanUtils.copyProperties(spu, spuBo);
            //查询分类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
            spuBo.setCname(StringUtils.join(names,"/"));

            //查询品牌的名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBos.add(spuBo);
        });
        //5. 返回分页结果集
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    //新增商品
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //1.新增spu
        //设置默认字段, 先将数据写入spu表中, 数据中缺少 saleable, valid, createtime, lastupdatetime
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        //2,新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insert(spuDetail);

        saveSkuAndStock(spuBo);

        sendMessage("insert", spuBo.getId());
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku->{
            //3. 新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //4新增库存stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }
    //根据主键查询spuDetail
    public SpuDetail querySpuDetailBySpuId(Long spuId){
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null){
            throw new RuntimeException("查询详情失败");
        }
        return spuDetail;
    }
    //根据主键 查询skus集合
    public List<Sku> querySkusBySpuId(Long spuId){
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(record);

        //查询每一个sku对应的库存
        skus.forEach(sku ->{
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            if (stock == null){
                throw new RuntimeException("查询sku库存失败");
            }
            sku.setStock(stock.getStock());
        });
        return skus;
    }


    //更新  先删后增
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //1.先删除sku和stock
        //收集所有的sku, 删除stock
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = skuMapper.select(record);
        skus.forEach(sku ->{
            stockMapper.deleteByPrimaryKey(sku.getId());
        });

        //根据spuId直接删除sku
        skuMapper.delete(record);

        //2. 再新增sku和stock
        saveSkuAndStock(spuBo);

        //3.更新spu和spuDetail
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);

        spuMapper.updateByPrimaryKeySelective(spuBo);

        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        this.sendMessage("update", spuBo.getId());
    }

    //删除  删除二合一（多个单个）
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id) {
        //删除spu表中数据
        this.spuMapper.deleteByPrimaryKey(id);

        //删除su_detail
        Example example = new Example(SpuDetail.class);
        example.createCriteria().andEqualTo("spuId",id);
        this.spuDetailMapper.deleteByExample(example);

        List<Sku> skuList = this.skuMapper.selectByExample(example);
        for (Sku sku : skuList) {
            //删除sku中的数据
            this.skuMapper.deleteByPrimaryKey(sku.getId());
            //删除stock中数据
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }

        this.sendMessage("delete", id);
    }

    //上下架
    @Transactional
    public void goodsSolddOut(Long id) {
        //下架或上架的商品
        Spu oldSpu = this.spuMapper.selectByPrimaryKey(id);
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",id);
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        if (oldSpu.getSaleable()){
            //下架
            oldSpu.setSaleable(false);
            this.spuMapper.updateByPrimaryKeySelective(oldSpu);
            //下架sku中的商品
            for (Sku sku : skuList) {
                sku.setEnable(false);
                this.skuMapper.updateByPrimaryKeySelective(sku);
            }
        }else{
            //上架
            oldSpu.setSaleable(true);
            this.spuMapper.updateByPrimaryKeySelective(oldSpu);
            //上架sku中的商品
            for (Sku sku : skuList) {
                sku.setEnable(true);
                this.skuMapper.updateByPrimaryKeySelective(sku);

            }
        }
        this.sendMessage("update", id);
    }
    ////根据spu的id查询spu
    public Spu querySpuById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }
    //根据sku的skuId查询sku
    public Sku querySkuById(Long skuId) {
        return skuMapper.selectByPrimaryKey(skuId);
    }

    private void sendMessage(String type,Long id ){
        //发送消息
        try{
            this.amqpTemplate.convertAndSend("item." +type, id);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
