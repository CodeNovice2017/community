package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    // 日志,处理事件,其实就是给某个人发送一条消息,那发送消息实际就是向message表中插入一条数据
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    // 我们可以写一个方法消费一个主题,也可以一个方法消费多个主题,就通过@KafkaListener(topics={"test"})来指定一个或多个主题
    // 在我们的项目中,因为系统消息的发布,就是在消息页面通知某某某点赞了,某某关注了,这些消息的逻辑比较相似,所以采用整合一个方法
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    // 选一个代表上面三个主题来命名方法
    public void handleCommentMessage(ConsumerRecord consumerRecord){
        if(consumerRecord == null || consumerRecord.value() == null){
            logger.error("消息的内容为空!");
            return;
        }
        // 将消息的内容(JSON格式的字符串)恢复为一个Event事件对象
        Event event = JSONObject.parseObject(consumerRecord.value().toString(),Event.class);
        if(event == null){
            // 如果有值,但是还原不回来,那么就是格式不对
            logger.error("消息格式错误!");
            return;
        }
        // 如果内容也没问题,格式也对了,那就可以利用数据去发送站内通知,站内通知实际就是发站内信,站内信就是构造一个message数据插入message表中
        // message表之前已经使用过用于发送私信,conversation_id是111_112的形式,是两个用户之间发
        // 现在是conversation_id是comment评论,follow关注或者like点赞,单指系统发给用户,并假设from_id为1,
        // 然后content存的是将来页面上为了拼出用户XXX评论/点赞/关注了你的XXX这个字符串所需要的条件,是一个JSON字符串
        // 也就是message存了两类数据,一个是人与人的私信,一个是系统发送的通知

        // 发送站内通知
        Message message = new Message();
        // 系统用户规定为1,user表中对应的第一个数据,也被设置为系统用户,不会被用户注册拥有这个id
        message.setFromId(SYSTEM_USERID);
        // 张三给李四的帖子点赞,关注等,都应该是给传入作者的id,entityUserId
        message.setToId(event.getEntityUserId());
        // 复用Message这张表,conversationId就是存的comment/follow/like等主题topic
        message.setConversationId(event.getTopic());
        // 默认状态就是0 状态0未读,状态1已读,状态2删除
//        message.setStatus();
        message.setCreateTime(new Date());

        // content就是页面上我要拼的那条数据JSON字符串的条件
        // 包含这件事是谁触发,对于哪个实体做了哪些操作,能连接到帖子实体的id
        Map<String,Object> content = new HashMap<>();
        // 触发事件的人
        content.put("userId",event.getUserId());
        // 实体的类型
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        // 注意这里不要乱,我们这个事件是事件触发的时候封装了相关的数据,
        // 那么事件的消费者,在消费这个事件的时候,得到的原始数据,最终需要把这个数据转换为一个message
        // message里面包含了一些基础数据和内容,内容是要拼出用户XXX点赞了XXX之类的话
        // 所以这里只是进行了数据的转化

        // 然后,event里在触发事件的时候可能还会有一些其他的数据,这些数据不方便存在message的其他字段,也不合适,所以这些数据统统的存入content中
        if(!event.getData().isEmpty()){
            // 如果不为空,我就要把event data属性(map类型)里所有数据拿出来存入上面那个content中,也就是和进去一起存入message表的content字段里
            // 遍历event.getData()这个map,遍历map,每次遍历得到一个entry
            // 遍历的是一个key,value的集合,每次遍历得到一个key,value,每次把这个key,value存入上面的content中
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        // 要弄懂这里为什么不用之前扩展写的CommunityUtil的getJSONString方法,
        // 因为扩展FastJSON是因为业务的需求,当用到getJSONString方法时,
        // 都是应该是在构造一个包含code,msg,data的一个JSON,这一般是我们用于异步返回给浏览器的数据
        // 而此处,我们并不是在构造这样给浏览器的数据,只是业务需要将content转换为JSON存到message表中,所以并不需要code,msg
        message.setContent(JSONObject.toJSONString(content));
        messageService.addLetter(message);

        // 到此为止,终于把消费者消费的方法写完了,这个方法消费的就是三个主题中的数据,
        // 消费的逻辑都类似,就是发送一条消息给用户(之后要做的也就是通过之前写的message的Service,Controller将系统消息的列表显示出来),消息构造的方式也一样
        // 也就是说实际上我们并不是通过kafka来直接给用户发送消息,发送消息,显示消息的逻辑依然是由Controller和Service加上模板引擎完成的
        // kafka在这个业务中的角色,就是充当中间的一个消息事件生产和如何消费事件的角色,注意这里的消息事件是理解为一个事件,这个事件才是由kafka来生产消费的
        // 而最后用户显示的系统消息依然是要用模板引擎等技术实现的
    }
}
