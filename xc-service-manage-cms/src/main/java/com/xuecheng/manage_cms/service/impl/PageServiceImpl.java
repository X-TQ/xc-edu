package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @Author xtq
 * @Date 2020/2/20 18:13
 * @Description
 */

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 页面发布
     * 步骤：
     *     1.执行页面静态化
     *     2.将静态化文件文件存储在GridFs中
     *     3.向mq发送消息
     * @param pageId
     * @return
     */
    @Override
    public ResponseResult post(String pageId) {
        //1.执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //2.将静态化文件文件存储在GridFs中
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_ILLEGAL_PARAMETER);
        }
        //3.向mq发送消息
        this.sendPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 保存页面  有则更新，没有则添加
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //查询页面是否存在
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 != null){
            //更新
            return this.update(cmsPage1.getPageId(), cmsPage);
        }
        //添加
        return this.add(cmsPage);
    }

    /**
     * 一键发布页面
     * 1、接收课程管理服务发布的页面信息
     * 2、将页面信息添加到 数据库（mongodb）
     * 3、对页面信息进行静态化
     * 4、将页面信息发布到服务器
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息保存到cmspage集合中
        CmsPageResult result = this.save(cmsPage);
        if(!result.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage reCmsPage = result.getCmsPage();
        String pageId = reCmsPage.getPageId();
        //执行页面发布(先静态化，保存到GridFS中，在向mq发送消息)
        ResponseResult result1 = this.post(pageId);
        if(!result1.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼装页面url =cmsSite.siteDomain+cmsSite.siteWebPath+cmsPage.pageWebPath+cmsPage.pageName
        //取出站点id
        String siteId = reCmsPage.getSiteId();
        //通过id查询该站点id信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        if(cmsSite == null){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //开始拼装url
        String pageUrl = cmsSite.getSiteDomain()+cmsSite.getSiteWebPath()+reCmsPage.getPageWebPath()+reCmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 通过id查询该站点id信息
     * @return
     */
    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }


    /**
     * 向mq发送消息
     * @param pageId
     */
    private void sendPostPage(String pageId){
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);

        //拼装消息
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转成json
        String jsonMsg = JSON.toJSONString(msg);
        //发送消息
        //routingkey
        String routingkey = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(
                RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,
                routingkey,jsonMsg);

    }

    /**
     * 保存静态页面内容到GridFs中
     */
    private CmsPage saveHtml(String pageId,String htmlContent){
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);

        InputStream inputStream = null;
        ObjectId objectId = null;
        //将hmtl文件内容保存到GridFs
        try {
            inputStream = IOUtils.toInputStream(htmlContent, "UTF-8");
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //更新cmsPage的html文件id
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);

        return cmsPage;
    }

    /**
     * 页面静态化
     *      1.静态化程序获取页面的DataUrl
     * 	    2.静态化程序远程请求DataUrl获取数据模型。
     * 	    3.静态化程序获取页面的模板信息
     * 	    4.执行页面静态化
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        //1.获取数据模型
        Map model = this.getModelByPageId(pageId);
        if(model == null){
            //抛出取不到数据 异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //2.获取模板信息
        String template = this.getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(template)){
            //抛出模板为空 异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //3.执行静态化
        String pageHtml = this.generateHtml(template, model);
        if(StringUtils.isEmpty(pageHtml)){
            //抛出执行静态化失败 异常
            ExceptionCast.cast(CmsCode.CMS_NO_GENERATERHTML);
        }
        return pageHtml;
    }

    //执行静态化
    private String generateHtml(String templateContent,Map model){
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //配置模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //设置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);

        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //进行静态化
            String page = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //获取模板信息
    private String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            //抛出页面找不到异常
            ExceptionCast.cast(CmsCode.CMS_PAGE_NO);
        }
        //获取页面模板id
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            //抛出模板id为空 异常
            ExceptionCast.cast(CmsCode.CMS_NO_TEMPLATEID);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id templateFileId
            String templateFileId = cmsTemplate.getTemplateFileId();

            //获取模板文件
            //通过id查询出文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //从流中获取数据
            try {
                String templateContent = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return templateContent;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //获取数据模型
    private Map getModelByPageId(String pageId){
        //取出dataUrl
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            //抛出页面找不到异常
            ExceptionCast.cast(CmsCode.CMS_PAGE_NO);
        }
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            //抛出dataUrl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //请求接口 dataUrl
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = responseEntity.getBody();
        return body;
    }

    /**
     * 页面查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        //封装自定义条件查询
        CmsPage cmsPage = new CmsPage();
        //判断QueryPageRequest是否为空
        if(queryPageRequest == null){
            queryPageRequest= new QueryPageRequest();

        }

        //设置条件值（站点id）  StringUtils.isNotEmpty判断是否不为空
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id作为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //配置条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //参数判断
        if(page<=0){
            page = 1;
        }
        page = page -1;
        if(size<=5){
            size = 5;
        }
        Pageable pageable = PageRequest.of(page,size );
        Example example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example,pageable);
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(cmsPages.getContent());
        queryResult.setTotal(cmsPages.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult add(CmsPage cmsPage) {
        if(cmsPage == null){
            //抛出异常，非法参数
            ExceptionCast.cast(CmsCode.CMS_ILLEGAL_PARAMETER);
        }

        //先根据页面名称、站点、页面webpath校验页面是否已经存在
        CmsPage reCmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(),cmsPage.getPageWebPath());
        if(reCmsPage != null){
            //抛出异常，页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);

    }
    /*@Override
    public CmsPageResult add(CmsPage cmsPage) {
        //先根据页面名称、站点、页面webpath校验页面是否已经存在
        CmsPage reCmsPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(),cmsPage.getPageWebPath());
        if(reCmsPage == null){
            //不存在，添加
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }*/

    /**
     * 根据页面id查询页面信息
     * @param id
     * @return
     */
    @Override
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    /**
     * 修改页面
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult update(String id, CmsPage cmsPage) {
        //根据id查询页面信息
        CmsPage one = this.findById(id);
        if(one!=null){
            //准备更新数据
            //设置要修改的数据
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新datUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //提交修改
            cmsPageRepository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS,one);
        }
        //修改失败
        return new CmsPageResult(CommonCode.FAIL,null);

    }

    /**
     * 通过id删除页面
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteById(String id) {
        //先通过id查询出是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 通过id 获取cms_config信息
     * @param id
     * @return
     */
    @Override
    public CmsConfig getModel(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }
}
