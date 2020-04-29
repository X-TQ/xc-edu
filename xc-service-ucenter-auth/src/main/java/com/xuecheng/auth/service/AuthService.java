package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.ext.UserTokenStore;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author xtq
 * @Date 2020/3/19 21:14
 * @Description
 */

@Service
public class AuthService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    private long tokenValiditySeconds;//token过期时间

    /**
     * 申请令牌、将令牌存储在redis
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken getToken(String username, String password, String clientId, String clientSecret) {

        //1.申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            //申请令牌失败异常
            ExceptionCast.cast(AuthCode.AUTH_LOGIN__APPLAYTOKEN_FAIL);
        }

        //2.将令牌存储在redis
        String contentJson = JSON.toJSONString(authToken);
        boolean flag = this.saveTokenToRedis(authToken.getJti_token(), contentJson, tokenValiditySeconds);
        if(!flag){
            //redis存储令牌失败
            ExceptionCast.cast(AuthCode.AUTH_REDIS_ERROR);
        }

        return authToken;
    }

    /**
     * 将令牌存储在redis
     * @param Jti_token  用户身份令牌
     * @param content  value
     * @param time  过期时间
     * @return
     */
    private boolean saveTokenToRedis(String Jti_token,String content,long time){
        String key = "user_token:" + Jti_token;
        String value = content;
        //存
        stringRedisTemplate.boundValueOps(key).set(value,time, TimeUnit.SECONDS);
        //查询是否存 成功
        Long res = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if(res<0){
           return false;
        }
        return true;
    }

    /**
     * 申请令牌的方法
     * @param username 用户名
     * @param password 密码
     * @param clientId 客户端id
     * @param clientSecret  客户端密码
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //从eureka中获得指定服务的实例
        ServiceInstance serviceInstance = loadBalancerClient.choose("xc-service-ucenter-auth");
        //此地址http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请地址(http://localhost:40400/auth/oauth/token)
        String authUrl = uri + "/auth/oauth/token";

        //定义headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //设置header参数
        //获取httpbasic
        String httpBasic = this.getHttpBasic(clientId, clientSecret);
        headers.add("Authorization",httpBasic);

        //定义body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //设置body参数
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        //封装header、body参数
        HttpEntity httpEntity = new HttpEntity(body, headers);

        //设置restTemplate远程调用时，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        //携带参数远程调用spring security申请令牌接口
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //获取申请令牌的信息
        Map tokenMes = exchange.getBody();
        if(tokenMes == null ||
                tokenMes.get("access_token") == null ||
                tokenMes.get("refresh_token")  == null ||
                tokenMes.get("jti")== null){

            //解析spring security返回的错误信息
            if(tokenMes!=null && tokenMes.get("error_description")!=null){
                //取出错误描述信息
                String error_description = (String) tokenMes.get("error_description");
                if(error_description.contains("UserDetailsService returned null")){
                    //账号不存在
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }else if(error_description.contains("坏的凭证")){
                    //密码错误
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return null;
        }

        //封装结果返回
        AuthToken authToken = new AuthToken();
        authToken.setJti_token((String)tokenMes.get("jti"));//用户身份令牌
        authToken.setJwt_token((String)tokenMes.get("access_token"));//jwt令牌
        authToken.setRefresh_token((String)tokenMes.get("refresh_token"));//刷新令牌
        return authToken;
    }


    /**
     * 获取httpbasic
     *
     * http协议定义的一种认证方式，将客户端id和客户端密码按照“客户端ID:客户端密码”的格式拼接，
     * 并用base64编 码，放在header中请求服务端。
     * 一个例子： Authorization：Basic WGNXZWJBcHA6WGNXZWJBcHA=
     */
    private String getHttpBasic(String clientId,String clientSecret){
        String basicStr = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64Utils.encode(basicStr.getBytes());
        return "Basic "+ new String(encode);
    }

    /**
     * 从redis查询令牌
     * @param jtiToken
     * @return
     */
    public AuthToken getUserToken(String jtiToken) {
        String key = "user_token:" + jtiToken;
        //从redis查询数据（令牌信息）
        String contentJson = stringRedisTemplate.opsForValue().get(key);
        if(contentJson == null){
            //ExceptionCast.cast(AuthCode.AUTH_REDIS__GET_ERROR);
            return null;
        }
        //将contentJson转成AuthToken
        AuthToken authToken = JSON.parseObject(contentJson, AuthToken.class);
        return authToken;
    }

    /**
     * 删除redis的用户令牌
     * @param jtiToken
     * @return
     */
    public boolean delToken(String jtiToken) {
        String key = "user_token:" + jtiToken;
        //删除
        Boolean res = stringRedisTemplate.delete(key);

        /**
         * 之所以返回true，是因为如果该key过期了（说明redis中已经没有了该token，此时业务需求已经达到了），将返回false。
         * 而此时业务需求是退出登录，只要redis中没有了该token数据就行，所以可以返回true
         */
        return true;
    }
}
