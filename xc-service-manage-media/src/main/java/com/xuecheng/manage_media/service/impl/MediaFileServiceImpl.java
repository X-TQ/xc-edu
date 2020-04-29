package com.xuecheng.manage_media.service.impl;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author xtq
 * @Date 2020/3/16 21:07
 * @Description
 */

@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;


    /**
     * 查询媒资文件列表
     * @param page
     * @param size
     * @param queryMediaFileRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if(queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }

        //创建封装条件的对象
        MediaFile findMediaFile = new MediaFile();
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            findMediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            findMediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            findMediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        //定义ExampleMatcher条件匹配器    .exact()精确匹配（如果不设置匹配器，默认精确匹配）
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("processStatus",ExampleMatcher.GenericPropertyMatchers.exact());

        //定义Example条件对象
        Example<MediaFile> example = Example.of(findMediaFile,exampleMatcher);

        //定义分页查询对象pageable
        if(page<=0){
            page = 1;
        }
        page = page - 1;
        if(size<=0){
            size = 10;
        }
        Pageable pageable = new PageRequest(page,size);

        //查询
        Page<MediaFile> mediaFilePage = mediaFileRepository.findAll(example, pageable);
        //获得总记录数
        long totalElements = mediaFilePage.getTotalElements();
        //获得数据列表
        List<MediaFile> mediaFileList = mediaFilePage.getContent();

        //封装 返回结果
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setTotal(totalElements);
        queryResult.setList(mediaFileList);
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }
}
