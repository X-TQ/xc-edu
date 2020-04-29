package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.system.SysDictionary;

public interface SysDictionaryService {
    SysDictionary findByDType(String type);
}
