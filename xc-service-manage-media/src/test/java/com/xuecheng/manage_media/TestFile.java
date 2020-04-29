package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author xtq
 * @Date 2020/3/15 19:01
 * @Description
 */

/*@SpringBootTest
@RunWith(SpringRunner.class)*/
public class TestFile {

    /***
     * 测试文件分块
     */
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\xuechengEdu\\xc\\video\\lucene.avi");
        //块文件目录
        String chunkFileFolder = "D:\\xuechengEdu\\xc\\video\\chunks\\";

        //定义块文件大小  1兆
        long chunkFileSize = 1*1024*1024;
        //块数=源文件/块文件大小
        long chunkFileNum = (long) Math.ceil(sourceFile.length()*1.0/chunkFileSize);

        //读源文件
        RandomAccessFile read = new RandomAccessFile(sourceFile, "r");

        for(int i=1;i<=chunkFileNum;i++){
            //创建写分块文件的流
            File chunkFile = new File(chunkFileFolder+i);
            RandomAccessFile write = new RandomAccessFile(chunkFile, "rw");
            //创建缓存字节数组
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len=read.read(bytes))!= -1){
                write.write(bytes,0,len);
                //如果块文件的大小达到 1M开始写下一块儿
                if(chunkFile.length()>=chunkFileSize){
                    break;
                }
            }
            write.close();
        }
        read.close();
    }

    /**
     * 合并文件
     */
    @Test
    public void testMerge() throws IOException {
        File newFile = new File("D:\\xuechengEdu\\xc\\video\\chunks\\lucene.avi");

        File sourceFile = new File("D:\\xuechengEdu\\xc\\video\\chunks");
        File[] files = sourceFile.listFiles();
        List<File> fileList = Arrays.asList(files);
        //将files按照文件名升序排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())-Integer.parseInt(o2.getName())<0){
                    //升序
                    return -1;
                }
                return 1;
            }
        });

        //创建写对象
        RandomAccessFile write = new RandomAccessFile(newFile,"rw");
        for (File file : files){
            //创建输入流
            RandomAccessFile read = new RandomAccessFile(file,"r");
            int len = -1;
            byte[] bytes = new byte[1024];
            while ((len=read.read(bytes)) != -1) {
                write.write(bytes,0,len);
            }
            read.close();
        }
        write.close();
    }
}
