package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    //http://api.leyou.com/api/item/spec/groups/76
    //根据分类id查询规格参数组
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> groups = specificationService.queryGroupsByCid(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    //http://api.leyou.com/api/item/spec/params?gid=1
    //根据组id查询规格参数       gid  cid   group_id
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(@RequestParam(value="gid",required = false)Long gid,
                                                       @RequestParam(value="cid",required = false)Long cid,
                                                       @RequestParam(value="generic",required = false)Boolean generic,
                                                       @RequestParam(value="searching",required = false)Boolean searching){
        List<SpecParam> params = specificationService.queryParams(gid,cid, generic,searching);
        if (CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);

    }

    // 新增规格模板的分组 group
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroup specGroup){
        specificationService.saveSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //删除规格模板中的某一分组
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpecgroup(@PathVariable("id") Long id){
        specificationService.deleteSpecGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //修改分组
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroup specGroup){
        specificationService.updateSpecGroup(specGroup);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //新增规格模板中某一组的规格参数
    @PostMapping("param")
    public ResponseEntity<Void> saveSpecParam(@RequestBody SpecParam specParam){
        specificationService.saveSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    //修改规格参数中某一个分组的某个规格参数
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam specParam){
        specificationService.updateSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //删除规格参数中某一分组的某个模版参数
    @DeleteMapping("param/{paramId}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("paramId") Long paramId){
        specificationService.deleteSpecParam(paramId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    //查询cid查询规格参数组
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupWithParam(@PathVariable("cid")Long cid){
        List<SpecGroup> groups = specificationService.queryGroupWithParam(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }
}
