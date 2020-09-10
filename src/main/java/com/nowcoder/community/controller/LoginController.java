package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    //注入application.properties中的一个固定的值,就要使用Value注解,这个并不是注入bean,当然也可以通过HttpServletRequest获取到
    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    // 也是得到一个登录页面而已
    // 这个方法的目的是是给浏览器返回一个html,而这个html里会保存一个图片的路径,浏览器会依据路径再次访问图片,我们会单独写一个请求,向浏览器放回图片,这个请求会在模板里引用它的路径
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    // 这里可以声明三个参数去接收那三个值(账号,密码,邮箱),也可以直接加一个User对象参数,只要传入值与user对象的属性相匹配,DispatcherServelet会自动进行注入
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            // 注册成功使用重定向方式调到首页去
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            // 注册失败时,把错误消息返还给发送页面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/activateCode这个路径可不能乱写了
    // 请求时get请求即可,激活的时候邮件并不是提供一个表单,只是利用路径携带了两个激活的条件,最后客户一方要求返回的是一个结果
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId,code);
        // 结果的含义依然不要手写0,1,2 也依然去实现那个接口,携带一定的含义的常量
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,该账号已经激活了!");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败,您提供的激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    // 方法返回值写void,因为像网页中返回的是一个特别的东西(图片),并不是String也不是html,所以需要自己用Response对象手动像浏览器输出
    // 另外,生成验证码之后,服务端需要把这个验证码记住,当登录的时候要检查输入的对不对,这个验证码是敏感信息,并不能存在浏览器端,而且要在多个请求之中用,要存在服务器端,一次请求是生成保存,以此请求要用这个数据,所以要用到session
    public void getKaptcha(HttpServletResponse httpServletResponse, HttpSession httpSession){

        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage bufferedImage = kaptchaProducer.createImage(text);

        // 将验证码存入session
        httpSession.setAttribute("kaptcha",text);

        // 将图片输出给浏览器
        // 首先声明给浏览器返回的是什么类型的数据
        httpServletResponse.setContentType("image/png");
//        response向浏览器做响应,我们需要获取它的输出流,输出内容
        // 这个流可以不用关闭,因为整个response是由springMVC管理的,所以会自动关,我们只管用即可
        try {
            OutputStream outputStream = httpServletResponse.getOutputStream();
            // 还有工具可用
            ImageIO.write(bufferedImage,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
            e.printStackTrace();
        }
    }

    // 请求方法不同,相同路径就不会冲突
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    // 我需要你用户在表单传入username,password,code,rememberMe
    // 还需要获取用户请求/login页面时,还会请求kaptcha生成验证码图片,我们将验证码信息保存在了session中,所以需要session
    // 登陆成功的话还需要给用户返回ticket,通过cookie传递,所以需要HttpServletResponse
    public String login(Model model,String username,String password,String code,boolean rememberme,
                        HttpSession session,HttpServletResponse response){

        // 检查验证码(这个是要在控制层判断的)
        // 如果验证码不对,账号密码不用看了
        // 方法返回的是Object类型,需要做强制转换
        String kaptcha = (String)session.getAttribute("kaptcha");
        // 两个都不能为空,也不能不相等
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        // 检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            // cookie的key value都必须要是字符串,所以要toString
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 让cookie整个项目下都会主动由浏览器发送
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}
