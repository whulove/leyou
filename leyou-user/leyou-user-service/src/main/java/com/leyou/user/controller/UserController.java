package com.leyou.user.controller;


import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController  {
    @Autowired
    private UserService userService;


    //检校用户名或者手机号是否可用
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(@PathVariable("data")String data, @PathVariable("type")Integer type){
        Boolean b = this.userService.checkUser(data, type);

        if (b == null ){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(b);
    }

    //发送手机验证码
    @PostMapping("code")
    public ResponseEntity<Void> verifyCode(@RequestParam("phone")String phone){
        this.userService.verifyCode(phone);
        return ResponseEntity.noContent().build();
    }

    //注册
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
        this.userService.register(user ,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //查询用户
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username")String username,@RequestParam("password")String password){
        User user = this.userService.queryUser(username,password);
        if(user == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }
}
