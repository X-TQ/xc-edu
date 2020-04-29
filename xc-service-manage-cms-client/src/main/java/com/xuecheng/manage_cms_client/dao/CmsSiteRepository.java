package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 操作cms_site
 */

public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
