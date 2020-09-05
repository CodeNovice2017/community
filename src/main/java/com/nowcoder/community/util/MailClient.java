package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


// 邮箱工具类,伪装为发送邮件客户端
// @Component代表交给Spring管理,且是一个通用的bean,表明在什么层次都可以用
@Component
public class MailClient {

    // 以当前类来命名,所以传入MailClient.class,getLogger是可以重载的,还有一个getLogger(String name)方法
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    // JavaMailSender也是由Spring容器来管理的
    // 源码实际是很简单的,最关键的就是构建好MimeMessage,然后send
    @Autowired
    private JavaMailSender javaMailSender;

    // 发送的目标是不同的,标题内容不同,但是发送人一般都是固定的,就是配置文件配置的username,所以把username注入到这个bean中来
    // 所以通过application.properties中的spring.mail.username这个key的值注入到String里
    @Value("${spring.mail.username}")
    private String from;

    // 公有的方法,无需返回值,只要不报错就是返回成功
    public void sendMail(String to,String subject,String content){
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            // 通过MimeMessageHelper来帮助我们建立配置MimeMessage
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            // setText是有重载的,第二参数设置为true可以允许html作为参数传入进来
            mimeMessageHelper.setText(content,true);
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
            e.printStackTrace();
        }

    }

}
