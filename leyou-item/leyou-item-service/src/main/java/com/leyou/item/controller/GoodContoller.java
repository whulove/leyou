package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodService;
import com.netflix.ribbon.proxy.annotation.Http;
import com.sun.org.apache.regexp.internal.RE;
import net.sf.jsqlparser.statement.select.First;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;

@RestController
@RequestMapping
public class GoodContoller {

    @Autowired
    private GoodService goodService;

    //查询商品spu分页
    //  http://api.leyou.com/api/item/spu/page?key=&=true&page=1&rows=5
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value="saleable",required = false)Boolean saleable,
            @RequestParam(value="page",defaultValue = "1")Integer page,
            @RequestParam(value="rows",defaultValue = "5")Integer rows
    ){
        PageResult<SpuBo> pageResult = this.goodService.querySpuBoByPage(key,saleable,page,rows);

        if (CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pageResult);
    }

    //http://api.leyou.com/api/item/goods
    //新增商品
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // http://api.leyou.com/api/item/goods
    //修改商品   ->先删除原有的商品sku和stock 再新增, 最后修改spu
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //http://api.leyou.com/api/item/spu/detail/{spuId}
    //修改商品时的数据回显,  根据主键查询spuDetail
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail = this.goodService.querySpuDetailBySpuId(spuId);
        if (spuDetail == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }


    //  http://api.leyou.com/api/item/sku/list?id=oldGoods.id
    //根据主键查询 skus
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id") Long spuId){
        List<Sku> skus = this.goodService.querySkusBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }

    //删除商品
    @DeleteMapping("/spu/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("id") String ids){
        String separator = "-";
        if (ids.contains(separator)){
            String[] goodsId = ids.split(separator);
            for (String id : goodsId) {
                this.goodService.deleteGoods(Long.parseLong(id));
            }
        }else{
            this.goodService.deleteGoods(Long.parseLong(ids));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
       // return ResponseEntity.ok().build();
    }

    //商品上下架
    @PutMapping("/spu/out/{id}")
    public ResponseEntity<Void> goodsSoldOut(@PathVariable("id") Long id){
        this.goodService.goodsSolddOut(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    ////根据spu的id查询spu  商品微服务
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        Spu spu = goodService.querySpuById(id);
        if (spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }

    //根据skuId查询sku
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("skuId")Long skuId){
        Sku sku = goodService.querySkuById(skuId);
        if (sku == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(sku);
    }
}
