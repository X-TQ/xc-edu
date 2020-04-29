package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author xtq
 * @Date 2020/2/22 9:09
 * @Description  异常捕获类
 */

@ControllerAdvice//控制器增强
public class ExcaptionCatch {

    //定义一个map 配置异常类型所对应的的错误代码
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> exceptionMap;
    //定义map的builder对象，去构建ImmutableMap
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    static {
        //定义异常类型所对应的错误代码
        builder.put(HttpMessageNotReadableException.class,CommonCode.ILLEGAL_PARAMETER);
    }

    //定义日志
    private static final Logger logger =LoggerFactory.getLogger(ExcaptionCatch.class);

    //捕获可预知 自定义CustomerException异常
    @ExceptionHandler(CustomerException.class)
    @ResponseBody
    public ResponseResult catchCustomerException(CustomerException customerException){
        //记录日志
        logger.error("catch exception:{}",customerException.getMessage());

        ResultCode resultCode = customerException.getResultCode();
        return new ResponseResult(resultCode);
    }

    //捕获不可预知 Exception异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult catchException(Exception exception){
        //记录日志
        logger.error("catch exception:{}",exception.getMessage());

        if(exceptionMap == null){
            exceptionMap = builder.build();//构建
        }

        //从exceptionMap查找异常对应的错误代码
        ResultCode resultCode = exceptionMap.get(exception.getClass());
        if(resultCode == null){
            //没有找到，未知的Exception异常
            //统一抛出9999异常
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }else{
            //找到了，预知的Exception异常
            return new ResponseResult(resultCode);
        }


    }
}
