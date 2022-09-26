package com.mayo.community.dao;

import com.mayo.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    Comment selectCommentById(int id);

    int insertComment(Comment comment);
}
