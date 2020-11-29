package com.leyou.auth.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\BaiduNetdiskDownload\\01 - 黑马JAVA EE 57期\\04-项目1\\07-乐优商城\\乐优商城-11月版\\leyou\\day17-授权中心\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\BaiduNetdiskDownload\\01 - 黑马JAVA EE 57期\\04-项目1\\07-乐优商城\\乐优商城-11月版\\leyou\\day17-授权中心\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "123abc");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        //String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTUzMzI4MjQ3N30.EPo35Vyg1IwZAtXvAx2TCWuOPnRwPclRNAM4ody5CHk8RF55wdfKKJxjeGh4H3zgruRed9mEOQzWy79iF1nGAnvbkraGlD6iM-9zDW8M1G9if4MX579Mv1x57lFewzEo-zKnPdFJgGlAPtNWDPv4iKvbKOk1-U7NUtRmMsF1Wcg";
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTYwNTkyNzk0M30.maxI3btd9wn42Vx617vyMdlf05od1vcjtg8O1SR6jPCX6hABfK3Y8bqEce2CcaLdhz6L6wUXqI7J0-4iZRvxPHLXOWzZJoC0SfFl5UbTGB9VhCFodeLE-Jm5FnLCigvE9RZno1QIqGqG8Zh04NXWtjokT2v5zL_NT4GK8csrbbU";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
