package com.mayo.community;

import com.mayo.community.dao.DiscussPostMapper;
import com.mayo.community.dao.MessageMapper;
import com.mayo.community.dao.UserMapper;
import com.mayo.community.entity.DiscussPost;
import com.mayo.community.entity.Message;
import com.mayo.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("meiao11");
        user.setSalt("xixi");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("Http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void UpdateUser(){
        int rows = userMapper.updateStatus(150,1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150,"Http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150,"meiaoxixi");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost post : list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void     testMessage(){
        List<Message> list = messageMapper.selectConversation(111, 0 , 20);
        for (Message message : list){
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0,10);
        for(Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);
    }
}
