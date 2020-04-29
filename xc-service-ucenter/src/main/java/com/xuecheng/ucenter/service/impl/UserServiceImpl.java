package com.xuecheng.ucenter.service.impl;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import com.xuecheng.ucenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author xtq
 * @Date 2020/3/21 19:13
 * @Description
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private XcUserRepository xcUserRepository;

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    public XcUser getUserByUsername(String username) {
        XcUser xcUser = xcUserRepository.findXcUserByUsername(username);
        return xcUser;
    }

    /**
     * 用户扩展信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username) {
        //通过用户名查询用户的基本信息
        XcUser xcUser = this.getUserByUsername(username);
        if(xcUser == null){
            return null;
        }

        //创建XcUserExt
        XcUserExt xcUserExt = new XcUserExt();
        //将用户的基本信息拷贝到xcUserExt
        BeanUtils.copyProperties(xcUser,xcUserExt);
        //用户id
        String userId = xcUserExt.getId();

        //通过用户id查询该用户所属的公司id
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findXcCompanyUserByUserId(userId);
        if(xcCompanyUser!=null){
            //封装用户所属的公司id
            xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        }

        //用户权限信息...
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        xcUserExt.setPermissions(xcMenus);

        return xcUserExt;
    }



}
