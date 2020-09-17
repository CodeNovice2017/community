package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPostAndUserName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class MyMethodTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testDiscussPost(){
        DiscussPostAndUserName discussPostAndUserName = discussPostMapper.selectDiscussPostAndUserNameById(109);
        System.out.println(discussPostAndUserName);
    }

}
