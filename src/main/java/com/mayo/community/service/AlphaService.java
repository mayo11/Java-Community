package com.mayo.community.service;

import com.mayo.community.dao.AlphaDao;
import com.mayo.community.dao.DiscussPostMapper;
import com.mayo.community.dao.UserMapper;
import com.mayo.community.entity.DiscussPost;
import com.mayo.community.entity.User;
import com.mayo.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service //业务注解
//容器管理初始化和销毁的方法，一般为单例模式
//@Scope("prototype")//多实例方式
public class AlphaService {

    @Autowired//依赖注入，使Service依赖于AlphaDao
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

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

    //事务，
    // 声明式事务
    // REQUIRED:支持当前事务（外部事务），如果不存在则创建新事务
    // REQUIRES_NEW: 创建一个新的事务，并且暂停当前事务（外部事务）
    // NESTED: 如果当前存在事务（外部事务)，则嵌套在该事务中执行（独立的提交和回滚）
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        // 新增用户
        User user = new User();
        user.setUsername("meimei");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("meimei@qq.com");
        user.setCreateTime(new Date());
        user.setHeaderUrl(userMapper.selectById(155).getHeaderUrl());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("欢迎来到技术世界");
        post.setContent("欢迎欢迎，热烈欢迎");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";
    }
    
    //编程式事务
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("meimei");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("meimei@qq.com");
                user.setCreateTime(new Date());
                user.setHeaderUrl(userMapper.selectById(155).getHeaderUrl());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("欢迎来到技术世界");
                post.setContent("欢迎欢迎，热烈欢迎");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }
}
