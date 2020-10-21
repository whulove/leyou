package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;



    //http://api.leyou.com/api/item/category/list?pid=0
    /**
     * 根据父id查找子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value="pid",defaultValue = "0")Long pid){
        if (pid == null || pid.longValue() < 0){
            //响应400 ,相当于ResponseEntity.status(HttpStatus.BAD_REQEUST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
        if (CollectionUtils.isEmpty(categories)){
            //响应404
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

    //根据品牌id查询分类
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoriesByBid(@PathVariable("bid") Long bid){
        List<Category> categories = categoryService.queryCategoryByBid(bid);
        if (categories.size() == 0 || categories == null ){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(categories);
    }

    //保存
    @PostMapping
    public ResponseEntity<Void> saveCategory(Category category){
        System.out.println(category);
        categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //删除
    @DeleteMapping(("cid/{cid}"))
    public ResponseEntity<Void> deleteCategory(@PathVariable("cid")Long id){
        this.categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //更新
    @PutMapping
    public ResponseEntity<Void> updateCategory(Category category){
        this.categoryService.updateCategory(category);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    //搜索微服务: 根据cid查询分类名称
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = categoryService.queryNamesByIds(ids);
        if (CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
    }

    //根据3级分类id  查询1-3级分类
    @GetMapping("all/level")
    public ResponseEntity<List<Category>> queryAllByCid3(@RequestParam("id")Long id){
        List<Category> list = categoryService.queryAllByCid3(id);
        if (list == null || list.size()<1){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

}
