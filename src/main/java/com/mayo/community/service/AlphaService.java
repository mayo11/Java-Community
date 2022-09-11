package com.mayo.community.service;

import com.mayo.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service //业务注解
//容器管理初始化和销毁的方法，一般为单例模式
//@Scope("prototype")//多实例方式
public class AlphaService {

    @Autowired//依赖注入，使Service依赖于AlphaDao
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }
    @PostConstruct//在构造器之后调用，初始化数据
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy//在对象销毁之前调用，释放资源
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }
}
