package com.xuecheng.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/3/19 20:23
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    //远程请求spring sevurity 获取令牌
    @Test
    public void testClient(){
        //从Eureka中获取认证服务的地址（因为spring security在认证服务中）
        //从eureka中获取一个指定的服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose("xc-service-ucenter-auth");
        //此地址http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请地址(http://localhost:40400/auth/oauth/token)
        String authUrl = uri + "/auth/oauth/token";

        //定义header
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = this.getHttpBasic("XcWebApp", "XcWebApp");
        //设置header参数
        header.add("Authorization",httpBasic);

        //定义header
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //设置参数
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        //封装参数
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(body,header);

        //设置restTemplate远程调用时，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> entity = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        //获取申请令牌的信息
        Map msgMap = entity.getBody();
        System.out.println(msgMap);
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
}
