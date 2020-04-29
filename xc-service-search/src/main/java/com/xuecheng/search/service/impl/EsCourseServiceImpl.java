package com.xuecheng.search.service.impl;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @Author xtq
 * @Date 2020/3/12 22:53
 * @Description
 */

@Service
public class EsCourseServiceImpl implements EsCourseService {

    @Value("${xuecheng.course.index}")
    private String course_index;
    @Value("${xuecheng.media.index}")
    private String course_media_index;
    @Value("${xuecheng.course.type}")
    private String course_type;
    @Value("${xuecheng.media.type}")
    private String course_media_type;
    @Value("${xuecheng.course.source_field}")
    private String course_source_field;
    @Value("${xuecheng.media.source_field}")
    private String course_media_source_field;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 课程综合搜索
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if(courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }

        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest(course_index);
        searchRequest.types(course_type);//设置搜索类型
        //创建源构建对象SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] sourceArr = course_source_field.split(",");
        searchSourceBuilder.fetchSource(sourceArr,new String[]{});

        //设置搜索条件
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //根据关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name",10);
            //将multiMatchQueryBuilder放入boolQueryBuilder
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //根据分类搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //根据二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //给源构建对象设置搜索方式
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 12;
        }
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//开始下标
        searchSourceBuilder.size(size);//每页显示条数


        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置要高亮的字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //searchRequest设置源构建对象
        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<CoursePub>();
        List<CoursePub> list = new ArrayList<>();
        //执行搜索
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.totalHits;//获取匹配到的总记录数
            queryResult.setTotal(totalHits);

            SearchHit[] searchHits = hits.getHits();
            for(SearchHit searchHit : searchHits){
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出name域内容
                String name = (String) sourceAsMap.get("name");
                //取高亮字段name
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if(highlightFields!=null){
                    HighlightField highlightField = highlightFields.get("name");
                    if(highlightField!=null){
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for(Text fragment : fragments){
                            stringBuffer.append(fragment);
                        }
                        name = stringBuffer.toString();
                    }
                }
                coursePub.setName(name);

                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double)sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<CoursePub>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    /**
     * 根据课程id查询课程信息
     * @param courseId
     * @return
     */
    public Map<String, CoursePub> getAll(String courseId) {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest(course_index);
        //指定类型
        searchRequest.types(course_type);

        //创建源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //设置搜索条件 使用termQuery
        searchSourceBuilder.query(QueryBuilders.termQuery("id",courseId));

        //搜索对象配置源构建对象
        searchRequest.source(searchSourceBuilder);

        Map<String,CoursePub> map = new HashMap<>();
        try {
            //搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获得查询结果
            SearchHits hits = searchResponse.getHits();
            //源文档结果集
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits){
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //课程id
                String resCourseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(resCourseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(resCourseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据多个课程计划id查询课程媒资信息
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getMedia(String[] teachplanIds) {
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        try {
            //创建搜索对象
            SearchRequest searchRequest = new SearchRequest(course_media_index);
            searchRequest.types(course_media_type);

            //创建源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //设置使用termsQuery方式查询 根据多个id查询
            searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
            //过滤源字段
            String[] sourceArr = course_media_source_field.split(",");
            searchSourceBuilder.fetchSource(sourceArr,new String[]{});

            //搜索对象配置源构建对象
            searchRequest.source(searchSourceBuilder);

            //使用restHighLevelClient客户端执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //结果集
            SearchHits searchHits = searchResponse.getHits();
            //获取搜索到的总记录数
            long totalHits = searchHits.totalHits;
            queryResult.setTotal(totalHits);
            //获得源文档列表
            SearchHit[] hits = searchHits.getHits();
            for (SearchHit searchHit : hits){
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                //获得源文档
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                teachplanMediaPub.setCourseId((String)sourceAsMap.get("courseid"));
                teachplanMediaPub.setMediaUrl((String)sourceAsMap.get("media_url"));
                teachplanMediaPub.setMediaFileOriginalName((String)sourceAsMap.get("media_fileoriginalname"));
                teachplanMediaPub.setMediaId((String)sourceAsMap.get("media_id"));
                teachplanMediaPub.setTeachplanId((String)sourceAsMap.get("teachplan_id"));
                teachplanMediaPub.setTimestamp((Date)sourceAsMap.get("timestamp"));
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //封装数据集合 teachplanMediaPubList
        queryResult.setList(teachplanMediaPubList);
        //定义封装最终返回结果
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
