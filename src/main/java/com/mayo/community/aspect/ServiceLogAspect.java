package com.mayo.community.aspect;

import com.mayo.community.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AOP统一管理日志
 */
@Component
@Aspect
public class ServiceLogAspect {


    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);


    @Pointcut("execution(* com.mayo.community.service.*.*(..))")
    public void pointcut(){

    }

//    @Pointcut("execution(* com.mayo.community.dao.UserMapper.update*(..))")
//    public void pointcutUpdate(){
//
//    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 用户[111.111.111.111]在[xxx],访问了[com.mayo.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = "127.0.0.1";
        if(attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ip = request.getRemoteHost();
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s] 在 [%s] 访问了[%s]。", ip , now , target));
    }


}
