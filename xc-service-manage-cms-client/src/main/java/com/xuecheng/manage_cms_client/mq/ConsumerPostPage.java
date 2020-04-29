package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/2/24 13:10
 * @Description 消费者： 监听队列
 */

@Component
public class ConsumerPostPage {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    private PageService pageService;

    /**
     * 发布页面
     * @param msg
     */
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        //得到消息中的pageId
        String pageId = (String) map.get("pageId");

        //校验页面是否存在
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if(cmsPage == null){
            logger.error("page is no exist,pageId is:{}",pageId);
            return;
        }

        //调用service方法，将页面从grid下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
