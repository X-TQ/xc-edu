package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.learning.response.GetMediaResult;

public interface CourseLearningService {
    //获取课程学习地址
    GetMediaResult getmedia(String courseId, String teachplanId);
}
