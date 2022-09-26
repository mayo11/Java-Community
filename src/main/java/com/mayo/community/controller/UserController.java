package com.mayo.community.controller;

import com.mayo.community.annotation.LoginRequired;
import com.mayo.community.entity.User;
import com.mayo.community.service.FollowService;
import com.mayo.community.service.LikeService;
import com.mayo.community.service.UserService;
import com.mayo.community.util.CommunityConstant;
import com.mayo.community.util.CommunityUtil;
import com.mayo.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path="/setting" , method= RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    //上传图片
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器内部发生异常！" , e);
        }

        //更新当前用户的头像路径（web访问路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;

        userService.updateHeader(user.getId() , headerUrl);
        return "redirect:/index";
    }

    //读取并渲染图片
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //响应图片
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取用户头像失败：" + e.getMessage());
        }
    }

    //修改密码
    @RequestMapping(path = "/update", method = RequestMethod.GET)
    public String updatePassword(@RequestParam("oldPassword")  String oldPassword ,
                                 @RequestParam("newPassword")  String newPassword,
                                 @RequestParam("confirmPassword")  String confirmPassword,Model model){
        //输入是否为空
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg", "原密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg", "新密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)){
            model.addAttribute("confirmPasswordMsg", "确认密码不能为空");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        //原密码是否正确
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            model.addAttribute("oldPasswordMsg","原密码错误，请重新输入");
            return "/site/setting";
        }
        //判断新密码是否相等
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmPasswordMsg", "两次输入密码不一致，请重新输入！");
            return "/site/setting";
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/index";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        User hostUser = hostHolder.getUser();
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId , ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER , userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if(hostUser != null) {
            hasFollowed = followService.hasFollowed(hostUser.getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
