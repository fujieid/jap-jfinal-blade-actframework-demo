package com.jap.routes;

import com.jap.controller.admin.*;
import com.jfinal.config.Routes;

/**
 * @author hq.W
 * @program JAP-demo
 * @description admin-AdminRoutes
 */
public class AdminRoutes extends Routes {
    @Override
    public void config() {
        this.setBaseViewPath("/static");
        //OAuth2--code——登录首页
        this.add("/", Oauth2CodeController.class);
        //OAuth2--password——登录首页
        this.add("/", Oauth2PasswordController.class);
        //simple——登录首页
        this.add("/", SimpleController.class);
        //第三方登录——登录首页
        this.add("/", ThirdLoginController.class);
        //OIDC——登录页面
        this.add("/",OidcController.class);
    }
}
