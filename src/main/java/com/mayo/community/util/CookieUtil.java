package com.mayo.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

//从请求中获取指定Cookie
public class CookieUtil {

    public static String getValue(HttpServletRequest request , String name){
        if(request == null || name ==null){
            throw new IllegalArgumentException("指定Cookie参数key为空");
        }

        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
