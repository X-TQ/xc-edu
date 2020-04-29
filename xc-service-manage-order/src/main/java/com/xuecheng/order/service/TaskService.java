package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;

import java.util.Date;
import java.util.List;

public interface TaskService {

    //查询某个时间之前的 n条记录
    List<XcTask> findTaskList(Date updateTime, int n);

    //发送消息
    void publish(XcTask xcTask,String ex,String routingkey);

    //获取任务
    int getTask(String id,int version);

    //完成任务
    void finishTask(String taskId);
}
