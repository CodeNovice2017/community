package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

// 提供一些简单的静态方法,不交给容器托管了
public class CommunityUtil {

    // 生成随机字符串
    // 提供上传文件功能,生成头像等文件的名字
    public static String generateUUID(){
        // 直接调用java自带的工具UUID,但是替换所有的-为空字符串
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // md5密码加密,md5加密特点:只能加密,不能解密
    // 比如hello加密 -> abc123456dnf  每次加密都是这个值,但是无法解密,看起来好像挺安全,容易被暴力破解(简单密码库)
    // 所以用到了User表的salt相加起来之后再加密
    // key作为这个password + salt
    public static String md5(String key){
        // Spring有这个工具,但是先做一个判断空
        // 通过导入的第三方包org.apache.commons.lang3来进行判空,会判定key为null,为空串,为空格都是空的
        if(StringUtils.isBlank(key)){
            return null;
        }
        // 把传入的结果加密成16进制的字符串
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
