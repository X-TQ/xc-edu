package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

@Api(value = "提供课程搜索服务接口",description = "课程搜索")
public interface EsCourseControllerApi {

    //搜索课程信息
    @ApiOperation("课程综合搜索")
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    //根据课程id查询课程信息
    @ApiOperation("根据课程id查询课程信息")
    public Map<String,CoursePub> getAll(String courseId);

    //ES：根据课程计划id查询课程媒资信息
    @ApiOperation("根据课程计划id查询课程媒资信息")
    public TeachplanMediaPub getMedia(String teachplanId);
}
