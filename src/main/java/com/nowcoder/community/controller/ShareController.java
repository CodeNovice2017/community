package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    // 提供一个方法,前端访问这个请求,我就生成图片
    // 生成图片的时候那一定是异步的方式,生成图片的时间比较长,所以一定是用异步的方式,而且一般我们使用事件驱动
    // Controller就只把事件Event丢给Kafka,之后由Kafka的消费者异步实现即可

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    // 参数:分享的时候是哪个功能,传入功能
    // 如果是app的话,就是用户传入要分析的功能,app根据功能找到对应的模板的路径,然后通过路径去生成图片,这里我们没有什么功能,干脆就传路径就完事了
    public String share(String htmlUrl) {
        // 文件名要随机一下
        String fileName = CommunityUtil.generateUUID();

        // 异步方式构建长图,所以要Event
        Event event = new Event()
                // 这个事件比较特别,和点赞和关注不同,不需要传实体,那就所有需要的数据放入之前设置的Data里就行了
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                // 指定生成图片的后缀
                .setData("suffix",".png");
        eventProducer.fireEvent(event);

        // 异步的处理长图,这里需要给用户返回一个访问路径,告诉用户怎样去访问
        // 利用我们通用的页面JSON返回消息,0表示成功,msg为null不需要消息,把返回的访问路径放入map中
        Map<String,Object> map = new HashMap<>();
        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        // map.put("shareUrl", shareBucketUrl + "/" + fileName);
        return CommunityUtil.getJSONString(0,null,map);
    }

    // 获取长图
    // 逻辑类似于获取用户头像
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    // 因为这个方法就不是给浏览器返回一个模板,而是直接给浏览器返回一个图片,那么我们就使用HttpServletResponse直接处理响应给浏览器
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            // 通过response获得输出流
            OutputStream os = response.getOutputStream();
            // 一边读取文件,一边输出
            // 把文件转换为输入流
            FileInputStream fis = new FileInputStream(file);
            // 长度自定义
            byte[] buffer = new byte[1024];
            // 游标
            int b = 0;
            // 每次把输入流的文件信息读取到buffer里
            // fis.read(buffer)不等于-1的话就代表确实读到了数据,那就把信息放入输出流里
            // 可能最终一次可能读不到那么满了,那就不足1024字节,所以那就要设置一个b接收每次读了多少,限制了最后一次读入的多少
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }
    }
}
