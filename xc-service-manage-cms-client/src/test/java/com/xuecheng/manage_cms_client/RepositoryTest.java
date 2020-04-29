package com.xuecheng.manage_cms_client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

/**
 * @Author xtq
 * @Date 2020/2/24 19:25
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class RepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;


    @Test
    public void test01(){
        Optional<CmsPage> optional = cmsPageRepository.findById("5e4fd10852d827db309fa8d1");
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            System.out.println(cmsPage);
        }
    }
}
