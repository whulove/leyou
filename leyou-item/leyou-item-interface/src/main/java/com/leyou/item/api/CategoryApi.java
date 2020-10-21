package com.leyou.item.api;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {
    //搜索微服务: 根据cid查询分类名称
    @GetMapping("names")
    public List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids);


}
