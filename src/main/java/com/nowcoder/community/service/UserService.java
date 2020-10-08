package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    //因为注册过程中要发邮件,所以要注入邮件客户端,还有模版引擎
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 发邮件要生成激活码,激活码邮件要包含域名还有项目名
    // 域名配了key,项目名很早就server.servlet.context-path=/community配置了
    // 但是这两个只是value,而不是bean,需要用到${}表示我用表达式的方式取key的值,还要注意不要写错,因为这个字符串是不会检查是否正确的
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;

    // 根据UserId查询user的方法
    public User findUserById(int id){
//        return userMapper.selectById(id);

        // 重构之后应该首先先从cache中查
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
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
    // 修改了用户状态,所以需要吧Redis的用户缓存清理掉
    public int activation(int userId,String code){

        // 先查到用户,然后看激活码对不对
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);

            // 重构
            clearCache(userId);

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
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 我们还是使用字符串来存,那么问题来了,这不是一个对象么,我们可以把他序列化为一个JSON字符串即可
        // redis会自动把这个对象序列化一个JSON字符串保存
        // 同时这个ticket是持久化在Redis中的,没有过期时间
        // 用户换一个客户端登录就会生成一个新的唯一的LoginTicket,但无论用户在哪里登录,登录成功后浏览器会存一个Cookie
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        // 可以放整个LoginTicket对象,也可以只放ticket
        // 其实login_ticket这张表就相当于一个session了,我们可以通过cookie的信息获取到ticket,进而获取到userId等信息
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    // 退出登录
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 有了Key之后,我们要先把LoginTicket取出来,把这个对象的状态修改完之后,再存回去
        // 用强制类型转换
        LoginTicket loginTicket = (LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        // 把更新了的值覆盖了原有的值
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    // 获取用户ticket对应的信息,封装在LoginTicket对象返回
    public LoginTicket findLoginTicket(String ticket){
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }

    // 更新用户headerUrl
    public int updateHeaderUrl(int userId,String headerUrl){

        // 重构
        // 直接在return前面加一个clearCache(userId);不太好,因为无法保证MySQL事务和Redis事务在一个范围之内
        // 就是怕clear执行成功,但是下面updateHeader执行失败
        // 还是应该先更新,在删缓存
//        clearCache(userId);
//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    // 修改用户密码
    public Map<String,Object> updateUserPassword(User user,String oldPassword,String newPassword,String ensurePassword){

        Map<String,Object> map = new HashMap<>();
        String oldRealPassword = user.getPassword();
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码不能为空,请重新输入!");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空,请重新输入!");
            return map;
        }
        if(StringUtils.isBlank(ensurePassword)){
            map.put("passwordMsg","确认密码不能为空,请重新输入!");
            return map;
        }
        if(!ensurePassword.equals(newPassword)){
            map.put("newPasswordMsg","确认密码和新密码不一致,请重新输入!");
            return map;
        }
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!oldRealPassword.equals(oldPassword)){
            map.put("passwordMsg","原密码错误,请重新输入!");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if(oldRealPassword.equals(newPassword)){
            map.put("newPasswordMsg","新密码不能和原密码相同,请重新输入!");
            return map;
        }
        userMapper.updatePassword(user.getId(),newPassword);
        clearCache(user.getId());
        return map;
    }

    public User findUserByUsername(String username){
        return userMapper.selectByName(username);
    }

    // 用户信息缓存重构Redis,3个方法
    // 1. 优先从缓存中取值
    // 给UserService内部调用
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 还是直接存User对象,Redis会自动序列化为JSON字符串
        return (User)redisTemplate.opsForValue().get(redisKey);
    }
    // 2. 取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 记得别忘了设置一个小时的缓存时间
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    // 3. 当数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 加一段逻辑,将用户的权限存入Spring Security的SecurityContext中
    // 那么首先用户查到以后有什么权限,根据用户Id获取用户权限的方法
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
