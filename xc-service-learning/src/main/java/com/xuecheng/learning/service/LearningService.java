package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.Date;

public interface LearningService {

    //添加选课
    ResponseResult add(String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask);
}
