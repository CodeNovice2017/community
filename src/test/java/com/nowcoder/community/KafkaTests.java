package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {

    @Autowired
    private KafkaProducer kafkaProducer;

    // 测试无非就是用生产者发一个消息,然后看一下消费者能不能自动的收到这个消息,并消费掉
    // 通常,我们编写代码时,都会对生产者和消费者做一个各自的封装
    @Test
    public void testKafka(){
        // 当我运行这个测试方法的时候,我就去发送消息,发送消息之后,不让程序立刻结束,否则就看不到消费者消费这个消息的状态了
        // 解决办法:可以将主线程sleep,阻塞在这,不用太长时间,等个几秒,消费者收到消息,输出这个消息

        kafkaProducer.sendMessage("test","你好");
        kafkaProducer.sendMessage("test","在吗");

        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// 加上@Component,将这个bean交给Spring管理
@Component
class KafkaProducer {
    // 生产者发一个消息主要是依靠一个工具KafkaTemplate,这个工具已经被Spring整合了,本身就在容器中
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@Component
class KafkaConsumer {

    // 消费者不需要依赖kafkaTemplate,因为他是被动的
    // 服务启动后,Spring就会自动监听这个test主题,就是有一个消费者身份的线程阻塞在那里,一直试图读取test主题下的消息,如果没有消息就阻塞着,如果有消息立刻就读
    @KafkaListener(topics={"test"})
    public void handleMessage(ConsumerRecord consumerRecord){
        System.out.println(consumerRecord.value());
    }
}
