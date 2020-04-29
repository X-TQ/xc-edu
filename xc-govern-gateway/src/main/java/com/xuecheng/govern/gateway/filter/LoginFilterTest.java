package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author xtq
 * @Date 2020/3/22 20:26
 * @Description
 */
//@Component
public class LoginFilterTest extends ZuulFilter {

    //设置过滤器类型(有四种)
    @Override
    public String filterType() {
        /**
         * pre: 请求在被路由之前执行
         * routing:在路由请求时调用
         * post：在routing和error过滤器之后调用
         * error：处理请求发生错误调用
         */
        return "pre";
    }

    //过滤器序号,越小越先优先执行
    @Override
    public int filterOrder() {
        return 0;
    }

    //返回true表示要执行当前过滤器;反之则不执行该过滤器
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //编写过滤器内容
    //需求：过虑所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务
    @Override
    public Object run() throws ZuulException {
        //
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = requestContext.getRequest();
        //得到response
        HttpServletResponse response = requestContext.getResponse();

        //得到Authorization头
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒决访问
            requestContext.setSendZuulResponse(false);

            //设置响应代码
            requestContext.setResponseStatusCode(200);
            //构建响应信息
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            //转成json
            String jsonString = JSON.toJSONString(responseResult);
            //设置响应信息
            requestContext.setResponseBody(jsonString);
            //设置响应信息的格式类型为json
            response.setContentType("application/json;charset=utf-8");
            return null;
        }
        return null;
    }
}
