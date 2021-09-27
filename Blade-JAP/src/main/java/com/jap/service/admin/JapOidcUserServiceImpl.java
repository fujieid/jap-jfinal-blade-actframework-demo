package com.jap.service.admin;

import com.blade.ioc.annotation.Bean;
import com.fujieid.jap.core.JapUser;
import com.fujieid.jap.core.JapUserService;
import com.fujieid.jap.oauth2.token.AccessToken;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.utils.StringUtils;
import me.zhyd.oauth.utils.UuidUtils;

import java.util.List;
import java.util.Map;

/**
 * @author hq.W
 * @program JAP-demo
 * @description 实现JAP 调用（操作）开发者业务系统中用户的接口-JapOauth2UserServiceImpl
 */
@Slf4j
@Bean
public class JapOidcUserServiceImpl implements JapUserService {
    /**
     * 模拟 DB 操作
     */
    private static final List<JapUser> userDatas = Lists.newArrayList();

    static {
        JapUser japUser = new JapUser();
        japUser.setUserId("1");
        japUser.setUsername("justauth");
        japUser.setPassword("justauthpassword");
        userDatas.add(japUser);
    }
    /**
     * 根据第三方平台标识（platform）和第三方平台的用户 uid 查询数据库
     *
     * @param platform 第三方平台标识
     * @param uid      第三方平台的用户 uid
     * @return JapUser
     */
    @Override
    public JapUser  getByPlatformAndUid(String platform, String uid) {
        // FIXME 注意：此处仅作演示用，并没有判断 platform，实际业务系统中需要根据 platform 和 uid 进行获取唯一用户，这里以gitee为例
        if(platform.equals("gitee")) {
            return userDatas.stream()
            .filter(user -> user.getUserId().equals(uid))
            .findFirst()
            .orElse(null);
        }
        return null;
    }

    /**
     * 创建并获取第三方用户，相当于第三方登录成功后，将授权关系保存到数据库（开发者业务系统中 oauth2 user -> sys user 的绑定关系）
     *
     * @param platform 第三方平台标识
     * @param userInfo JustAuth 中的 AuthUser
     * @return JapUser
     */
    @Override
    public JapUser createAndGetOauth2User(String platform, Map<String, Object> userInfo, Object tokenInfo) {
        // FIXME 业务端可以对 tokenInfo 进行保存或其他操作
        AccessToken accessToken = (AccessToken) tokenInfo;
        // FIXME 注意：此处仅作演示用，不同的 oauth 平台用户id都不一样，此处需要开发者自己分析第三方平台的用户信息，提取出用户的唯一ID
        //第三方应用用户部分信息如下
        if(StringUtils.isEmpty((String) userInfo.get("upn"))){
//            aliyun 主账号信息
            System.out.print(
                    "\n------------------------------------------------------------------------------------------------------\n\t"+
                    "your information:\n\t"+
                    "uid: \t\t\t\t\t" + userInfo.get("uid") + "\n\t" +
                    "login_name: \t\t\t" + userInfo.get("login_name") + "\n\t" +
                    "name: \t\t\t\t\t" + userInfo.get("name") + "\n\t" +
                    "requestid: \t\t\t\t" + userInfo.get("requestid") + "\n\t" +
                    "bid: \t\t\t\t\t" + userInfo.get("bid") + "\n\t" +
                    "aid: \t\t\t\t\t" + userInfo.get("aid") + "\n\t" +
                    "sub: \t\t\t\t\t" + userInfo.get("sub") +
                    "\n------------------------------------------------------------------------------------------------------"
            );
        }
        else{
            //aliyun RAM用户访问信息
            System.out.print(
                    "\tuid: \t\t\t\t\t"+ userInfo.get("uid") + "\n\t" +
                    "name: \t\t\t\t\t"+userInfo.get("name") + "\n\t" +
                    "requestid: \t\t"+userInfo.get("requestid") + "\n\t" +
                    "bid: \t\t\t\t\t" + userInfo.get("bid") + "\n\t" +
                    "aid: \t\t\t\t\t"+userInfo.get("aid") + "\n\t" +
                    "sub: \t\t\t\t\t"+ userInfo.get("sub") + "\n\t" +
                    "upn: \t\t\t\t\t"+ userInfo.get("upn"));
        }

        String uid = String.valueOf(userInfo.get("uid"));
        if (uid == null){
            uid = UuidUtils.getUUID();
        }
        // 查询绑定关系，确定当前用户是否已经登录过业务系统
        JapUser japUser = this.getByPlatformAndUid(platform, uid);
        if (null == japUser) {
            japUser = createJapUser(userInfo);
            japUser.setAdditional(userInfo);
            userDatas.add(japUser);
        }
        return japUser;
    }
    private JapUser createJapUser(Map<String, Object> userInfo) {
        JapUser user = new JapUser();
        user.setUserId(String.valueOf(userInfo.get("uid")));         //gitee的用户id 类型为Integer，需要转为String
        user.setUsername(userInfo.get("login_name").toString());
        user.setPassword("安全关系，暂不显示，详细信息控制台可见");
        return user;
    }

    @Override
    public JapUser createAndGetSocialUser(Object userInfo) {
        AuthUser authUser = (AuthUser) userInfo;
        // 查询绑定关系，确定当前用户是否已经登录过业务系统
        JapUser japUser = this.getByPlatformAndUid(authUser.getSource(), authUser.getUuid());
        if (null == japUser) {
            japUser = createJapUser(null);
            japUser.setAdditional(authUser);
            userDatas.add(japUser);
        }
        return japUser;
    }
}
