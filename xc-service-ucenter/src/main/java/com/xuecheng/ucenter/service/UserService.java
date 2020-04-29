package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;

public interface UserService {
    //根据用户名查询用户信息
    XcUser getUserByUsername(String username);

    //用户扩展信息
    XcUserExt getUserExt(String username);
}
