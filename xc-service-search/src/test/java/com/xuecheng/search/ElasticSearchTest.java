package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/3/11 10:01
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

    //注入RestHighLevelClient客户端（高级的）
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //注入RestClient客户端（低级）
    @Autowired
    private RestClient restClient;

    /**
     * 使用RestHighLevelClient
     * 删除索引
     */
    @Test
    public void deleteTest() throws IOException {
        //创建要删除的索引对象
        DeleteIndexRequest indexRequest = new DeleteIndexRequest("xc_course");
        //获得操作索引的客户端
        IndicesClient indicesClient = restHighLevelClient.indices();
        //删除索引
        DeleteIndexResponse deleteIndexResponse = indicesClient.delete(indexRequest);
        //得到响应
        boolean res = deleteIndexResponse.isAcknowledged();
        System.out.println(res);
    }

    /**
     * 使用RestHighLevelClient
     * 创建索引
     */
    @Test
    public void createTest() throws IOException {
        //创建索引对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        //设置参数
        createIndexRequest.settings(Settings.builder().put("index.number_of_shards","1").put("index.number_of_replicas","0"));
        //指定映射
        //指定映射
        createIndexRequest.mapping("doc"," {\n" +
                " \t\"properties\": {\n" +
                "            \"studymodel\":{\n" +
                "             \"type\":\"keyword\"\n" +
                "           },\n" +
                "            \"name\":{\n" +
                "             \"type\":\"keyword\"\n" +
                "           },\n" +
                "           \"description\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\":\"ik_max_word\",\n" +
                "              \"search_analyzer\":\"ik_smart\"\n" +
                "           },\n" +
                "           \"pic\":{\n" +
                "             \"type\":\"text\",\n" +
                "             \"index\":false\n" +
                "           }\n" +
                " \t}\n" +
                "}", XContentType.JSON);
        //创建操作索引的客户端
        IndicesClient indicesClient = restHighLevelClient.indices();
        //创建索引
        boolean res = indicesClient.create(createIndexRequest).isAcknowledged();
        System.out.println(res);
    }

    /**
     * 使用RestHighLevelClient
     * 创建文档(即创建索引)
     */
    @Test
    public void createDoucmentTest() throws IOException {
        //文档内容
        //准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);

        //创建文档
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
        //指定索引文档的内容
        indexRequest.source(jsonMap);
        //创建
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    /**
     * 使用RestHighLevelClient
     * 查询文档
     */
    @Test
    public void getDoucmentTest() throws IOException {
       GetRequest getRequest = new GetRequest("xc_course","doc" ,"yoZ3x3AB5gd2RxAl3IOb" );
        GetResponse response = restHighLevelClient.get(getRequest);
        Map<String, Object> source = response.getSource();
        System.out.println(source);

    }

    /**
     * 使用RestHighLevelClient
     * 更新文档
     */
    @Test
    public void updateDoucmentTest() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name","springCloud鸭");

        //创建文档对象
        UpdateRequest updateRequest = new UpdateRequest("xc_course","doc" , "yoZ3x3AB5gd2RxAl3IOb");
        //设置要更新的内容
        updateRequest = updateRequest.doc(jsonMap);
        //更新
        UpdateResponse response = restHighLevelClient.update(updateRequest);
        GetResult result = response.getGetResult();
        System.out.println(result);
    }
}
