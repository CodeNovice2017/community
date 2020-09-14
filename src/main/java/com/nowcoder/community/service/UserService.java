package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    //因为注册过程中要发邮件,所以要注入邮件客户端,还有模版引擎
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    // 发邮件要生成激活码,激活码邮件要包含域名还有项目名
    // 域名配了key,项目名很早就server.servlet.context-path=/community配置了
    // 但是这两个只是value,而不是bean,需要用到${}表示我用表达式的方式取key的值,还要注意不要写错,因为这个字符串是不会检查是否正确的
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;

    // 根据UserId查询user的方法
    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    // 返回值可以设置为整数,表示返回不同的状态,也可以返回String,也可以返回自定义的类,这个方法返回各种错误信息,密码不能为空,账号不能为空等错误信息
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        // 判断空值
        // 如果用户对象为空,那么肯定是程序出现了问题了
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 如果用户名是空的,不能报错,账号是空的只是业务上的漏洞,但不是程序的错误,我们就把信息封装起来返回给客户端,告诉其这样是不对的
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 是否已存在业务逻辑判断
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已存在");
            return map;
        }

        // 如果上面全通过了,那么意味着确实用户可以成功注册了
        // 注册用户
        // 先随机生成一个字符串salt,用substring做一个五位的长度限制
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        // 普通用户
        user.setType(0);
        // 尚未激活状态
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        // 调用insert语句之后,user对象里面就有id了,mybatis自动的生成id的回填
        // 原因:因为我们配置了mybatis.configuration.useGeneratedKeys=true,使用自动生成id的机制,然后id的字段对应着哪个属性就会给那个属性自动的回填
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // 要求激活路径应该是这样的http://localhost:8080/community/activation/101/activateCode
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println(url);
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"牛客网注册激活邮件",content);

        // 最后返回的map为空就代表没有问题
        return map;
    }

    // 激活账号
    // 重复激活情况给提示,激活码伪造失败,成功激活三种情况,我们设计一个接口Interface存储常量,以便之后复用,然后直接让UserService实现这个类就行了
    public int activation(int userId,String code){

        // 先查到用户,然后看激活码对不对
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        } else{
            return ACTIVATION_FAILURE;
        }
    }

    // 登录也是用户的一种行为,所以直接写在User的业务层中也可以,也可以单独写一个Service
    public Map<String,Object> login(String username,String password,int expiredSeconds){

        Map<String, Object> map = new HashMap<>();

        // 空值判断
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        // 判断账号激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        // 可以放整个LoginTicket对象,也可以只放ticket
        // 其实login_ticket这张表就相当于一个session了,我们可以通过cookie的信息获取到ticket,进而获取到userId等信息
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    // 退出登录
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    // 获取用户ticket对应的信息,封装在LoginTicket对象返回
    public LoginTicket findLoginTicket(String ticket){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket;
    }

    // 更新用户headerUrl
    public int updateHeaderUrl(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

}
