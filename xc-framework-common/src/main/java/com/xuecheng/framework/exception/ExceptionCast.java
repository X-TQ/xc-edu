package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author xtq
 * @Date 2020/2/22 9:07
 * @Description
 */

public class ExceptionCast {

    public static void cast(ResultCode resultCode){
        throw new CustomerException(resultCode);
    }
}
