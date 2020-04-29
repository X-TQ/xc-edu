package com.xuecheng.filesystem.service;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemService {
    //上传文件
    UploadFileResult uploaad(MultipartFile multipartFile, String filetag, String businesskey, String metadata);
}
