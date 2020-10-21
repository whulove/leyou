package com.leyou.auth.controller;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletRequest request, HttpServletResponse response){
        //调用service方法 生产jwt
        String token = authService.login(username, password);
        if (StringUtils.isBlank(token)){
            return ResponseEntity.badRequest().build();
        }
        //使用cookieUtils.setCookie把jwt类型token设置给cookie
        //将token 写入cookie , 并将httpOnly设置为true, 防止通过JS获取和修改
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(),token, 60*30, "utf-8", true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token, HttpServletRequest request, HttpServletResponse response){
        try{
            ///使用公钥解析jwt, 获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            if (userInfo == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            //解析成功 重新刷新token
            token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //更新cookie中的token
            CookieUtils.setCookie(request,response, jwtProperties.getCookieName(), token, jwtProperties.getExpire()*30, null);

            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            e.printStackTrace();
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
