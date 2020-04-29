package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePicReposioty extends JpaRepository<CoursePic,String> {
    //通过id删除
    long deleteByCourseid(String courseId);
}
