package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("yt_999@163.com","迷糊","通过JavaMailSender发送给小迷糊!");
    }
    // 在MVC的DispatcherServlet的帮助下,我们可以很容易的在Controller配置模板,只要返回一个String路径即可,DispatcherServlet会自动帮我们调用模板
    // 但是在Test环境下我们需要主动去调用thymeleaf模板
    // 也不难,thymeleaf模板引擎有一个核心的类,这个类是被容器管理起来了,直接注入这个bean即可TemplateEngine

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","sunday");

        // 参数构建好,就调用模板引擎生成动态网页,经过process方法之后就可以生成一个动态网页,其实就是一个字符串
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("yt_999@163.com","迷糊",content);
    }
}
