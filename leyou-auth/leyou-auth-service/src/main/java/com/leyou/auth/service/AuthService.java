package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    public String accredit(String username, String password) {
        // 判断用户是否存在，需要查询表数据
        User user = this.userClient.queryUser(username, password);
        if (user == null) {
            return null;
        }
        try {
            // 生成 jwt token : 1. 载荷（id username） 2. 公钥  3.私钥  4. 过期时间
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            return JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
