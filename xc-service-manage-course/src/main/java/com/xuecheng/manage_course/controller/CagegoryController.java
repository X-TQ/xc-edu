package com.xuecheng.manage_course.controller;

import com.xuecheng.api.category.CategoryControllerApi;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author xtq
 * @Date 2020/2/26 15:48
 * @Description
 */

@RestController
@RequestMapping("/category")
public class CagegoryController implements CategoryControllerApi {

    @Autowired
    private CourseService courseService;

    /**
     * 查询课程分类
     * @return
     */
    @Override
    @GetMapping("/list")
    public CategoryNode findCategoryList() {
        return courseService.findCategoryList();
    }
}
