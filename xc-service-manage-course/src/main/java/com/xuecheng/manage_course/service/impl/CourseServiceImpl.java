package com.xuecheng.manage_course.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.service.CourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author xtq
 * @Date 2020/2/25 15:03
 * @Description 课程service
 */

@Service
public class CourseServiceImpl implements CourseService {

    //@Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath="/course/detail/";
    //@Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath="/course/detail/";
    //@Value("${course‐publish.siteId}")
    private String publish_siteId="5e59178fa12d4b6419b93b61";
    //@Value("${course‐publish.templateId}")
    private String publish_templateId="5e58fec6a12d4b6419b93b43";
    //@Value("${course‐publish.previewUrl}")
    private String previewUrl="http://www.xuecheng.com/cms/preview/";
    //@Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre="http://localhost:31200/course/courseview/";

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    private CoursePubRepository coursePubRepository;

    @Autowired
    private CmsPageClient cmsPageClient;

    @Autowired
    private CoursePicReposioty coursePicReposioty;

    @Autowired
    private CourseMarketReposioty courseMarketReposioty;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanRepository teachplanRepository;



    /**
     * 课程计划查询
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     * 添加课程计划
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan == null){
            ExceptionCast.cast(CmsCode.CMS_ILLEGAL_PARAMETER);
        }

        String courseid = teachplan.getCourseid();
        //处理parentId
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //查询出该课程的根节点(通过couseId和parentId=0查询出该课程)
            parentid = this.getTeachplanRoot(courseid);
        }

        Teachplan teachplanNew = new Teachplan();
        //将页面提交的信息 拷贝到teachplanNew
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        //设置级别 根据父节点的级别+1
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        String grade = teachplan1.getGrade();
        if(grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        //保存
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程分类
     * @return
     */
    public CategoryNode findCategoryList() {
        CategoryNode category = categoryMapper.selectList();
        return category;
    }


