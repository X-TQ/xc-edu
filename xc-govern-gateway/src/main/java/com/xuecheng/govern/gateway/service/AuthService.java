package com.xuecheng.govern.gateway.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author xtq
 * @Date 2020/3/22 20:57
 * @Description
 */

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //从头取出jwt令牌
    public String getJwtFormHeader(HttpServletRequest request){
        //取出头Authorization信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        //如果取到的信息，不是以"Bearer "开头的，说明信息有误
        if(!authorization.startsWith("Bearer ")){
            return null;
        }

        //截取信息中的jwt令牌(从索引下标7开始截取)
        String jwtToken = authorization.substring(7);
        return jwtToken;
    }

    //从cookie中取出token（用户身份令牌）
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String jtiToken = map.get("uid");
        if(StringUtils.isEmpty(jtiToken)){
            return null;
        }
        return jtiToken;
    }

    //查询令牌有效期
    public long getUserToken(String jtiToken) {
        String key = "user_token:" + jtiToken;
        //查询令牌有效期
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
}
