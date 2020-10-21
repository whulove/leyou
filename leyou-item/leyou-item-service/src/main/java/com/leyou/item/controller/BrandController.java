package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import com.netflix.ribbon.proxy.annotation.Http;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 根据查询条件分页并排序查询品牌信息
     * http://api.leyou.com/api/item/brand/page?key=&page=1&rows=5&sortBy=id&desc=false
     */
    @GetMapping("page")
    public ResponseEntity<PageResult> queryBrandsByPage(
            @RequestParam(value="key", required = false)String key,     //检索条件,false代表不可查询
            @RequestParam(value="page",defaultValue = "1")Integer page, //页面
            @RequestParam(value="rows",defaultValue = "5")Integer rows, //一页几条
            @RequestParam(value="sortBy",required = false)String sortBy, //排序方式
            @RequestParam(value="desc",required = false)Boolean desc  //是否降序
    ){
        PageResult<Brand> result = this.brandService.queryBrandsByPage(key,page,rows,sortBy,desc);
        if (CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    //http://api.leyou.com/api/item/brand
    //新增品牌
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //对品牌进行修改
    //http://api.leyou.com/api/item/category/bid/1115
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        brandService.updateBrand(brand,cids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //删除        删除tb_brand中的数据,单个删除、多个删除二合一
    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") String bid){
        String separator="-";
        if (bid.contains(separator)){
            String[] ids = bid.split(separator);
            for (String id : ids) {
                this.brandService.deleteBrand(Long.parseLong(id));
            }
        }else{
            this.brandService.deleteBrand(Long.parseLong(bid));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    //根据cid查询品牌
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCId(@PathVariable("cid")Long cid){
        List<Brand>  brands = brandService.queryBrandsByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            return ResponseEntity.notFound().build();
        }
        return  ResponseEntity.ok(brands);
    }

    //根据id查询品牌
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        Brand brand = brandService.queryBrandById(id);
        if (brand == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }
}
