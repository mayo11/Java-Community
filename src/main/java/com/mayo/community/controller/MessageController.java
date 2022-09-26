package com.mayo.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.mayo.community.entity.Message;
import com.mayo.community.entity.Page;
import com.mayo.community.entity.User;
import com.mayo.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list" , method = RequestMethod.GET)
    public String getLetterList (Model model, Page page){
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");

        // 会话列表
        List<Message> conversationList = messageService.findConversation(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVo = new ArrayList<>();
        if(conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findUnreadLetterCount(user.getId(), message.getConversationId()));
                // 目标用户
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversationVo.add(map);
            }
        }
        model.addAttribute("conversations", conversationVo);

        //查询未读消息数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    // 私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}" , method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letterVo = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letterVo.add(map);
            }
        }
        model.addAttribute("letters", letterVo);

        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getMessageUnreadIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getMessageUnreadIds(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();

        if(messageList != null){
            for(Message message : messageList){
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){

        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1, "私信用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    // 系统通知列表
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        // 查询评论类通知
        Map<String, Object> commentMessageVO = getNoticeVO(TOPIC_COMMENT);
        model.addAttribute("commentNotice", commentMessageVO);

        // 查询点赞类通知
        Map<String, Object> likeMessageVO = getNoticeVO(TOPIC_LIKE);
        model.addAttribute("likeNotice", likeMessageVO);
        // 查询关注类通知
        Map<String, Object> followMessageVO = getNoticeVO(TOPIC_FOLLOW);
        model.addAttribute("followNotice", followMessageVO);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    // 系统通知详情
    @RequestMapping(path = "/notice/detail/{topic}" , method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model){
        User user= hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        // 通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVOList = new ArrayList<>();
        if(noticeList != null){
            for(Message notice : noticeList){
                Map<String,Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 通知内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                if(topic != TOPIC_FOLLOW){
                    map.put("postId", data.get("postId"));
                }
                noticeVOList.add(map);
            }
        }
        model.addAttribute("notices", noticeVOList);


        // 设置已读
        List<Integer> ids = getMessageUnreadIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

    private Map<String,Object> getNoticeVO(String topic){
        User user = hostHolder.getUser();

        Message message = messageService.findLatestNotice(user.getId(), topic);
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("message", message);
        if(message != null){
            // 将通知内容由JSON字符串转为Map对象
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            if(topic != TOPIC_FOLLOW) {
                messageVO.put("postId", data.get("postId"));
            }
            // 通知总数量
            int count = messageService.findNoticeCount(user.getId(), topic);
            messageVO.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), topic);
            messageVO.put("unreadCount", unreadCount);
        }
        return messageVO;
    }


}
