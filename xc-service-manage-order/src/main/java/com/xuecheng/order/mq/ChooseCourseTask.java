package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @Author xtq
 * @Date 2020/3/25 16:18
 * @Description
 */

@Component
public class ChooseCourseTask {

    private static final Logger logger = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    private TaskService taskService;

    //定时发送添加选课任务
    //@Scheduled(cron = "0 0/1 * * * *")//每隔1分钟执行一次定时任务
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChooseCourseTask(){
        //得到一分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        //1.调用service查询某个时间之前的 n条记录
        List<XcTask> xcTaskList = taskService.findTaskList(time, 100);
        //System.out.println(xcTaskList);
        //2.调用service发送消息，将添加选课的任务发送给mq
        for(XcTask xcTask : xcTaskList){
            //取任务  乐观锁  大于0说明取到任务（即当前获取到的xcTask任务，没有被其他事务所占用）
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                //要发送消息的交换机
                String exchange = xcTask.getMqExchange();
                //发送消息要带的routingkey
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,exchange,routingkey);
            }

        }
    }

    //监听完成选课 队列
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receivefinishChooseCourseTask(XcTask xcTask){
        if (xcTask!=null && StringUtils.isNotEmpty(xcTask.getId())){
            //完成选课
            taskService.finishTask(xcTask.getId());
        }
    }

    //@Scheduled(fixedDelay = 3000)//上次执行完毕后3秒执行
    //@Scheduled(fixedRate = 3000)//上次执行开始后3秒执行
    //@Scheduled(cron = "0/3 * * * * *")//每隔3秒执行一次
    public void task01(){
        logger.info("=====task01开始任务=====");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("=====task01执行完毕=====");
    }

    //@Scheduled(cron = "0/3 * * * * *")//每隔3秒执行一次
    public void task02(){
        logger.info("=====task02开始任务=====");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("=====task02执行完毕=====");
    }
}
