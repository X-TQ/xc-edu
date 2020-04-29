package com.xuecheng.manage_course.service.impl;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_course.dao.SysDictionaryRepository;
import com.xuecheng.manage_course.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Author xtq
 * @Date 2020/2/26 17:38
 * @Description
 */

@Service
public class SysDictionaryServiceImpl implements SysDictionaryService {

    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    /**
     * 通过d_type查询
     * @param type
     * @return
     */
    @Override
    public SysDictionary findByDType(String type) {
        SysDictionary sysDictionary = sysDictionaryRepository.findByDType(type);
        if(sysDictionary == null){
            ExceptionCast.cast(CommonCode.NO_CONTENT);
        }
        return sysDictionary;
    }
}
