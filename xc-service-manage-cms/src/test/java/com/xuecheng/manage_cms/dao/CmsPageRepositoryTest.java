package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @Author xtq
 * @Date 2020/2/20 16:34
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    /**
     * 测试查询所有
     */
    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testFindPage(){
        int page = 0;//从0开始
        int size = 5;
        Pageable pageable = PageRequest.of(page,size );
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    /**
     * 测试 修改
     */
    @Test
    public void testUpdate(){
        //先查询出来ObjectId("5df8f64345da711a24e5b46c")
        Optional<CmsPage> optional = cmsPageRepository.findById("5df8f64345da711a24e5b46c");
        CmsPage cmsPage = null;
        //判断是否存在
        if(optional.isPresent()){
            cmsPage = optional.get();
        }

        cmsPage.setPageName("xtq修改");
        cmsPageRepository.save(cmsPage);


    }


    /**
     * 测试 自定义 findByPageName  查询
     */
    @Test
    public void testFindByPageName(){
        CmsPage cmsPage = cmsPageRepository.findByPageName("xtq修改");
        System.out.println(cmsPage);
    }

    /**
     * 自定义条件查询  pageAliase别名模糊查询   siteId站点精确
     */
    @Test
    public void testFind(){
        //封装条件
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageAliase("页面");
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //创建条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("siteId",ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        Example example = Example.of(cmsPage,exampleMatcher);
        List<CmsPage> all = cmsPageRepository.findAll(example);
        System.out.println(all);

    }


    @Test
    public void testById(){
        Optional<CmsPage> optional = cmsPageRepository.findById("5e4fd10852d827db309fa8d1");
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            System.out.println(cmsPage);
        }
    }

}
