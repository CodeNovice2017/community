package com.nowcoder.community.util;

// 敏感词过滤器
// 为了复用方便,所以交给容器来管理

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 统一替换得常量,替换符
    private static final String REPLACEMENT = "***";

    // 第二步:初始化前缀树

    // 根节点
    private TreeNode rootNode = new TreeNode();

    // 初始化工具,就是首次访问到这个工具的时候,自动就把这个树初始化好,而且不需要初始化多次,只需一次即可
    // PostConstruct这个注解表明这是一个初始化方法,在容器实例化这个bean以后,在调用这个SensitiveFilter构造器之后,这个方法就会被自动的调用
    // Spring的这个bean,是在服务启动的时候被实例化的,所以在服务启动之后,这个方法就会被调用,树形结构结构好了,所以在init()方法里完成初始化树的编写是比较合理的
    @PostConstruct
    public void init(){
        // 有多种方法去读取sensitive-words.txt
        // this.getClass().getClassLoader()是我要获取类加载器,而类加载器是在类路径下加载资源
        // 类路径就是编译之后的classes,程序一旦编译,所有的代码包括配置文件都会在这个路径之下
        // sensitive-words.txt本来就在classes文件下,所以不用写路径,直接写文件名即可
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 从字节流读文字不太方便 最好转成字符流
                // 直接使用 new InputStreamReader(inputStream);也不是很方便,最好是使用一个缓冲流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ){
            String keyword;
            while((keyword = bufferedReader.readLine()) != null){

                // 第二步
                // 把敏感词添加到前缀树
                // 这里的逻辑复杂一些,所以最好封装在一个方法当中
                this.addKeyWord(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败:"+ e.getMessage());
        }
    }

    // 把一个敏感词添加到前缀树对象当中去
    // 内部使用
    private void addKeyWord(String keyword){
        TreeNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 把一个字符挂在当前节点下面
            TreeNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                // 初始化子节点
                subNode = new TreeNode();
                tempNode.addSubNode(c,subNode);
            }
            // 到此为止,我们要么找到了一个重复的子节点,要么新建了一个子节点,且子节点对应c字符

            // 下一层处理
            // 指针指向子节点,进入下一轮循环
            tempNode = subNode;

            // 当某一个字符串被循环结束之后,要在最后一个字符打一个标记
            //  设置结束标识
            if(i == (keyword.length() - 1)){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    // 第三步
    // 检索过程的实现
    // 检索前缀树
    // 传入的是可能含有敏感词的字符串,返回的是过滤之后替换好的字符串
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1指向树,默认指向根
        // 调用filter之前,整个前缀树已经通过init()方法创建好了
        // 这个前缀树也是整个项目的前缀树
        TreeNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 变长字符串
        // 结果记录
        StringBuilder stringBuilder = new StringBuilder();

        // 从头到尾的检测字符串,可以以指针2或者指针3,一般指针3会先结束,因为不是只循环一趟,指针三也依然是每次都会回来指针2同位置重新循环,所以使用指针三是效率会更高一些
        // 排除了指针2最后可能要多出一轮循环(讲师这么说)
        while(position < text.length()){
            char c = text.charAt(position);

            // 先不着急做敏感词的判断,先跳过符号
            // 防止上网的人聪明在穿插一些符号
            // 单独写一个小方法
            if(isSymbol(c)){
                // 若指针1处于根节点,就将此符号计入结果,让指针2向下走一步
                if(tempNode == rootNode){
                    stringBuilder.append(c);
                    begin++;
                }
                // 无论符号位于开头或中间,指针3都要向下走一步
                position++;
                continue;
            }
            // 不是符号
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                // 以begin为开头的字符串,不是敏感词
                stringBuilder.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if(tempNode.isKeyWordEnd()){
                // 发现了敏感词
                // 将begin到position这段的字符串替换掉
                stringBuilder.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            }else{
                // 是疑似敏感词,但不是敏感词的结尾,继续检查下一个字符
                position++;
            }
        }
        // 将指针3到终点,但是2还没有到终点的时候的字符串存入
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    // 判断符号方法
    private boolean isSymbol(Character c){
        // 这个方法判断这个字符是不是普通字符,如果普通字符返回true,(c<0x2E80 || c>0x9FFF)是东亚的文字范围,在范围内的话我们不认为是符号
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    // 开发三步
    // 定义前缀树
    // 因为其他估计用不到这个结构,所以设置为private
    // 定义节点
    private class TreeNode{

        // 关键词结束标识
        private boolean isKeyWordEnd = false;

        // 描述当前节点子节点
        // 一个节点的孩子可能是多个,所以用一个map来存,key是下级节点字符,value是子节点
        private Map<Character,TreeNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点方法,而且不能是直接生成了,不是简单的set了
        public void addSubNode(Character c,TreeNode node){
            subNodes.put(c,node);
        }

        // 获取子节点,通过key取value
        public TreeNode getSubNode(Character c){
            return subNodes.get(c);
        }

    }

}