    /**
     * 新增课程
     * @param courseBase
     * @return
     */
    public ResponseResult addCourserBase(CourseBase courseBase) {
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 通过课程id查询course_base
     * @param id
     * @return
     */
    public CourseBase findCourseBaseById(String id) {
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.NO_CONTENT);
        }
        CourseBase courseBase = optional.get();
        return courseBase;
    }

    /**
     * 更新课程信息
     * @param id
     * @param courseBase
     * @return
     */
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        //先查询
        CourseBase reCourseBase = this.findCourseBaseById(id);
        if(reCourseBase == null){
            ExceptionCast.cast(CommonCode.NO_CONTENT);
        }

        BeanUtils.copyProperties(courseBase,reCourseBase);
        //保存
        courseBaseRepository.save(reCourseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 通过id获取课程营销信息
     * @param courseId
     * @return
     */
    public CourseMarket findCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketReposioty.findById(courseId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.NO_CONTENT);
        }
        CourseMarket courseMarket = optional.get();
        return courseMarket;
    }

    /**
     * 更新课程营销信息
     * @param id
     * @param courseMarket
     * @return
     */
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket reCourseMarket = this.findCourseMarketById(id);
        if(reCourseMarket != null){
            BeanUtils.copyProperties(courseMarket,reCourseMarket);
            //保存
            courseMarketReposioty.save(reCourseMarket);
        }else{
            //不存在
            CourseMarket newCourseMarket = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, newCourseMarket);
            //设置课程id
            reCourseMarket.setId(id);
            courseMarketReposioty.save(newCourseMarket);
        }

        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * 添加课程图片  向课程管理数据添加课程与图片关联的信息
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        //先查询
        Optional<CoursePic> optional = coursePicReposioty.findById(courseId);
        if(optional.isPresent()){
            coursePic = optional.get();
        }
        //没有课程图片则新建
        if(coursePic == null){
            coursePic = new CoursePic();
        }

        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存
        coursePicReposioty.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片信息
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicReposioty.findById(courseId);
        if(optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long res = coursePicReposioty.deleteByCourseid(courseId);
        if(res>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 课程视图查询
     * @param id
     * @return
     */
    public CourseView getCourseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> optional1 = coursePicReposioty.findById(id);
        if(optional1.isPresent()){
            CoursePic coursePic = optional1.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程营销信息
        Optional<CourseMarket> optional2 = courseMarketReposioty.findById(id);
        if(optional2.isPresent()){
            CourseMarket courseMarket = optional2.get();
            courseView.setCourseMarket(courseMarket);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    /**
     * 课程预览
     * 步骤：
     *      1.请求cms添加页面
     *      2.拼装页面预览url
     *      3.返回CoursePublishResult对象
     * @param id
     * @return
     */
    public CoursePublishResult preview(String id) {
        //查询课程基础信息，用于获取页面别名
        CourseBase one = this.findCourseBaseById(id);
        //请求cms添加页面,远程调用
        //准备cmsPage
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        // 课程预览站点
        // 模板
        cmsPage.setTemplateId(publish_templateId);
        // 页面名称
        cmsPage.setPageName(id+".html");
        // 页面别名
        cmsPage.setPageAliase(one.getName());
        // 页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        // 页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        // 数据url
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        //远程请求cms添加页面
        CmsPageResult reCmsPage = cmsPageClient.saveCmsPage(cmsPage);
        if(!reCmsPage.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = reCmsPage.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览url
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 课程发布
     * @param id
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //更改课程的发布状态
        CourseBase courseBase = this.saveCoursePubState(id);

        //课程索引...
        /**
         * 保存课程索引信息 到course_pub表
         */
        //1.创建CoursePub对象
        CoursePub coursePub = this.createCoursePub(id);
        //2.保存coursePub到course_pub表
        CoursePub resCoursePub = this.saveCoursePub(id, coursePub);


        //存储媒资信息(向teachplanMediaPub中保存课程媒资信息)
        /**
         * 1.先通过courseId删除teachplanMediaPub
         * 2.查询通过courseId查询teachplan_media表
         * 3.将查询出来的数据，封装到TeachplanMediaPub
         */
        this.saveTeachplanMediaPub(id);


        // 课程缓存...

        // 页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //存储媒资信息(向teachplanMediaPub中保存课程媒资信息)
    private void saveTeachplanMediaPub(String courseId){
        //1.先通过courseId删除teachplanMediaPub
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //2.通过courseId查询teachplan_media表
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //3.将teachplanMediaList插入到TeachplanMediaPub
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            teachplanMediaPub.setTeachplanId(teachplanMedia.getTeachplanId());
            teachplanMediaPub.setMediaId(teachplanMedia.getMediaId());
            teachplanMediaPub.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
            teachplanMediaPub.setMediaUrl(teachplanMedia.getMediaUrl());
            teachplanMediaPub.setCourseId(teachplanMedia.getCourseId());
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        //保存
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    /**
     * 保存课程计划与媒资文件的关联
     * @param teachplanMedia
     * @return
     */
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.ILLEGAL_PARAMETER);
        }
        //校验课程计划是否是三级的
        //获取课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.ILLEGAL_PARAMETER);
        }
        //课程计划 取出等级
        Teachplan teachplan = optional.get();
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3") ){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * 查询课程列表
     * @param companyId
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        courseListRequest.setCompanyId(companyId);

        //分页
        PageHelper.startPage(page,size);
        //查询我的课程列表
        Page<CourseInfo> courseInfoPage = courseMapper.findCourseListPage(courseListRequest);

        //取出结果集
        List<CourseInfo> courseInfoList = courseInfoPage.getResult();
        //取出总条数
        long total = courseInfoPage.getTotal();

        //创建对象封装数据，返回
        QueryResult<CourseInfo> queryResult = new QueryResult<CourseInfo>();
        queryResult.setList(courseInfoList);
        queryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,queryResult);
    }


    /**
     * 保存coursePub到course_pub表
     */
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub reCoursePub = null;
        //根据id查询
        Optional<CoursePub> optional = coursePubRepository.findById(id);
        if(optional.isPresent()){
            //查到了
            reCoursePub = optional.get();
        }else{
            //没有查到
            reCoursePub = coursePub;
        }

        //将reCoursePub拷贝到coursePub
        BeanUtils.copyProperties(reCoursePub,coursePub);
        //需要手动设置的信息
        coursePub.setId(id);//id
        coursePub.setTimestamp(new Date());//时间戳
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(new Date());
        coursePub.setPubTime(time);//发布时间
        //保存
        CoursePub saveCoursePub = coursePubRepository.save(coursePub);
        return saveCoursePub;

    }


    /**
     * 创建CoursePub对象
     * @param id
     * @return
     */
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //查询课程基本信息
        Optional<CourseBase> optional1 = courseBaseRepository.findById(id);
        if(optional1.isPresent()){
            CourseBase courseBase = optional1.get();
            //将课程基本信息拷贝到coursePub
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程营销信息
        Optional<CourseMarket> optional2 = courseMarketReposioty.findById(id);
        if(optional2.isPresent()){
            CourseMarket courseMarket = optional2.get();
            //将营销信息拷贝到coursePub
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //转成json格式
        String teachplanJson = JSON.toJSONString(teachplanNode);
        //将teachplanJson保存到coursePul中
        coursePub.setTeachplan(teachplanJson);

        return coursePub;
    }


    //更改课程的发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002"); //202002表示为发布标识
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    /**
     * 查询课程的根节点，查询不到自动添加根节点
     * @param courseId
     * @return
     */
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }

        CourseBase courseBase = optional.get();

        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(teachplanList ==null || teachplanList.size()<=0){
            //查询不到  自动添加根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        //返回根节点id
        return teachplanList.get(0).getId();
    }
}
