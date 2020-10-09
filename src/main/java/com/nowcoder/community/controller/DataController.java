package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 统计页面
    // 访问统计页面
    // 为什么method声明了两种请求方式呢?也就是说这个方法既可以处理GET请求,也可以处理POST请求
    // 这就是为了转发而设置的,这样这个请求就可以处理两种请求的方式
    // 因为当下面的getUV()方法转发的时候,方法是从getUV跳到getDataPage,但是,请求依然还是getUV()方法处理的POST请求,转发是在一个请求内完成的
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    // 页面上传过来的时候是一个日期的字符串,日期的字符串是如何转为Date的呢?
    // 默认下是不容易转的,因为服务器不知道传入的字符串是什么格式
    // 所以要告诉服务器传入的格式
    // 通过注解@DateTimeFormat(pattern = "yyyy-MM-dd")即可,就免去了在Controller方法内做String->Date的转换
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        // 如果不使用下面这种返回,而是使用return "/site/admin/data"返回一个模板
        // 这个模板是返回给DispatcherServlet,DispatcherServlet得到这个模板再去做处理
        // forward是声明我当前这个方法只能把整个请求处理一半,还需要另外一个方法继续处理请求,另外一个方法也是和他平级的方法,也是处理请求的方法,而不是模板
        // 那么下面这样写其实就是转发到getDataPage()这个处理请求的方法,然后getDataPage()中返回了一个模板,其实根本上还是返回模板
        // 只不过通过这样转发的方式来学习一下forward,而假设getDataPage()中还有其他逻辑的话,那么这段逻辑还可以复用,所以这样写是有一定好处的
        return "forward:/data";
    }

    // 统计活跃用户
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data";
    }
}
