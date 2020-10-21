package com.leyou.auth.service;


import com.leyou.auth.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserClient userClient;

    //登陆验证
    public String login(String username, String password) {
        User user = userClient.queryUser(username, password);
        System.out.println(user);
        if (user == null){
            return  null;
        }

        //初始化载荷信息
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());

        try{
            //返回token信息 jwt类型
            String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            System.out.println(token);
            return token;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
