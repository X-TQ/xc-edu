package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseService {
    //课程计划查询
    TeachplanNode findTeachplanList(String courseId);
    //添加课程计划
    ResponseResult addTeachplan(Teachplan teachplan);
    //课程分类
    CategoryNode findCategoryList();
    //新增课程
    ResponseResult addCourserBase(CourseBase courseBase);
    //通过课程id查询course_base
    CourseBase findCourseBaseById(String id);
    //更新课程信息
    ResponseResult updateCoursebase(String id,CourseBase courseBase);
    //通过id获取课程营销信息
    CourseMarket findCourseMarketById(String courseId);
    //更新课程营销信息
    ResponseResult updateCourseMarket(String id, CourseMarket courseMarket);
    //添加课程图片
    ResponseResult addCoursePic(String courseId, String pic);
    //查询课程图片信息
    CoursePic findCoursePic(String courseId);
    //删除课程图片
    ResponseResult deleteCoursePic(String courseId);
    //课程视图查询
    CourseView getCourseView(String id);
    //课程预览
    CoursePublishResult preview(String id);
    //课程发布
    CoursePublishResult publish(String id);
    //保存课程计划与媒资文件的关联
    ResponseResult saveMedia(TeachplanMedia teachplanMedia);
    //查询课程列表
    QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest);
}
