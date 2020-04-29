package com.xuecheng.manage_media.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @Author xtq
 * @Date 2020/3/15 21:03
 * @Description
 */

@Service
public class MediaUploadServiceImpl implements MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    /**
     * 文件上传注册,用于文件上传前的准备，校验文件是否存在
     * 1.检查文件在磁盘上是否存在
     * 2.检查文件信息在mongodb中是否存在
     *
     *
     * 根据文件md5得到文件路径
     *  规则：
     *  一级目录：md5的第一个字符
     *  二级目录：md5的第二个字符
     *  三级目录：md5
     *  件名：md5+文件扩展名
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.检查文件在磁盘上是否存在
        //文件目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5,fileExt);
        File file = new File(filePath);
        //文件是否存在
        boolean exists = file.exists();


        //2.检查文件信息在mongodb中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if(exists && optional.isPresent()){
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在
        //检查文件所在目录是否存在
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            //目录不存在，创建
            fileFolder.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 分块检查，用于检查分块文件是否已经上传，以上传则返回true
     *
     *
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件所在目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            //分块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }
        //分块文件存不在
        return new CheckChunkResult(CommonCode.FAIL,false);
    }

    /**
     * 上传分块，用于将分块文件上传到指定路径
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //检查文分块目录是否存在
        File chunkFolder = new File(chunkFileFolderPath);
        if(!chunkFolder.exists()){
            //不存在，创建分块目录
            chunkFolder.mkdirs();
        }

        //开始上传
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            //得到文件输入流
            inputStream = file.getInputStream();
            //上传到服务器上的位置
            File uploadFile = new File(chunkFileFolderPath + chunk);
            //创建输出流
            fileOutputStream = new FileOutputStream(uploadFile);
            //拷贝到服务器上的指定位置
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并文件，用于将所有的分块文件合并为一个文件
     * 步骤：
     *  1.合并所有分块
     *  2.校验文件md5值是否和前端传入md5一致
     *  3.将文件的信息写入mongodb
     *  4.Rabbitmq发送消息
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //1.合并所有分块
        //得到分块文件所属目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath);
        //分块文件列表
        File[] listFiles = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(listFiles);
        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if(mergeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //2.校验文件md5值是否和前端传入md5一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3.将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存的相对路径
        String addfilePath = fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(addfilePath);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        //保存
        mediaFileRepository.save(mediaFile);

        //4.RabbbitMQ发送消息，将要处理视频
        this.sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //RabbbitMQ发送消息，将要处理视频
    private ResponseResult sendProcessVideoMsg(String mediaId){
        //查询数据库mediaFile
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            //不存在
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //构建消息内容
        Map<String,String> map = new HashMap<String, String>();
        map.put("mediaId",mediaId);
        String jsonMsg = JSON.toJSONString(map);
        //发送消息 参数：交换机、routingkey、消息内容
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonMsg);
        }catch (Exception ex){
            ex.printStackTrace();
            //操作失败
            ExceptionCast.cast(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //得到文件所属目录的路径
    private String getFileFolderPath(String fileMd5){
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" + fileMd5 + "/";
    }

    //得到文件的路径
    private String getFilePath(String fileMd5,String fileExt){
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +"." + fileExt;
    }

    //得到分块文件所在目录
    private String getChunkFileFolderPath(String fileMd5){
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" + fileMd5 + "/chunks/";
    }

    //合并所有分块
    private File mergeFile(List<File> fileList, File mergeFile){
        try{
            //校验合并文件是否已经存在。存在，删除；不存在，创建
            if(mergeFile.exists()){
                //存在，删除
                mergeFile.delete();
            }else{
                //不存在，创建
                mergeFile.createNewFile();
            }

            //对块文件 按照文件名称 升序排序
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())- Integer.parseInt(o2.getName())<0){
                        //升序
                        return -1;
                    }
                    return 1;
                }
            });

            //创建一个写对象
            RandomAccessFile write = new RandomAccessFile(mergeFile,"rw");
            byte[] buff = new byte[1024];
            int len = -1;
            for (File file : fileList){
                //创建读对象
                RandomAccessFile read = new RandomAccessFile(file,"r");
                while ((len=read.read(buff))!=-1){
                    write.write(buff,0,len);
                }
                read.close();
            }
            write.close();

            return mergeFile;
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    //校验文件md5值是否和前端传入md5一致
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            //得到文件md5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            //和传入的md5比较  .equalsIgnoreCase忽略大小写
            if(md5.equalsIgnoreCase(md5Hex)){
                //一致
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
