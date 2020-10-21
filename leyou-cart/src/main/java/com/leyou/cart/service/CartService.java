package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    //添加购物车
    public void addCart(Cart cart) {
        //获取登陆用户
        UserInfo userInfo = LoginInterceptor.get();
        // 查询
        BoundHashOperations<String , Object,Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());

        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();

        if (hashOperations.hasKey(skuId)){
            //判断是否有, 更新数量
            String cartJson = hashOperations.get(skuId).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(num + cart.getNum());
        }else{
            //没有, 新增
            cart.setUserId(userInfo.getId());
            //查询商品信息
            Sku sku = this.goodsClient.querySkuById(cart.getSkuId());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }
        //将购物车数据写入redis
        hashOperations.put(skuId.toString(), JsonUtils.serialize(cart));
    }

    //查询购物车
    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo userInfo = LoginInterceptor.get();
        //判断购物车是否存在,判断hash操作对象是否存在
        if(!this.redisTemplate.hasKey(userInfo.getId().toString())){
            //不存在, 返回
            return null;
        }
        //查询购物车数据
        BoundHashOperations<String ,Object, Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());
        List<Object> carts = hashOperations.values();

        //判断是否有数据
        if (CollectionUtils.isEmpty(carts)){
            return null;
        }
        return carts.stream().map(cartJson ->JsonUtils.parse(cartJson.toString(),Cart.class)).collect(Collectors.toList());

    }

    //更新数量
    public void updateCarts(Cart cart) {
        //获取登录用户
        UserInfo userInfo = LoginInterceptor.get();
        //操作hash对象
        BoundHashOperations<String ,Object, Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());
        //获取购物车信息
        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();
        Cart cart1 = JsonUtils.parse(cartJson, cart.getClass());
        //更新数量
        cart1.setNum(cart.getNum());
        //写入购物车
        hashOperations.put(cart.getSkuId().toString(), JsonUtils.serialize(cart1));
    }

    //删除
    public void delete(String skuId) {
        //获取登录用户
        UserInfo userInfo = LoginInterceptor.get();
        BoundHashOperations<String ,Object, Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());
        hashOperations.delete(skuId);
    }
}
