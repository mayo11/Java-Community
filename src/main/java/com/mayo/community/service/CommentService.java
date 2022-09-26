package com.mayo.community.service;

import com.mayo.community.dao.CommentMapper;
import com.mayo.community.entity.Comment;
import com.mayo.community.util.CommunityConstant;
import com.mayo.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
    // 添加评论
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //添加评论
        //过滤文本中的标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤文本中的敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 插入的行数
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }
}
