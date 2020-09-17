package com.nowcoder.community;

import com.nowcoder.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class JSONTests {

    @Test
    public void jsonTest(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",25);
        System.out.println(CommunityUtil.getJSONString(0,"ok",map));
    }

}
