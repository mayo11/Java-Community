package com.mayo.community.dao;

import com.mayo.community.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {

    User selectById(int id);//根据ID查询用户
    //根据姓名查用户
    User selectByName(String username);
    //根据邮箱查用户
    User selectByEmail(String email);
    //增加用户
    int insertUser(User user);
    //修改状态
    int updateStatus(int id, int status);
    //更新头像的路径
    int updateHeader(int id, String headerUrl);
    //更新密码
    int updatePassword(int id, String password);
}
