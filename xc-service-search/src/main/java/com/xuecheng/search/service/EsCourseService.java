package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;

import java.util.Map;

public interface EsCourseService {
    //课程综合搜索
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);
    //根据课程id查询课程信息
    Map<String, CoursePub> getAll(String courseId);
    //根据多个课程计划id查询课程媒资信息
    QueryResponseResult<TeachplanMediaPub> getMedia(String[] teachplanIds);
}
