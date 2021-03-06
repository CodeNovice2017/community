package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    //写一个能够处理浏览器请求的方法
    public String sayHello(){
        return "Hello Spring Boot!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    //首先演示在Spring MVC框架之下如何获得请求对象,如何获得响应对象,这是偏底层的行为,先介绍底层,Spring MVC自带就简化也就是对这些麻烦的底层做的封装
    @RequestMapping("/http")
    //为什么这里不需要返回值了呢,不需要ResponseBody了呢?
    //因为我们直接可以通过response对象可以直接向浏览器输出任何数据,不依赖返回值了
    //如果想获取请求对象,响应对象那么就在这个方法中进行声明即可,声明了这两个类型以后,dispatcherServlet调用这个方法的时候就会自动的把那两个对象传给你,也就是说这两个对象dispatcherServlet在底层已经创建好了
    //Request对象有好几层接口,常用的接口是HttpServletRequest
    public void http(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        //处理请求就是处理请求中所包含的数据
        //获取请求数据
        System.out.println(httpServletRequest.getMethod());  //请求方式
        System.out.println(httpServletRequest.getServletPath()); //请求路径
        Enumeration<String> enumeration = httpServletRequest.getHeaderNames(); //请求的头部信息,若干行数据,用的很老的一种数据结构键值迭代器存放的
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = httpServletRequest.getHeader(name);
            System.out.println(name + " : " + value);
        }
        //获取业务数据,也就是请求体,也就是包含的参数
        System.out.println(httpServletRequest.getParameter("code"));

        //response是用来给浏览器返回响应数据的对象
        //返回响应数据(放回一个网页类型的文本,加上字符集)
        httpServletResponse.setContentType("text/html;charset=utf-8");
        //要想通过response向浏览器响应网页,那就需要输出流向浏览器输出,通过response对象获取到输出流,这个都是底层建好的,不用自己去创建
        //java7之后可以通过try加一个小括号,把writer在这个小括号里创建,编译的时候就会自动finally,关闭流,但是前提是writer必须要有close方法
        try(PrintWriter printWriter = httpServletResponse.getWriter();) {
            //但实际上网页很复杂,一级标题上面还需要有document标签,head标签等,但也底层都是这样一行一行输出的,这边只是演示一下
            printWriter.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //GET请求的两种传参方式,一种是?name=123,一种是/student/123,路径变量,要用不同的注解获得参数

    //现在用更简单的方式编写处理请求request和返回response的代码
    //GET请求的处理(默认请求)
    // students?current=1&limit=20  比如显示学生成绩,然后肯定要分页显示,一个还要限制显示20个,还要有当前是第几页
    // @RequestMapping可以和上面一样简化的声明,也可以使用参数等,明确限制只接受GET请求
    @RequestMapping(path = "/students",method= RequestMethod.GET)
    @ResponseBody
    //只要参数名和传入的参数名保持一致即可,dispatcherServlet检测到这个参数以后,会把request当中与这个方法匹配的参数直接赋值给它
    //也可以通过@RequestParam(name="current",required = false,defaultValue = "1")限制,如果限制了name属性,那么就不用一定要方法参数和请求参数同名
    public String getStudents(@RequestParam(name="current",required = false,defaultValue = "1") int cu, @RequestParam(name="limit",required = false,defaultValue = "10")int limit){
        System.out.println(cu);
        System.out.println(limit);
        return "some students";
    }

    // student/123 直接将参数编排到路径当中,成为路径的一部分,此时就不同于上面了
//    PathVariable就是路径变量,这个注解会从路径中得到这个变量然后赋值给方法参数
    @RequestMapping(path = "/student/{id}",method= RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }

    //POST请求一般是浏览器向服务器提交请求时用到的

    //这需要使用到静态的页面,至少有一个表单的提交
    //templates文件夹一般存放的是动态的页面,里面是需要model数据来替换的
    //static一般用于存放静态的页面,css,js等
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    //如何获取POST请求中的参数呢?直接声明参数,参数名和表单中数据的名字一致即可,也可以使用requestParam注解
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //如何向浏览器响应数据
    //如何向浏览器响应一个动态的html

    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    //要返回html就不要加@ResponseBody这个注解了,不加这个注解默认就是返回html
    //之前讲过Spring MVC的原理,实际上Controller响应返回给浏览器的就是由模板引擎渲染后由Controller,
    // DispatcherServlet会调用Controller的某个方法->这个方法需要依赖业务层和数据层,Controller将获取的数据组装为Model还有视图相关的数据
    // 然后将Model和视图数据提交给模板引擎,然后由模板引擎渲染,然后由Controller最终返还给dispatcherServlet,然后返回给浏览器
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        //就是模板里需要多少个变量,就add多少个数据
        modelAndView.addObject("name","张三");
        modelAndView.addObject("age",30);
        //还要向ModelAndView对象里面设置模板的路径和名字
        //模板就是放在templates目录下,这一级目录不需要写,下一级目录要写
        //同时因为Thymeleaf的模板就是html文件,就是固定的一个文件,所以不需要写后缀,就只写文件名即可,只要心里知道下面这个路径的view指的是view.html即可
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    //另一种更简单一些的响应写法
    //讲师更推荐这种方式写法 原因:更简洁一些
    //就是上面的方法其实更直观,是将Model和View都装在一个对象ModelAndView里面,而这个方法不是这么弄的,把View的视图直接返回,而Model的引用是通过Model是一个由Spring管理的bean,是通过dispatcherServlet自动实例化持有的
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    //view可以通过String的返回值来传递,model的传递要靠Model这个类型,Model这个类型也不是我们自己创建的,而是dispatcherServlet调用这个方法时,看到我们的参数有Model对象,他就会自动实例化这个对象传给我们
    //Model也是一个bean,是一个对象,dispatcherServlet是持有这个引用的,所以我们在方法内部向Model里面存数据,dispatcherServlet也是可以得到的
    public String getSchool(Model model){

        //return的是view的路径,就是返回的类型写成String,就代表返回的是view的路径
        model.addAttribute("name","北京大学");
        model.addAttribute("age","80");
        return "/demo/view";
    }

    //向浏览器响应JSON数据
    //通常是在异步请求当中
    //例如,我要注册B站,点击注册,注册过程要输入昵称密码,而输入昵称过程中,失去焦点后就会判断昵称是否被占用,而此时实际上浏览器请求了服务端,且服务端也响应了,但是页面并没有被刷新,当前网页不刷新,悄悄访问了服务器一次得到了一次响应,这也代表本次响应不是一个html

    //注意:当前我们写的代码都是Java代码,Java是面向对象的语言,所以打交道的都是一些Java对象,如果要把Java对象返回给浏览器,而浏览器解析对象用的是JS对象
    //浏览器肯定希望直接接受JS对象,Java对象肯定是无法直接转变为JS对象
    //JSON就可以实现两者的兼容.JSON本质上就是一个带有特定格式的字符串

    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    //如果要返回的响应是JSON的话,那么一定要加上ResponseBody,否则会认为返回的是html
    //dispatcherServlet调用这个方法的时候,一看加了@ResponseBody注解,并且声明返回的是这样的HashMap类型,他会自动转换java对象为JSON字符串
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",24);
        map.put("salary",8000.00);
        return map;
    }

    //还有有时候可能返回的不是一个员工,而是一组员工,也就是返回多个结构的情况
    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    //如果要返回的响应是JSON的话,那么一定要加上ResponseBody,否则会认为返回的是html
    //dispatcherServlet调用这个方法的时候,一看加了@ResponseBody注解,并且声明返回的是这样的HashMap类型,他会自动转换java对象为JSON字符串
    @ResponseBody
    public List<Map<String,Object>> getEmps(){

        List<Map<String,Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",24);
        map.put("salary",8000.00);
        list.add(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("name","李四");
        map1.put("age",24);
        map1.put("salary",8000.00);
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name","王二麻子");
        map2.put("age",24);
        map2.put("salary",8000.00);
        list.add(map2);

        return list;
    }


    // Cookie相关示例

    // 模拟浏览器访问服务器第一次请求,服务器创建Cookie实现

    @RequestMapping(path = "/cookie/set" ,method = RequestMethod.GET)
    @ResponseBody
    // 因为返回Cookie需要由Response头携带,所以需要一个Response对象作为参数
    // 测试使用就可以运行项目,访问这个页面,f12查看浏览器network中的set请求,response中就有相关的Set-Cookie
    // 然后还可以测试比如访问index,request header是没有cookie的,而访问有效的/alpha/cookie/get就能查看request header中有了cookie
    // 重新编译项目后也无需先访问set,直接get即可都是保存好的,不会因为重启项目就使cookie消失
    public String getCookie(HttpServletResponse response){

        // 创建cookie
        // 必须传入参数,没有无参构造,且一个Cookie对象只能传一组字符串
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置Cookie生效的范围,只在/community/alpha和其子路径有效
        cookie.setPath("/community/alpha");
        // 浏览器得到cookie会存,默认是存在浏览器的内存中,再次访问就没了,一旦设置生成时间,会放在硬盘中,超出生存时间才会失效
        // 十分钟
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);

        return "set cookie";
    }

    // 模拟浏览器如何携带Cookie访问浏览器
    @RequestMapping(path = "/cookie/get" ,method = RequestMethod.GET)
    @ResponseBody
    // 有了cookie怎么用,可以服务器要用,取里面的值,也可以模板用
    // 如何在服务端程序得到cookie,可以设置HTTPServletRequest对象参数,然后通过getCookies方法,但这种是直接从宿主获取,cookie数量很多的话还需要遍历从中去找
    // 那么如何获取某一个key的cookie呢? 通过一个注解可以实现@CookieValue("code"),代表从cookie中取key为code的值给这个参数
    public String getCookie(@CookieValue("code") String code){

        System.out.println("code = " + code);

        return "get Cookie";
    }
    
    // Session示例

    @RequestMapping(path = "/session/set" ,method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){

        // 在服务器端创建session不用手动创建
        // 服务器(Spring MVC)会自动帮我们创建session,帮我们注入进来,所以说session对象的使用和request model等对象的用法是一样的,只需要声明,Spring MVC就会自动注入
        // cookie的局限点还有cookie就只能存一些简短的字符串数据,因为一方面cookie在客户端存储,浏览器那边又无法识别代码,只能识别字符串
        // 首次访问这个路径后,response header内会有Set-Cookie: JSESSIONID=465DEFF53CB2CE88EC92F1CA0D0880DA; Path=/community; HttpOnly
        // 代表服务器给浏览器返回一个sessionid path是在整个项目都有效 未配置生存时间,所以默认存在内存里面,重启浏览器后消失
        session.setAttribute("id",1);
        session.setAttribute("name","TestSession");
        System.out.println(session);

        return "setSession";
    }
    @RequestMapping(path = "/session/get" ,method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {

        // 取值
        System.out.println("session = " + session);
        System.out.println(session.getAttribute("id"));

        return "getSession";
    }

    // AJAX示例
    // 请求的方式设置为POST,通常情况下,页面是要给服务器通过异步的方式提交一些数据,然后服务器保存之后,给浏览器返回提示
    // 因为是异步请求,我们不会再Controller给浏览器返回网页,返回的是字符串
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    // 希望浏览器给我提交一个name,提交一个age
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功!");
    }



}
