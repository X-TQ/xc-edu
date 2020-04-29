package com.xuecheng.manage_cms_client.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;

public interface PageService {
    //保存HTML页面到服务器的物理路径
    void savePageToServerPath(String pageId);
    //根据页面id查询cmsPage页面信息
    CmsPage findCmsPageById(String pageId);
    //根据站点id查询cmsSite信息
    CmsSite findCmsSiteById(String SiteId);
}
