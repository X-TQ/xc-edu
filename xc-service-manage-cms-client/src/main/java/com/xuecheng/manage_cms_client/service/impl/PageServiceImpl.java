package com.xuecheng.manage_cms_client.service.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

/**
 * @Author xtq
 * @Date 2020/2/24 10:44
 * @Description
 */

@Service
public class PageServiceImpl implements PageService {

    //定义日志
    private static final Logger logger = LoggerFactory.getLogger(PageServiceImpl.class);

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    /**
     * 保存HTML页面到服务器的物理路径
     * 步骤：
     *    1.从gridFs中查询出html文件
     *    2.将html页面保存到服务器物理路径上
     * @param pageId
     */
    public void savePageToServerPath(String pageId) {
        //得到html的文件id，从cmsPage中取出htmlFileId
        CmsPage cmsPage = this.findCmsPageById(pageId);
        //获取文件id
        String htmlFileId = cmsPage.getHtmlFileId();
        //根据文件id获取文件内容
        InputStream inputStream = this.getFileById(htmlFileId);
        if(inputStream == null){
            logger.error("getFileById inputStream is null,htmlFileId:{}",htmlFileId);
            return;
        }

        //开始拼装文件将要保存在服务器中的路径 url =cmsSite.sitePhysicalPath+cmsSite.siteWebPath+cmsPage.pageWebPath+cmsPage.pageName
        //页面物理路径
        //通过站点id查询该站点信息
        CmsSite cmsSite = this.findCmsSiteById(cmsPage.getSiteId());
        String pageUrl = cmsSite.getSitePhysicalPath()+cmsSite.getSiteWebPath()+cmsPage.getPageWebPath()+cmsPage.getPageName();


        //将html页面保存到服务器物理路径上
        File file = new File(pageUrl);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 根据文件id获取文件内容
     * @param fileId
     * @return
     */
    private InputStream getFileById(String fileId){
        //通过id查询出文件信息
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开一个下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //传入gridFSDownloadStream创建GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //获取文件的输入流
        try {
            InputStream inputStream = gridFsResource.getInputStream();
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 根据页面id查询cmsPage页面信息
     */
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    /**
     * 根据站点id查询cmsSite信息
     */
    public CmsSite findCmsSiteById(String SiteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(SiteId);
        if(optional.isPresent()){
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }
}
