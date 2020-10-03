package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // kafka角度:发送消息的方法
    // 业务角度:触发事件的方法(本质就是发一个消息)
    // 通常叫fireEvent触发事件
    // 外界调用事件触发的方法的话,那肯定要把事件传给我,谁调用谁传,因为调用方才知道要触发的是哪个事件
    public void fireEvent(Event event){

        // 将事件发布到指定的主题
        // 参数有两个一个是主题,这个事件对象中有,一个是一个字符串,字符串要包含事件对象所有的消息
        // 通过JSONObject.toJSONString(event),fastJSON工具,这样就发送了一个消息,不过这个消息的内容是一个JSON字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
