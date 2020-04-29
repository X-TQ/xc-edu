package com.xuecheng.framework.domain.cms.request;

import lombok.Data;

/**
 * @Author xtq
 * @Date 2020/2/20 15:18
 * @Description 封装请求参数
 */

@Data
public class QueryPageRequest {
    //站点id
    private String siteId;
    //页面ID
    private String pageId;
    //页面名称
    private String pageName;
    //别名
    private String pageAliase;
    //模版id
    private String templateId;
}
