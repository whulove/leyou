package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtProperties jwtProperties;
    //定义一个 线程域,寸登陆用户
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
    /**
     *      * 在业务处理器处理请求之前被调用
     *      * 如果返回false
     *      *      则从当前的拦截器往回执行所有拦截器的afterCompletion(),再退出拦截器链
     *      * 如果返回true
     *      *      执行下一个拦截器，直到所有拦截器都执行完毕
     *      *      再执行被拦截的Controller
     *      *      然后进入拦截器链
     *      *      从最后一个拦截器往回执行所有的postHandle()
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //判断是否为空
        if (StringUtils.isBlank(token)){
            return false;
        }
        //解析token, 获取用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        //放入THREAD_LOCAL
        THREAD_LOCAL.set(userInfo);
        return true;
    }
    public static UserInfo get(){
        return  THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,@Nullable Exception ex) throws Exception {
        //清除线程变量,必须操作, 因为我们使用的是线程池, 一次请求完成后, 线程并没有结束
        THREAD_LOCAL.remove();
    }
}
