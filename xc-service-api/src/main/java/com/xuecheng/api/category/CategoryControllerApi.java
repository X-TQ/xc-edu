package com.xuecheng.api.category;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @Author xtq
 * @Date 2020/2/25 14:11
 * @Description  课程管理api
 */

@Api(value = "分类管理接口")
public interface CategoryControllerApi {

    @ApiOperation("课程分类查询")
    public CategoryNode findCategoryList();
}
