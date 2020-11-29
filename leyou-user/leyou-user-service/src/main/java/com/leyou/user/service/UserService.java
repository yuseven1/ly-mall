package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.PAData;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:";

    /**
     * 验证用户是否存在
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User record = new User();
        switch(type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                return null;
        }
        return !(this.userMapper.selectCount(record) == 0);
    }

    /**
     * 发送验证码到redis，并将验证请求发送到消息队列
     * @param phone
     */
    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank(phone)) {
            return;
        }
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        // 发送消息到rabbitmq
        Map<String, String> map = new HashMap<>();
        map.put("code",code);
        map.put("phone",phone);
        this.amqpTemplate.convertAndSend("leyou.sms.exchange","verifycode_sms",map);
        // 把验证码保存到redis
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 2, TimeUnit.MINUTES);
    }

    /**
     * 用户注册，新增用户
     * @param user
     * @param code
     */
    public void register(User user, String code) {
        // 获取redis中的验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        // 校验验证码
        if (!StringUtils.equals(redisCode,code)) {
            return;
        }
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 加盐+加密
        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(password);
        // 新增用户
        user.setId(null); // 防止的注入，id自增不需要设置
        user.setCreated(new Date());
        this.userMapper.insertSelective(user); // insertSelective方法区别insert方法：前面忽略null值操作，后面不忽略

    }

    /**
     * 查询用户名和密码
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
        // 通过用户名查找salt
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名是否存在
        if (user == null) {
            return null;
        }
        // 通过salt和password，获取加密密码
        String cPassword = CodecUtils.md5Hex(password, user.getSalt());
        // 加密密码和查出来的密码比较
        if (StringUtils.equals(user.getPassword(),cPassword)) {
            return user;
        }
        return null;
    }
}
