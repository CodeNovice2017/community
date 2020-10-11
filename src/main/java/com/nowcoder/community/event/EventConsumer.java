package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {

    // 日志,处理事件,其实就是给某个人发送一条消息,那发送消息实际就是向message表中插入一条数据
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    // 导入一个可以执行定时任务的线程池
    // 但是之前讲过了,一般这样的定时任务的线程池都是通过Quartz去执行,使用Quartz执行的原因,是考虑分布式部署的问题
    // 但是这里为什么用Spring自带的线程池呢? 为什么可以用呢?
    // 在消费分享事件的代码解释
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;


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

    // 增加一个新的主题的处理方式
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord consumerRecord){
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

        // 我们只要从消息事件中得到帖子id,查到对应的帖子,然后存入ES服务器即可
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    // 消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShare(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        String htmlUrl = (String)event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        // 拼接命令
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        // 现在代码一执行,图片就生成了,但是怎么传上七牛云呢?
        // 不可以直接传的,因为如果直接传Runtime.getRuntime().exec(cmd);这句代码实际是比logger.info("生成长图成功: " + cmd);先执行完的
        // 因为生成图片是比较耗时的过程,依赖于图片的大小和处理器的速度,这个命令假设500ms以后执行结束
        // 但是当前方法的主线程会继续执行,也就是说 Runtime.getRuntime().exec(cmd);后面的逻辑比它执行完还早,所以在这里如果想把图片上传到七牛云
        // 图片还未生成呢,不可能传入的
        // 如何解决这个问题呢?我们也不可能硬把程序阻塞在这里
        // 我们可以在这里认为的启动一个定时器,每隔半秒看一眼生成与否,如果一直不生成,我等了有30秒,那我就取消生成,我不上传了
        // 我们这里用到定时器主要是等这个生成命令的完成
        // 那么我们这里是可以用Scheduler,因为我们这个逻辑并不是每一个服务器都会执行
        // 虽然每一台服务器都部署了同一个Consumer,但是消费者消费Kafka消息队列的事件时有一个抢占的机制
        // 比如我有5台服务器,5台服务器部署了5个Consumer,5台服务器只有一个服务器能抢占到这个事件去处理
        // 所以说这个方法只有某一个服务器会执行,其他服务器是不会执行的,所以说我在这一个服务器上启动一个定时器,和其他服务器不会产生关联和影响
        // 之前的定时任务是服务器一启动就一直在那里运行了,所以服务启动就是五个服务器都在运行,而这里不是,这里是谁抢到这个事件消息,谁才会启动定时器

        // 启用定时器,监视该图片,一旦图片生成了,则上传至七牛云
        UploadTask task = new UploadTask(fileName,suffix);
        // 每隔500ms,尝试上传一次
        // 然后这个定时器还要考虑什么时候停止,只是这么写的话就会永远不停止
        // 定时器停止肯定不是在这里停止,在这里只是触发定时器的执行,肯定是在run方法之内某个条件达成才停止
        // 如何停止定时器呢?
        // 通过Future,Future里封装了任务的状态,Future还可以停止定时器
        // 但是这个Future是threadPoolTaskScheduler.scheduleAtFixedRate(task,500)调用以后才返回的
        // 那么这个Future怎么给Runnable用呢?
        // 我们可以在下面线程体任务的类中加一个set方法,要求实例化完之后,把Future传进来

        // 当给UploadTask填了属性future以后,然后添加了setFuture方法以后,我就是先实例化了Task,然后启用了定时器,
        // 然后因为下面这句话执行是需要500ms的最开始的执行间隔的,所以应该是下面这句话后面的代码先执行,然后到了500ms它才执行
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task,500);
        // 那我就赶紧把这个future传给任务,那么run方法执行的过程中就可以通过Future停止这个定时器了
        task.setFuture(future);
        // 某些极端的情况下,命令执行失败,图片一直没生成
        // 还有有可能图片生成了,但是服务器传给七牛云服务器失败了,可能恰好网络中断了,万一这种情况,也不行
        // 所以一定要考虑兜底的方案
    }

    // 线程体
    // 虽然可以做匿名的实现,就是通过 new Runnable(){ 覆写run方法 }的形式,但是当这个线程体比较复杂最好是单独写一个
    class UploadTask implements Runnable{

        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;

        // 启动任务的返回值,他可以用于停止定时器

        private Future future;

        // 开始时间
        // 在实例化这个task的时候,记录一下开始时间
        // 任务执行的时候,我再得到当前时间,减掉开始时间算一下任务跑了多久,一旦任务跑了超过30秒,就认为出了问题,那就强制关闭
        private long startTime;

        // 上传次数
        // 往往见分晓的成功或者失败,往往最开始的几次上传就能知道了,但是这个时间还不容易界定,所以用次数也做一个兜底,即使比如三次上传都失败了发生在5秒之内,那我也取消了
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        // 生成一个有参的构造器,强制必须传入两个参数
        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            // 先判断终止的条件
            // 生成失败
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("任务执行时间过长,终止任务:" + fileName);
                // 停止定时器任务
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3) {
                logger.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            // 从本地目录里找到文件
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                // 这里和表单不一样,表单提交用户头像是通过JS指定上传的地址操作的,这里是通过Java指定的
                // Zone.zone1()就是华北z1机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                try {
                    // 开始上传图片
                    // 七牛云的返回信息是Response类型
                    // manager.put()方法传递图片
                    // 参数依次:本地文件路径,文件名,上传凭证,null,上传文件类型,false
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
