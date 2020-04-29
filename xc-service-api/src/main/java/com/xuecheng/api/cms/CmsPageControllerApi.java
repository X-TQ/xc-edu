package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author xtq
 * @Date 2020/2/20 15:22
 * @Description  cms页面管理Api
 */

@Api(value="cms页面管理接口",description = "cms页面管理接口，提供增删改查")
public interface CmsPageControllerApi {

    //页面分页查询
    @ApiOperation("页面分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
                    @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
                    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    //新增页面
    @ApiOperation("新增页面接口")
    public CmsPageResult add(CmsPage cmsPage);

    //根据页面id查询页面信息
    @ApiOperation("根据页面id查询页面信息")
    public CmsPage findById(String id);

    //修改页面
    @ApiOperation("修改页面")
    public CmsPageResult edit(String id, CmsPage cmsPage);

    //通过id删除页面
    @ApiOperation("通过id删除页面")
    public ResponseResult deleteById(String id);

    //页面发布
    @ApiOperation("页面发布")
    public ResponseResult post(String pageId);

    //保存页面
    @ApiOperation("保存页面接口")
    public CmsPageResult save(CmsPage cmsPage);

    //一键发布页面
    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
