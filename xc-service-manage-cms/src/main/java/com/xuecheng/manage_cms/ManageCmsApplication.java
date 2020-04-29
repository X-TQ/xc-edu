package com.xuecheng.manage_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Author xtq
 * @Date 2020/2/20 16:00
 * @Description
 */

@EnableDiscoveryClient//表示一个EurekaClient从EurekaServer发现服务
@SpringBootApplication
@EntityScan(basePackages = {"com.xuecheng.framework.domain.cms"})//扫描实体类
@ComponentScan(basePackages = {"com.xuecheng.framework"})//扫描common下的类
@ComponentScan(basePackages = {"com.xuecheng.api"})//扫描接口
@ComponentScan(basePackages = {"com.xuecheng.manage_cms"})//扫描本项目下的类
public class ManageCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageCmsApplication.class,args);
    }

    /**
     * SpringMVC提供 RestTemplate请求http接口，
     * RestTemplate的底层可以使用第三方的http客户端工具实现http 的 请求，
     * 常用的http客户端工具有Apache HttpClient、OkHttpClient等，
     * 本项目使用OkHttpClient完成http请求， 原因也是因为它的性能比较出众。
     * @return
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
