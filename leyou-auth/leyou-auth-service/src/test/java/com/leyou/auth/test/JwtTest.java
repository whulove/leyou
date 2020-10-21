package com.leyou.auth.test;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private  static final String pubKeyPath = "C:\\tmp\\rsa\\rsa.pub";
    private  static final String priKeyPath = "C:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception{
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception{
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey  = RsaUtils.getPrivateKey(priKeyPath);
    }
    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }
    @Test
    public void testParseToken() throws Exception {
        // String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4NDYzMzAzMX0.ZRbTwzMNJkipR_SQGPtOE92KiKRuBpqrLszlTVk_TtLA9xXucW5--IQAOij6TeO06cPpWGe431q_p7UFXyNmnspTPsHvSNjjQq7wtuX11ON0S24v6UcnNCdhVSCxya5gR_NkF2L_MAkcu0z3zc-GkCbNemURiesZtkTQpR9wNOc\n";
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU5NjYyOTk2NH0.RQxY18iUWg724nKkEmE4nLLm4eYnakClQSucWLB2eSRm6MjtwbSxBaWnQpaG0tdMrtUpCnPJTLQYW699Tc9OfxjtfjcVx4FptEm_T40a_XvFOP23Ga0KCJmm-j1umtcDkmk5_tVVjzc_kRuemfhzSVaQgv1LfaO7qK5MiPZo2a0";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }

}
