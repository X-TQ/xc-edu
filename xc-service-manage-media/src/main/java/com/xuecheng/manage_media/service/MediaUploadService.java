package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadService {
    //文件上传注册,用于文件上传前的准备，校验文件是否存在
    ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
    //分块检查，用于检查分块文件是否已经上传，以上传则返回true
    CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);
    //上传分块，用于将分块文件上传到指定路径
    ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5);
    //合并文件，用于将所有的分块文件合并为一个文件
    ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);
}
