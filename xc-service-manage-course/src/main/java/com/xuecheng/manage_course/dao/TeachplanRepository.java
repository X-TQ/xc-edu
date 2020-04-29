package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @Author xtq
 * @Date 2020/2/25 16:45
 * @Description
 */

public interface TeachplanRepository extends JpaRepository<Teachplan,String> {

    //通过couseId和parentId=0查询出该课程
    List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
