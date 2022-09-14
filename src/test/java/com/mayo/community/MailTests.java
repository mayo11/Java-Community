package com.mayo.community;

import com.mayo.community.dao.LoginTicketMapper;
import com.mayo.community.entity.LoginTicket;
import com.mayo.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
// 指定需要加载的配置文件，比如application-local.yml
//@ActiveProfiles()
// 修改启动环境
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testTextMail(){
        mailClient.sendMail("1301491773@qq.com", "Test", "First Email Test");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username" , "池昌熙");

        String contet = templateEngine.process("/mail/demo", context);
        System.out.println(contet);

        mailClient.sendMail("691477244@qq.com", "HTML", contet);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("avc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 *10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("avc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("avc", 1);
        loginTicket = loginTicketMapper.selectByTicket("avc");
        System.out.println(loginTicket);
    }
}
