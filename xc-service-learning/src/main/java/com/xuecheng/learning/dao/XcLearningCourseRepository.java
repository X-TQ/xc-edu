package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {

    //根据userId和courseId查询
    XcLearningCourse findXcLearningCourseByUserIdAndCourseId(String userId,String courseId);

}
