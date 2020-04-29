package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.ext.UserTokenStore;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/3/19 20:19
 * @Description
 */

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi{

    @Autowired
    private AuthService authService;

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    /**
     * 登录
     * @param loginRequest
     * @return
     */
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if(loginRequest==null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginRequest==null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        //申请令牌
        AuthToken authToken = authService.getToken(username,password,clientId,clientSecret);

        //将令牌token存储到cookie
        String jti_token = authToken.getJti_token();
        this.saveCookie(jti_token);

        return new LoginResult(CommonCode.SUCCESS,jti_token);
    }



    /**
     * 退出登录
     * @return
     */
    @PostMapping("/userlogout")
    public ResponseResult loginout() {

        //拿出cookie中的用户身份令牌
        String jtiToken = this.getTokenFromCookie();

        //1.删除cookie中的用户身份令牌
        this.delCookie(jtiToken);
        //2.删除redis的用户身份令牌
        boolean flag = authService.delToken(jtiToken);
        if(!flag){
            ExceptionCast.cast(AuthCode.AUTH_LOGINOUT_ERROR);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     * 查询用户jwt令牌
     * @return
     */
    @GetMapping("/userjwt")
    public JwtResult userJwt() {
        //1.取出cookie中的用户身份令牌jtiToken
        String jtiToken = this.getTokenFromCookie();
        if(jtiToken == null){
            //用户未登录
            return new JwtResult(CommonCode.FAIL,null);
        }

        //2.拿身份令牌查jwt令牌(从redis查询令牌)
        AuthToken authToken = authService.getUserToken(jtiToken);
        if(authToken!=null){
            String jwt_token = authToken.getJwt_token();
            //将jwt令牌返回
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }

        return new JwtResult(CommonCode.FAIL,null);
    }

    //删除cookie
    private void delCookie(String jtiToken){
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //删除cookie  将cookie修改为0
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jtiToken,0,false);
    }

    //将令牌存储到cookie
    private void saveCookie(String Jti_token){
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //添加cookie
        CookieUtil.addCookie(response,cookieDomain,"/","uid",Jti_token,cookieMaxAge,false);
    }

    //取出cookie中的用户身份令牌jtiToken
    private String getTokenFromCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(map!=null & map.get("uid")!=null){
            String jtiToken = map.get("uid");
            return jtiToken;
        }
        return null;
    }
}
