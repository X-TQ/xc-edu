package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;

public interface MediaFileService {
    //查询媒资文件列表
    QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);
}
