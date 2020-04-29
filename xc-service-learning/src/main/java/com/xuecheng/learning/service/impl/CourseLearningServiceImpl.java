package com.xuecheng.learning.service.impl;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.service.CourseLearningService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author xtq
 * @Date 2020/3/18 10:21
 * @Description
 */
@Service
public class CourseLearningServiceImpl implements CourseLearningService {

    @Autowired
    private CourseSearchClient courseSearchClient;

    /**
     * 获取课程学习地址(远程调用search服务工程的，或获取课程学习地址)
     * @param courseId
     * @param teachplanId
     * @return
     */
    public GetMediaResult getmedia(String courseId, String teachplanId) {
        //远程调用search服务工程的，或获取课程学习地址
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if(teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取视频播放地址出错
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());
    }
}
