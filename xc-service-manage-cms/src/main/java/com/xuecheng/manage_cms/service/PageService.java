package com.xuecheng.manage_cms.service;


import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface PageService {

    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    CmsPageResult add(CmsPage cmsPage);

    CmsPage findById(String id);

    CmsPageResult update(String id, CmsPage cmsPage);

    ResponseResult deleteById(String id);

    CmsConfig getModel(String id);

    String getPageHtml(String pageId);

    ResponseResult post(String pageId);

    //保存页面  有则更新，没有则添加
    CmsPageResult save(CmsPage cmsPage);
    //一键发布页面
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
