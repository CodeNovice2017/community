package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    // 核心接口producer,两个方法createImage,createText,然后通过ctrl + n查找producer,然后通过箭头看到producer的一个具体实现类
    @Bean
    public Producer kaptchaProducer(){

        // Properties对象,实际上就是为了封装properties文件当中数据的,也可以在properties文件中读,当然也可以不再配置文件中写,就在这里实例化
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        // 指定随机几个字符拼在一起
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        // 指定要使用哪个干扰类(噪声类),就是在图片上加几条线,加几个点之类的,防止机器人暴力破解
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();

        // 用config对象储存配置
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
