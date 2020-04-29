package com.xuecheng.auth;

import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author xtq
 * @Date 2020/3/19 15:22
 * @Description
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    //测试生成jwt令牌
    @Test
    public void produceJwtToken(){
        //密钥库文件
        String keystore = "xc.keystore";
        //密钥库密码
        String keystore_password = "xuechengkeystore";

        //密钥别名
        String alias = "xckey";
        //密钥的访问密码
        String key_password = "xuecheng";

        //加载密钥库文件
        ClassPathResource classPathResource = new ClassPathResource(keystore);
        //创建密钥工厂
        KeyStoreKeyFactory keyStoreFactoryBean = new KeyStoreKeyFactory(classPathResource,keystore_password.toCharArray());
        //获取密钥对（私钥和公钥）
        KeyPair keyPair = keyStoreFactoryBean.getKeyPair(alias, key_password.toCharArray());
        //获得私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //jwt令牌的内容
        Map<String,String> map = new HashMap<>();
        map.put("name","XTQ");
        String contentJSon = JSON.toJSONString(map);
        //生成令牌token
        Jwt jwt = JwtHelper.encode(contentJSon, new RsaSigner(aPrivate));
        //生成jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    //校验jwt令牌
    @Test
    public void produceJwtToken2(){
        //公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //jwt令牌
        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiWFRRIn0.ceLHLV29VjypKWlyb9RCwqq11vYIvJsnT8Rh8U8WtSVnarwbwRQURMXRGkq15blGcU74eh3ctj2ohtDngj4JXr7uYO7mKHhVxsKa-aiSJLUgc5PIG2plSLtoTvxRf54XEakpL4XB-AhIM6rGz5zcnaOIb03dq0QiAsmxe6zo7BHTpPztlE2B4Ss1hzPcKYw1FhMPtHae3GqDXDqfDool3WOZ5IW8v9Xv5TMvIbwaEzllCO9rBUqrnyUaHLLf1X4yyprd_8c_XrFLBcj7KAfqo549trXN-PWcA-X-G8LyuX6SGkuw-232KMKhj3QHqHZ1ggixDQg4fLtVYIKgdkpC4g";

        //校验
        Jwt jwt = JwtHelper.decodeAndVerify(jwtToken, new RsaVerifier(publicKey));
        //拿到jwt令牌中定义的信息
        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
