package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/3/11 18:38
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 查询搜索匹配的
     * @throws IOException
     */
    @Test
    public void searchTest01() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式(全部搜索)
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }


    /**
     * 分页查询
     * @throws IOException
     */
    @Test
    public void searchPageTest02() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式(全部搜索)
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }

    /**
     * termQuery
     * 精确查询
     * @throws IOException
     */
    @Test
    public void termQueryTest03() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }

    /**
     * termQuery
     * 精确查询 通过id查询
     * @throws IOException
     */
    @Test
    public void termQueryByIdTest04() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式 通过id查询
        String[] ids = new String[]{"1","2"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }

    /**
     * matchQuery  全文检索
     * @throws IOException
     */
    @Test
    public void matchQueryTest05() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式 全文检索，它的搜索方式是先将搜索字符串分词，再使用各各词条从索引中搜索。
        /**
         * “spring开发框架”会被分为三个词：spring、开发、框架
         * 设置"minimum_should_match": "80%"表示，三个词在文档的匹配占比为80%，
         * 即3*0.8=2.4，向上取整得2，表 示至少有两个词在文档中要匹配成功。
         */
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架").minimumShouldMatch("80%"));
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }


    /**
     * multiMatchQuery  可同时搜索多个域
     * @throws IOException
     */
    @Test
    public void multiQueryTest06() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式 multiMatch 第一个参数：要搜索的关键字为spring和css 第二三参数：指定要搜索的域
        /**
         * .minimumShouldMatch("50%")  表示spring和css这两个词在文档中的匹配度为50% 即2*0.5=1 至少有1个词在文档中要匹配成功。
         * .field("name",10));  表示提升name域的权重为10倍，权重提高了，该文档信息将优先显示
         */
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description")
                .minimumShouldMatch("50%")
                .field("name",10));
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }


    /**
     * boolQueryBuilder查询  可以将不同源构建对象，进行组合起来
     * @throws IOException
     */
    @Test
    public void boolQueryBuilderTest07() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //在定义一个termQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel","201001");
        //定义boolQueryBuilder将上面两个进行组合
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }


    /**
     *过滤器 查询
     * 使用过滤器 查询性能较高
     *
     * 过虑是针对搜索的结果进行过虑，过虑器主要判断的是文档是否匹配，不去计算和判断文档的匹配度得分，
     * 所以过 虑器性能比查询要高，且方便缓存，推荐尽量使用过虑器去实现查询或者过虑器和查询共同使用。
     */
    @Test
    public void filterTest08() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //搜索方式
        //定义一个MultiMatchQuery
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);

        //定义boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        //设置过滤器
        //筛选studymodel域值201001的
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        //筛选价格在 60-100之间的  即大于60小于100
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }


    /**
     *排序sort 查询
     */
    @Test
    public void sortTest09() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 2;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //搜索方式
        //定义boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //设置过滤器
        //筛选价格在 60-100之间的  即大于60小于100
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");
        }


    }

    /**
     *高亮显示 highligh
     */
    @Test
    public void highTest10() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构造对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        int page = 1;
        int size = 3;
        int startIndex = (page-1)*size;
        searchSourceBuilder.from(startIndex);//起始下标，从0开始
        searchSourceBuilder.size(size);//没有显示的记录数
        //排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //搜索方式
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
        //设置源字段过滤，第一个参数 结果包括哪些指定，第二个参数 结果不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();;
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));//设置高亮的域
        searchSourceBuilder.highlighter(highlightBuilder);

        //搜索请求对象设置搜索源构造对象
        searchRequest.source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        System.out.println(searchResponse);

        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到搜索的文档结果集
        SearchHit[] hitsHits = hits.getHits();
        for(SearchHit searchHit : hitsHits){
            //文档的id
            String id = searchHit.getId();
            //获得源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            System.out.println(sourceAsMap);
            String name = (String) sourceAsMap.get("name");
            Double price = (Double) sourceAsMap.get("price");


            //取出高亮结果集
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if(highlightFields != null){
                //取出高亮name域的结果集
                HighlightField highlightField = highlightFields.get("name");
                if(highlightField != null){
                    //取出高亮name域的文本组
                    Text[] fragments = highlightField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text : fragments){
                        //拼装好将要高亮显示name域 的字段
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                    System.out.println(name);
                }
            }
        }


    }
}
