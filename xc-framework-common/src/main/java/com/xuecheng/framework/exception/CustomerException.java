package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author xtq
 * @Date 2020/2/22 9:05
 * @Description  自定义异常类
 */

public class CustomerException extends RuntimeException {

    ResultCode resultCode;

    public CustomerException(ResultCode resultCode){
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode(){
        return resultCode;
    }
}
