package com.xuecheng.order.service.impl;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import com.xuecheng.order.service.TaskService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author xtq
 * @Date 2020/3/25 18:34
 * @Description
 */

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    /**
     * 查询某个时间之前的 n条记录
     * @param updateTime
     * @param n
     * @return
     */
    public List<XcTask> findTaskList(Date updateTime, int n) {
        //分页参数
        Pageable pageable = PageRequest.of(0,n);
        //查询某个时间之前的 n条记录
        Page<XcTask> xcTaskPage = xcTaskRepository.findXcTaskByUpdateTimeBefore(pageable, updateTime);
        //获取结果集
        List<XcTask> xcTaskList = xcTaskPage.getContent();
        return xcTaskList;
    }

    /**
     * 发送消息
     * @param xcTask
     * @param ex
     * @param routingkey
     */
    @Transactional
    public void publish(XcTask xcTask, String ex, String routingkey) {
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if(optional.isPresent()){
            //发送消息
            rabbitTemplate.convertAndSend(ex,routingkey,xcTask);
            //更新任务时间
            XcTask resXcTask = optional.get();
            resXcTask.setUpdateTime(new Date());
            xcTaskRepository.save(resXcTask);
        }


    }

    /**
     * 获取任务
     * @param id
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String id, int version) {
        //通过乐观锁的方式来更新数据表，如果结果大于0说明取到任务
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }

    /**
     * 完成任务
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if(optional.isPresent()){
            //当前任务
            XcTask xcTask = optional.get();
            //创建一个任务历史记录
            XcTaskHis xcTaskHis =new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);

            //将当前任务添加到任务历史记录
            xcTaskHisRepository.save(xcTaskHis);

            //删除当前任务
            xcTaskRepository.delete(xcTask);
        }
    }


}
