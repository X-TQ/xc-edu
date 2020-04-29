package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExcaptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @Author xtq
 * @Date 2020/3/23 22:51
 * @Description 课程管理自定义的异常类
 */

@ControllerAdvice//控制器增强
public class CustomException extends ExcaptionCatch {

    static {
        //无权限访问异常
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
