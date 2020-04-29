package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author xtq
 * @Date 2020/2/22 21:15
 * @Description
 */

public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {
}
