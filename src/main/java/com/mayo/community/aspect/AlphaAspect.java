package com.mayo.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * AOP切面
 */
//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.mayo.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("aroundBefore");
        Object obj = joinPoint.proceed();//接入点
        System.out.println("aroundAfter");
        return obj;
    }

}
