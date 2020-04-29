package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "媒资管理接口",description = "媒资管理接口，提供文件上传，文件处理等接口")
public interface MediaUploadControllerApi {

    //文件上传注册,用于文件上传前的准备，校验文件是否存在
    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    //分块检查，用于检查分块文件是否已经上传，以上传则返回true
    @ApiOperation("分块检查")
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);

    //上传分块，用于将分块文件上传到指定路径
    @ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5);

    //合并文件，用于将所有的分块文件合并为一个文件
    @ApiOperation("合并文件")
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

}
