package com.mayo.community.controller;

import com.mayo.community.annotation.LoginRequired;
import com.mayo.community.entity.Event;
import com.mayo.community.entity.Page;
import com.mayo.community.entity.User;
import com.mayo.community.event.EventProducer;
import com.mayo.community.service.FollowService;
import com.mayo.community.service.UserService;
import com.mayo.community.util.CommunityConstant;
import com.mayo.community.util.CommunityUtil;
import com.mayo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    //关注
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType , entityId);

        // 是否已经关注
        boolean hasFollowed = followService.hasFollowed(user.getId(), entityType, entityId);

        if(hasFollowed) {
            // 触发关注事件
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
            // 详情页链接到该用户主页，无需传入其他实体Id
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0, "已关注！");
        }else {
            return CommunityUtil.getJSONString(0, "已取消关注！");
        }
    }

    //关注
//    @LoginRequired
//    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
//    @ResponseBody
//    public String unfollow(int entityType, int entityId){
//        User user = hostHolder.getUser();
//
//        followService.unfollow(user.getId(), entityType , entityId);
//
//        return CommunityUtil.getJSONString(0, "已取消关注！");
//    }

    // 关注列表
    @RequestMapping(path = "/followee/{userId}", method = RequestMethod.GET)
    public String getFollowee(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        User loginUser = hostHolder.getUser();
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followee/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowee(
                loginUser.getId(), userId, page.getOffset(), page.getLimit());
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    // 粉丝列表
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        User loginUser = hostHolder.getUser();
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String,Object>> userList = followService.findFollowers(
                loginUser.getId(), userId, page.getOffset(), page.getLimit());
        model.addAttribute("users", userList);

        return "/site/follower";
    }
}
