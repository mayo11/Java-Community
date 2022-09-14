package com.mayo.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 重复激活
     */
    int ACTIVATION_FAILD = 2;

    /**
     * 默认状态下的登录凭证超时时间
     */
    int DEAFAULT_EXPIRED_SECONDS = 3600 * 3;

    /**
     * 记住我状态下的登录状态超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 3;
}
