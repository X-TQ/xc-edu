package com.xuecheng.manage_cms.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author xtq
 * @Date 2020/2/22 21:41
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServerTest {

    @Autowired
    private PageService pageService;

    /**
     * 测试静态化
     */
    @Test
    public void testGenerateHtml(){
        String pageHtml = pageService.getPageHtml("5e4fd10852d827db309fa8d1");
        System.out.println(pageHtml);
    }
}
