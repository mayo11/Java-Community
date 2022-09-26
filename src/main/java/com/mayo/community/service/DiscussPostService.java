package com.mayo.community.service;

import com.mayo.community.dao.DiscussPostMapper;
import com.mayo.community.entity.DiscussPost;
import com.mayo.community.util.SensitiveFilter;
import org.apache.tomcat.util.http.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId, int offset , int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset , limit);
    }

    public int findDiscussPostRows(int userId){
        return  discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义HTML标记，防止标签注入导致显示异常
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById (int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount (int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
}
