[TOC]

# 牛客社区项目

## 1 项目解析

### 1.1 技术架构

- Spring Boot
- Spring(管理对象,对其他项目进行整合) Spring MVC(解决如何处理浏览器的请求) MyBatis(服务端如何访问数据库,Spring整合,让它变得简单)
- Redis(NoSQL数据库,默认将数据存放在内存中) Kafka(目前性能较好的消息队列服务器) ElasticSearch(最流行的搜索引擎),**面试重点话题,都是在某个层面提高应用的性能的技术**
- Spring Security(权限管理) Spring Actuator(对系统全面监控,让运维人员全面了解服务器运行状况)

#### 消息队列服务器

- 因为网站每天都会给用户发送大量的消息,用户之间的评论点赞,系统都要通知用户,这就要求使用专用的消息队列服务器
- 需要学习多线程方面的知识,需要理解消费者与生产者模式

#### 全站搜索(搜索引擎)

- 将搜索结果做高亮显示,并不是在数据库中查询一下就结束了,需要通过专业的搜索引擎实现
- 数据库和搜索引擎之间如何同步数据,是如何支持分词,高亮显示都是要讨论的问题

#### 网站UV,活跃用户统计

- 如何提高统计的效率,节省内存的开销

### 1.2 开发环境搭建

#### Maven

- 核心概念,仓库

- 创建项目骨架,编译,清除等

- **如何引入包**,可以上`mvnreposirory.com`上下载,比如搜索MySQL,进入后查看不同版本的使用次数,然后复制下面的字段,复制到项目pom.xml的dependencies下

- ```xml
  <!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.13</version>
  </dependency>
  ```

- 但是这样来一个一个导入和管理还是麻烦

#### Spring  Initializr

- `https://start.spring.io/`
- 带有snapshot或者其他标识的是非正式的版本
- ![image-20200901153831389](https://raw.githubusercontent.com/CodeNovice2017/ImageRepository/master/img/20200901153831.png)

- SpringBoot比较神奇,一般web开发都是war包,但是SpringBoot也可以使用jar包
- 添加依赖,Aspect,Web,Thymeleaf,DevTools(改了代码,重新启动服务器,安装这个会自动重启)
- 然后generate,下载到本地后,然后直接用IDEA打开即可
- **错误**,此时直接跑项目可能会出现`org.apache.catalina.LifecycleException: Failed to start component [StandardEngine[Tomcat]`的报错,原因未知,**解决办法**可以先关闭一次IDEA重新启动即可
- SpringBoot会将tomcat应用服务器以jar包的形式内置在了项目中,这样我们就不需要单独安装tomcat了,可以在External Libraries看到

#### Spring boot优势

- **起步依赖**,在pom文件中有很多依赖,同时这些不是单个的依赖,不是单个的包,而是一堆包,在IDEA通过Ctrl点击`spring-boot-starter-thymeleaf`进去可以看见这个集合了什么依赖(集合的依赖还有可能有starter的起步依赖,逐层向下)

- ```xml
  <dependencies>
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-thymeleaf</artifactId>
  		</dependency>
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-web</artifactId>
  		</dependency>
  
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-devtools</artifactId>
  			<scope>runtime</scope>
  			<optional>true</optional>
  		</dependency>
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-test</artifactId>
  			<scope>test</scope>
  		</dependency>
  	</dependencies>
  ```

- **自动配置**,无需Spring那样创建项目配置那么多

- **端点监控**,项目运行状况的监视(项目上线后)

#### 启动一个简单的Spring Boot项目

- com.nowcoder.community下创建controller包,然后创建一个类,简单写以下代码

- ```java
  package com.nowcoder.community.controller;
  
  import org.springframework.stereotype.Controller;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.ResponseBody;
  
  @Controller
  @RequestMapping("/alpha")
  public class AlphaController {
  
      @RequestMapping("/hello")
      @ResponseBody
      //写一个能够处理浏览器请求的方法
      public String sayHello(){
          return "Hello Spring Boot!";
      }
  }
  ```

- 然后重新编译运行项目,即可通过`http://localhost:8080/alpha/hello`访问这个页面

- Controller和ResponseBody都是SpringMVC的注解

- 我理解的流程是,通过Controller告诉Spring这是一个控制类(也就是控制页面),这个类的映射到/alpha访问,然后类里面提供了一个方法,这个方法也映射为/hello访问,也就是说我们需要通过`http://localhost:8080/alpha/hello`才能访问到这个方法,然后如果直接这样写不加ResponseBody注解的话会报错`Error resolving template [Hello Spring Boot!], template might not exist or might not be accessible by any of the configured Template Resolvers`,ResponseBody的作用应该就是在当前页面添加这句话,而不是生成一个新的页面

- 然后我们可以通过`application.properties`文件来配置项目,**约定大于俗成**就是这个意思,比如通过配置下面两句配置了内嵌tomcat启动的端口号和给项目取一个访问路径,这样接下来就要通过``http://localhost:8080/community/alpha/hello``来访问

- ```properties
  server.port=8080
  server.servlet.context-path=/community
  ```

### 1.3 Spring入门

#### Spring全家桶

- Spring Framework
- Spring Boot
- Spring Cloud 微服务(本次项目就是牛客网的一个模块,没有必要向下拆分,所以没有使用微服务)
- Spring Cloud Data Flow 利用Spring做数据集成

#### Spring Framework

- Spring Core `IoC AOP(Spring管理的对象一般称为bean)`
- Spring Data Access `Transactions Spring Mybatis`
- Web Servlet `Spring MVC`
- Integration `Email Scheduling AMQP Security`

##### Spring IoC

- IoC是一种面向对象编程的设计思想,是另一种管理对象的方式,有助于代码解耦,Dependency Injection是它的实现方式,IoC容器是依赖注入的关键,本质上是一个工厂

#### Spring入门解析

- ==Repository Controller Service 这些注解都是由Component来实现的,所以一共有这四种注解能够使类被Spring扫描作为bean==

##### 如何在测试环境下测试学习Spring

- ```java
  //通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
  @ContextConfiguration(classes = CommunityApplication.class)
  ```

- **然后建立一个dao包,Data Access Object数据访问对象,这里存放的是用于访问数据库的bean**

- ```java
  @Test
   	public void testApplicationContext(){
   		System.out.println(applicationContext);
   
   		//从容器获取扫描到的bean,使用接口或者实现类都可以,如果填写AlphaDao.class,就会按照实现接口的类扫描bean,如果在AlphaDaoHibernate和AlphaDaoMybatis都仅使用了@Repository注解
   		//那么就会报错org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'com.nowcoder.community.dao.AlphaDao' available: expected single matching bean but found 2: alphaDaoHibernateImpl,alphaDaoMybatisImpl
   		//提示不确定到底哪个bean才是被指出的
   		//这样的话,有两个解决方法,要么直接将参数改为AlphaDaoMybatisImpl.class,要么直接在某一个注解上添加@Primary
   		//这就显示出了Spring IoC的优势,控制反转,也就是我这边真正的逻辑代码根本不用动,我有业务拓展需求,比如将Hibernate替换为Mybatis技术栈,我并不需要修改本java文件的代码,只要让Mybatis重写一个AlphaDao的实现类,然后给Mybatis设置一个@Primary注解即可
   		// 因为我们这个文件依赖的是接口AlphaDao,而不是具体的实现类,调用方和实现类之间没有什么关系,这也就是依赖注入的实现
   		// 但这样会带来新的问题,比如我仍需要Hibernate在一部分模块工作,那么一方面可以直接使用alphaDao = applicationContext.getBean(AlphaDaoHibernateImpl.class);
   		// 另一方面可以使用@Repository("alphaHibernate")直接给予bean一个名字,那么就可以使用alphaDao = applicationContext.getBean("alphaHibernate",AlphaDao.class);这种方法利用getBean的重载指明了是AlphaDao.class类型,还可以使用强制类型转换(AlphaDao)applicationContext.getBean("alphaHibernate")
   		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
   		System.out.println(alphaDao.select());
  //		alphaDao = applicationContext.getBean(AlphaDaoHibernateImpl.class);
  		alphaDao = applicationContext.getBean("alphaHibernate",AlphaDao.class);
  		System.out.println(alphaDao.select());
  	}
  }
  ```

##### ==面试题目==**SpringIoC的好处,控制反转到底是什么含义**

- Spring管理bean的作用域

- 更多的Spring学习测试

  - ```java
    //测试一下bean的管理方式
    	//测试结果,bean在Spring管理的容器内默认都是单例的,也就是在容器内只有一个实例
    	//想要非单例模式,那么可以在AlphaService中加入注解 @Scope()设定,默认参数是singleton,@Scope("prototype")设置后,每次访问bean都会创建一个新的实例
    	//通常情况下,都是单例模式
    	@Test
    	public void testBeanManagement(){
    		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
    		System.out.println(alphaService);
    		alphaService = applicationContext.getBean(AlphaService.class);
    		System.out.println(alphaService);
    	}
    
    	//有时候我们希望导入一些第三方的jar包的类作为bean,但是肯定不能像现在这样在人家的代码上加@Service注解了,因为那是人家的代码,而且打成jar包后也未必看得到源码了
    	//这就需要我们自己写配置类,以后配置类都在package config下
    	@Test
    	public void testBeanConfig(){
    		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
    		System.out.println(simpleDateFormat.format(new Date()));
    	}
    
    	//在实际上,我们并不是主动这样获取容器,然后getBean去使用,这是底层的一些学习,而实际上我们是使用依赖注入,也就是怎么简单怎么来使用
    	//比如当前的bean要使用alphaDao,我没有必要容器去getBean,我只用声明我将要给当前的bean注入alphaDao即可
    	//@Autowired就是 我希望Spring容器可以把AlphaDao注入这个成员变量中
    	@Autowired
    	private AlphaDao alphaDao;
    	//而如果我们想要继续使用Hibernate,那么可以额外加一个注解@Qualifier("alphaHibernate")
    	//同时,这是最体现Spring降低耦合的例子,就是多态,当前的bean依赖的是接口,底层的实现是不和这个上层代码耦合的,降低了耦合度,而且不需要实例化
    	//@Autowired还可以用在构造器之前,还可以放在setter方法之前都可以,但基本都是直接放在属性(成员变量)前面即可
    
    	@Autowired
    	private AlphaService alphaService;
    
    	@Autowired
    	private SimpleDateFormat simpleDateFormat;
    
    	//而且测试表明这个通过成员变量获取的alphaDao实例和上面通过容器getBean获得的alphaDao的是同一个实例
    	@Test
    	public void testDI(){
    		System.out.println(alphaDao);
    		System.out.println(alphaService);
    		System.out.println(simpleDateFormat);
    	}
    ```

- ==那么,开发过程中实际上这么个流程,由Controller处理浏览器的请求,在请求的过程中会调用业务组件(service层)去处理业务,而业务组件会调用Dao层去访问数据库,那么就可以说controller层,service层,dao层他们都是相互依赖的,那么这样的上层到下层的依赖关系刚好可以用Spring这种依赖注入进行实现,比如将AlphaDao注入给AlphaService==

  - ```java
        @Autowired
        private AlphaDao alphaDao;
        
        //写一个方法模拟查询业务
        //也就是service依赖于dao的依赖注入方式
        public String find(){
            return alphaDao.select();
        }
    ```

- 同样的道理Controller处理请求时,调用Service,那就AlphaService注入AlphaController中

  - ```java
        @Autowired
        private AlphaService alphaService;
        
        @RequestMapping("/data")
        @ResponseBody
        public String getData(){
            return alphaService.find();
        }
    ```

- 最后访问`http://localhost:8080/community/alpha/data`就应该会出现mybatis

### 1.4 Spring MVC入门

#### HTTP协议(Web开发基石)

- https://developer.mozilla.org/zh-CN/
- 上面的网站的文档很官方,写的很详细

#### 服务器代码分层

- **目的**为了解耦,为了代码可以方便维护

#####  服务端三层架构

- ==表现层,业务层,数据访问层,同时一般是表现层中是Controller,对应后端项目的controller层,这是最上层的(因为是直接面对请求的),然后controller依赖于service业务层,service业务层又要依赖dao数据层==

##### MVC三层架构

- ==是解决表现层的问题(不要和服务端的三层弄混淆),当浏览器请求服务器时,请求的就是Controller,Controller接收请求中的数据,调用业务层去处理,然后将得到的数据封装在Model中,然后传给视图层,视图层负责渲染展现生成html发送给浏览器==(要注意这个服务端三层架构和MVC三层架构要区分好)
- **核心组件**,前端控制器DispatcherServlet(Controller还是View都是通过这个类来调度的)
- ![mvc context hierarchy](https://docs.spring.io/spring/docs/current/spring-framework-reference/images/mvc-context-hierarchy.png)
- ==DispatcherServlet是核心,SpringMVC所有组件都是DispatcherServlet管理的,管理的前提是基于WebApplicationContext就是Spring容器,管理我们所写的Controller和Service,Dao,还可以管理ViewResolver视图解析器,还能管理HandlerMapping映射组件,我们输入一个路径,这个路径于Controller相匹配,就是由HandlerMapping来管理的,@RequestMapping注解就是由HandlerMapping来进行管理的==
- ![image-20200902133217522](https://raw.githubusercontent.com/CodeNovice2017/ImageRepository/master/img/20200902133217.png)
- Delegate rendering of response 响应委托渲染
- **这个图实际就是Spring MVC的底层调度的原理,这也是面试题**

##### Thymeleaf模板引擎

- 无论是什么模板引擎最终的目的都是利用model数据生成动态的html的
- ![image-20200902134315232](https://raw.githubusercontent.com/CodeNovice2017/ImageRepository/master/img/20200902134315.png)
- 模板引擎想要生效都需要上面提供的模板文件和Model
- Thymeleaf倡导以html文件为模板文件,以前的JSP以jsp文件为模板文件,前端工程师也能看得懂
- 无论是什么模板引擎,我们都是学三方面内容,**标准表达式,判断与循环,模板的布局(将页面一样的区域来做复用)**

###### Thymeleaf的使用

- 首先在application.properties进行配置

- ```properties
  #https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#common-application-properties
  #上面是Spring Boot的常用配置的含义
  #这些配置都是相当于给一个个配置类注入数据,比如spring.thymeleaf.cache=false就是在给ThymeleafAutoConfiguration在注入数据
  #tomcat的端口号
  server.port=8080
  #项目本身的路径
  server.servlet.context-path=/community
  #关闭Thymeleaf关闭,因为开发的时候需要经常改页面,那么如果有缓存就会导致有可能页面的更新会有延迟,上线后应该开启,可以降低服务器的压力
  spring.thymeleaf.cache=false
  ```

- 实际上`ThymeleafAutoConfiguration`类还是有个注解`@EnableConfigurationProperties({ThymeleafProperties.class})`就是说`ThymeleafAutoConfiguration`配置的文件是`ThymeleafProperties`,所以再去`ThymeleafProperties`中去看,一般的配置基本都是直接就是`Properties`的形式,Thymeleaf的配置比较特殊

- ```java
  @ConfigurationProperties(
      prefix = "spring.thymeleaf"
  )
  public class ThymeleafProperties {
      private static final Charset DEFAULT_ENCODING;
      public static final String DEFAULT_PREFIX = "classpath:/templates/";
      public static final String DEFAULT_SUFFIX = ".html";
      private boolean checkTemplate = true;
      private boolean checkTemplateLocation = true;
      private String prefix = "classpath:/templates/";
      private String suffix = ".html";
      private String mode = "HTML";
      private Charset encoding;
      private boolean cache;
      private Integer templateResolverOrder;
      private String[] viewNames;
      private String[] excludedViewNames;
      private boolean enableSpringElCompiler;
      private boolean renderHiddenMarkersBeforeCheckboxes;
      private boolean enabled;
      private final ThymeleafProperties.Servlet servlet;
      private final ThymeleafProperties.Reactive reactive;
  
      public ThymeleafProperties() {
          this.encoding = DEFAULT_ENCODING;
          this.cache = true;
          this.renderHiddenMarkersBeforeCheckboxes = false;
          this.enabled = true;
          this.servlet = new ThymeleafProperties.Servlet();
          this.reactive = new ThymeleafProperties.Reactive();
      }
  ```

- ==可以从上面`ThymeleafProperties`中看到,有一个@ConfigurationProperties(prefix = "spring.thymeleaf")注解,其实就是说这个配置注入的prefix前缀是spring.thymeleaf开头的,我们设置的cache就是spring.thymeleaf开头,然后后面的cache就对应`ThymeleafProperties`的成员变量的名字==

- 所以说Spring Boot的配置实际上原理很简单,我们就是在给Spring的某一个bean注入值

- 视图层的两部分代码一个就是在Controller包,一个就是在resources的templates文件夹中

##### Spring MVC处理请求和响应,包括配置/path/id型参数和/path?id=型参数

- ```java
  //首先演示在Spring MVC框架之下如何获得请求对象,如何获得响应对象,这是偏底层的行为,先介绍底层,Spring MVC自带就简化也就是对这些麻烦的底层做的封装
      @RequestMapping("/http")
      //为什么这里不需要返回值了呢,不需要ResponseBody了呢?
      //因为我们直接可以通过response对象可以直接向浏览器输出任何数据,不依赖返回值了
      //如果想获取请求对象,响应对象那么就在这个方法中进行声明即可,声明了这两个类型以后,dispatcherServlet调用这个方法的时候就会自动的把那两个对象传给你,也就是说这两个对象dispatcherServlet在底层已经创建好了
      //Request对象有好几层接口,常用的接口是HttpServletRequest
      public void http(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
          //处理请求就是处理请求中所包含的数据
          //获取请求数据
          System.out.println(httpServletRequest.getMethod());  //请求方式
          System.out.println(httpServletRequest.getServletPath()); //请求路径
          Enumeration<String> enumeration = httpServletRequest.getHeaderNames(); //请求的头部信息,若干行数据,用的很老的一种数据结构键值迭代器存放的
          while(enumeration.hasMoreElements()){
              String name = enumeration.nextElement();
              String value = httpServletRequest.getHeader(name);
              System.out.println(name + " : " + value);
          }
          //获取业务数据,也就是请求体,也就是包含的参数
          System.out.println(httpServletRequest.getParameter("code"));
  
          //response是用来给浏览器返回响应数据的对象
          //返回响应数据(放回一个网页类型的文本,加上字符集)
          httpServletResponse.setContentType("text/html;charset=utf-8");
          //要想通过response向浏览器响应网页,那就需要输出流向浏览器输出,通过response对象获取到输出流,这个都是底层建好的,不用自己去创建
          //java7之后可以通过try加一个小括号,把writer在这个小括号里创建,编译的时候就会自动finally,关闭流,但是前提是writer必须要有close方法
          try(PrintWriter printWriter = httpServletResponse.getWriter();) {
              //但实际上网页很复杂,一级标题上面还需要有document标签,head标签等,但也底层都是这样一行一行输出的,这边只是演示一下
              printWriter.write("<h1>牛客网</h1>");
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  
      //GET请求的两种传参方式,一种是?name=123,一种是/student/123,路径变量,要用不同的注解获得参数
  
      //现在用更简单的方式编写处理请求request和返回response的代码
      //GET请求的处理(默认请求)
      // students?current=1&limit=20  比如显示学生成绩,然后肯定要分页显示,一个还要限制显示20个,还要有当前是第几页
      // @RequestMapping可以和上面一样简化的声明,也可以使用参数等,明确限制只接受GET请求
      @RequestMapping(path = "/students",method= RequestMethod.GET)
      @ResponseBody
      //只要参数名和传入的参数名保持一致即可,dispatcherServlet检测到这个参数以后,会把request当中与这个方法匹配的参数直接赋值给它
      //也可以通过@RequestParam(name="current",required = false,defaultValue = "1")限制,如果限制了name属性,那么就不用一定要方法参数和请求参数同名
      public String getStudents(@RequestParam(name="current",required = false,defaultValue = "1") int cu, @RequestParam(name="limit",required = false,defaultValue = "10")int limit){
          System.out.println(cu);
          System.out.println(limit);
          return "some students";
      }
  
      // student/123 直接将参数编排到路径当中,成为路径的一部分,此时就不同于上面了
  //    PathVariable就是路径变量,这个注解会从路径中得到这个变量然后赋值给方法参数
      @RequestMapping(path = "/student/{id}",method= RequestMethod.GET)
      @ResponseBody
      public String getStudent(@PathVariable("id") int id){
          System.out.println(id);
          return "a student";
      }
  
      //POST请求一般是浏览器向服务器提交请求时用到的
      
      //这需要使用到静态的页面,至少有一个表单的提交
      //templates文件夹一般存放的是动态的页面,里面是需要model数据来替换的
      //static一般用于存放静态的页面,css,js等
      @RequestMapping(path = "/student",method = RequestMethod.POST)
      @ResponseBody
      //如何获取POST请求中的参数呢?直接声明参数,参数名和表单中数据的名字一致即可,也可以使用requestParam注解
      public String saveStudent(String name,int age){
          System.out.println(name);
          System.out.println(age);
          return "success";
      }
  ```

- 

- ==DispatcherServlet会调用Controller的某个方法->这个方法需要依赖业务层和数据层,Controller将获取的数据组装为Model还有视图相关的数据,然后将Model和视图数据提交给模板引擎,然后由模板引擎渲染,然后由Controller最终返还给dispatcherServlet,然后返回给浏览器==

##### DispatcherServlt的自动初始化和自动参数赋值

- ==Spring MVC的核心DispatcherServlet会自动的帮我们处理路径中的参数,比如下面这个Controller的例子,明明有参数设置为两个对象,一个Model,一个Page,DispatcherServlet会自动把他们作为bean实例化,同时遇到参数名和传入的参数名一致,dispatcherServlet检测到网页请求的路径中有这个参数以后(在本例是`http://localhost:8080/community/index?current=15`),会把Page当中与这个方法匹配的参数current直接赋值给它的current属性,可以通过Debug设置断点来验证==

- ```java
  @RequestMapping(path = "/index",method = RequestMethod.GET)
      public String getIndexPage(Model model, Page page){
  
          // 总行数
          // 首页 不以用户id为参数查询 所以参数填0
          page.setRows(discussPostService.findDiscussPostRows(0));
          page.setPath("/index");
          // page还有当前页码,每页显示数据,html页面可以传进来,就无须服务器操心了
  
          List<DiscussPost> list = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit());
          // 这个集合里面是一个能够封装DiscussPost以及User对象的一个对象
          List<Map<String,Object>> discussPosts = new ArrayList<>();
          if(!list.isEmpty()){
              for(DiscussPost discussPost : list){
                  Map<String,Object> map = new HashMap<>();
                  map.put("post",discussPost);
                  User user = userService.findUserById(discussPost.getUserId());
                  map.put("user",user);
                  discussPosts.add(map);
              }
          }
          model.addAttribute("discussPosts",discussPosts);
          // 最后返回模板的路径
          // 同时因为Thymeleaf的模板就是html文件,就是固定的一个文件,所以不需要写后缀,就只写文件名即可,只要心里知道下面这个路径的view指的是index.html即可
          return "/index";
      }
  ```

###### 如何向浏览器响应数据,如何向浏览器响应一个动态的html?

- **一种方法(ModelAndView)**

- ```java
      //如何向浏览器响应数据
      //如何向浏览器响应一个动态的html
  
      @RequestMapping(path = "/teacher",method = RequestMethod.GET)
      //要返回html就不要加@ResponseBody这个注解了,不加这个注解默认就是返回html
      //之前讲过Spring MVC的原理,实际上Controller响应返回给浏览器的就是由模板引擎渲染后由Controller,
      // DispatcherServlet会调用Controller的某个方法->这个方法需要依赖业务层和数据层,Controller将获取的数据组装为Model还有视图相关的数据
      // 然后将Model和视图数据提交给模板引擎,然后由模板引擎渲染,然后由Controller最终返还给dispatcherServlet,然后返回给浏览器
      public ModelAndView getTeacher(){
          ModelAndView modelAndView = new ModelAndView();
          //就是模板里需要多少个变量,就add多少个数据
          modelAndView.addObject("name","张三");
          modelAndView.addObject("age",30);
          //还要向ModelAndView对象里面设置模板的路径和名字
          //模板就是放在templates目录下,这一级目录不需要写,下一级目录要写
          //同时因为Thymeleaf的模板就是html文件,就是固定的一个文件,所以不需要写后缀,就只写文件名即可,只要心里知道下面这个路径的view指的是view.html即可
          modelAndView.setViewName("/demo/view");
          return modelAndView;
      }
  ```

- 然后在模板中,用模板引擎特性的语法编写即可

- ```html
  <!DOCTYPE html>
  <!--要先做一个声明.让服务器知道这是一个thymeleaf模板,而不是普通的html-->
  <!--xmlns:th="http://www.thymeleaf.org"就是告诉服务器,我当前的网页是一个模板-->
  <html lang="en" xmlns:th="http://www.thymeleaf.org">
  <head>
      <meta charset="UTF-8">
      <title>老师注册</title>
  </head>
  <body>
      <p th:text="${name}">
  
      </p>
      <p th:text="${age}">
  
      </p>
  </body>
  </html>
  ```

- 另一种方法

- ```java
      //另一种更简单一些的响应写法
      //就是上面的方法其实更直观,是将Model和View都装在一个对象ModelAndView里面,而这个方法不是这么弄的,把View的视图直接返回,而Model的引用是通过Model是一个由Spring管理的bean,是通过dispatcherServlet自动实例化持有的
      @RequestMapping(path = "/school", method = RequestMethod.GET)
      //view可以通过String的返回值来传递,model的传递要靠Model这个类型,Model这个类型也不是我们自己创建的,而是dispatcherServlet调用这个方法时,看到我们的参数有Model对象,他就会自动实例化这个对象传给我们
      //Model也是一个bean,是一个对象,dispatcherServlet是持有这个引用的,所以我们在方法内部向Model里面存数据,dispatcherServlet也是可以得到的
      public String getSchool(Model model){
          
          //return的是view的路径,就是返回的类型写成String,就代表返回的是view的路径
          model.addAttribute("name","北京大学");
          model.addAttribute("age","80");
          return "/demo/view";
      }
  ```

- ==就是上面的方法其实更直观,是将Model和View都装在一个对象ModelAndView里面,而这个方法不是这么弄的,把View的视图直接返回,而Model的引用是通过Model是一个由Spring管理的bean,是通过dispatcherServlet自动实例化持有的==

##### 返回JSON的响应

- 向浏览器响应JSON数据通常是在异步请求当中

###### 什么是异步请求?

- ==例如,我要注册B站,点击注册,注册过程要输入昵称密码,而输入昵称过程中,失去焦点后就会判断昵称是否被占用,而此时实际上浏览器请求了服务端,且服务端也响应了,但是页面并没有被刷新,当前网页不刷新,悄悄访问了服务器一次得到了一次响应,这也代表本次响应不是一个html==

###### JSON响应方式

- **注意:当前我们写的代码都是Java代码,Java是面向对象的语言,所以打交道的都是一些Java对象,如果要把Java对象返回给浏览器,而浏览器解析对象用的是JS对象**

- **浏览器肯定希望直接接受JS对象,Java对象肯定是无法直接转变为JS对象**

- **JSON就可以实现两者的兼容.JSON本质上就是一个带有特定格式的字符串**

- JSON就可以起到衔接的作用,尤其是在异步请求当中,客户端要求返回一个是否验证成功的结果用这种方式就很方便

- ```java
  @RequestMapping(path = "/emp",method = RequestMethod.GET)
     //如果要返回的响应是JSON的话,那么一定要加上ResponseBody,否则会认为返回的是html
     //dispatcherServlet调用这个方法的时候,一看加了@ResponseBody注解,并且声明返回的是这样的HashMap类型,他会自动转换java对象为JSON字符串
     @ResponseBody
     public Map<String,Object> getEmp(){
         Map<String, Object> map = new HashMap<>();
         map.put("name","张三");
         map.put("age",24);
         map.put("salary",8000.00);
         return map;
     }
   
     //还有有时候可能返回的不是一个员工,而是一组员工,也就是返回多个结构的情况
     @RequestMapping(path = "/emps",method = RequestMethod.GET)
     //如果要返回的响应是JSON的话,那么一定要加上ResponseBody,否则会认为返回的是html
     //dispatcherServlet调用这个方法的时候,一看加了@ResponseBody注解,并且声明返回的是这样的HashMap类型,他会自动转换java对象为JSON字符串
     @ResponseBody
     public List<Map<String,Object>> getEmps(){
   
         List<Map<String,Object>> list = new ArrayList<>();
   
         Map<String, Object> map = new HashMap<>();
         map.put("name","张三");
         map.put("age",24);
         map.put("salary",8000.00);
         list.add(map);
   
         Map<String, Object> map1 = new HashMap<>();
         map1.put("name","李四");
         map1.put("age",24);
         map1.put("salary",8000.00);
         list.add(map1);
   
         Map<String, Object> map2 = new HashMap<>();
         map2.put("name","王二麻子");
         map2.put("age",24);
         map2.put("salary",8000.00);
         list.add(map2);
   
         return list;
     }
  ```

##### Spring+Spring MVC题目

- 在项目资料中

### 1.5 Mybatis入门

#### MySQL安装配置

- 见MySQL笔记即可

#### 项目数据库部署

- `create database community;` 创建数据库
- `show databases` 查看全部数据库
- `use community;` 使用该数据库
- `source C:/workspace/Coder/Java_Codes/communityProjectData/community-init-sql-1.5/init_schema.sql` 
- `source C:/workspace/Coder/Java_Codes/communityProjectData/community-init-sql-1.5/init_data.sql`
- 注意反斜杠,注意上面两个数据导入有先后顺序
- `show tables`

#### 核心组件

- www.mybatis.org/spring学习

- **SqlSessionFactory**：用于创建SqlSession的工厂类
  - SqlSession是Mybatis最核心的组件,用来向数据库执行sql的一个对象,相当于jdbc当中的connection
- **SqlSession**：MyBatis的核心组件，用于向数据库执行SQL
- **主配置文件**：XML配置文件，可以对MyBatis的底层行为做出详细的配置
- **Mapper接口**：就是DAO接口，在MyBatis中习惯性的称之为Mapper
- **Mapper映射器**：用于编写SQL，并将SQL和实体类映射的组件，采用XML、注解均可实现(==为什么需要这样一个东西呢?因为我们使用mybatis访问数据库,我们只需要写出接口,不需要写出实现类,底层会帮我们自动实现这个接口,前提是把增删改查的操作所依赖的SQL告诉他,所以映射器就是编写sql的,映射器不只是有sql,还有sql查询到了数据或者是向数据库提交数据的时候,sql需要和java中的实体类,比如查到了一个结果,就用一个类去封装,查了user表,用user类封装,字段也要对应,映射器也是sql和实体类之间的一种对应关系==)

##### Spring Boot下的Mybatis

- 在Spring boot项目中,前三个**SqlSessionFactory**,**SqlSession**,**主配置文件**不需要写代码,因为Spring Boot会自动的初始化SqlSessionFactory,自动的创建SqlSession,自动的整合了配置文件,可以直接把xml中的配置在application properties中配置

##### User表小解析

- salt就是为了给用户密码加点料,防止用户密码过于简单被破解
- header_url用户头像
- create_time 用户注册时间

##### 开始代码

- https://mvnrepository.com/通过网址找使用人数最多的

- 然后复制maven代码

- 粘贴到项目的pom.xml(**Project Object Model 项目对象模型**)中去

- **本项目**:分别搜索mysql和mybatis

- mybatis搜索后有mybatis,mybatis spring,mybatis spring boot starter,因为我们的项目时spring boot框架 所以选择最后一个,因为这样肯定是最适合本项目的,也会带一些默认的配置

- 包处理完了之后,还需要对数据库和mybatis做一些配置,因为即便是spring boot能自动配置,他也无从知道你连的数据库是什么,账号密码是什么,启动的最大连接是什么

- ```properties
  # MySQL和Mybatis
  # DataSourceProperties(配置的是mysql数据库和连接池,连接池也叫数据元,是可以统一管理连接的工厂,能够统一的初始化一批连接,可供反复使用,还能够管理连接的上限,避免数据库因为过多的人次访问瘫痪)
  # 前面四条是mysql配置,后面四条是连接池的配置
  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
  spring.datasource.url=jdbc:mysql://localhost:3306/community?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong
  spring.datasource.username=root
  spring.datasource.password=chenxinshuai5656
  # 这个是Spring Boot中性能最好的连接池
  spring.datasource.type=com.zaxxer.hikari.HikariDataSource
  # 最大连接数
  spring.datasource.hikari.maximum-pool-size=15
  # 最小空闲连接 连接都被回收了之后 最少保留5个
  spring.datasource.hikari.minimum-idle=5
  # 超时时间,就是连接空闲了就立刻关闭,要等30000ms才关闭
  spring.datasource.hikari.idle-timeout=30000
  
  # MybatisProperties
  # 映射文件的存放位置,为什么是classpath下,因为项目编译后,target/classes就是classpath类路径,而static和template等都在这个路径下
  mybatis.mapper-locations=classpath:mapper/*.xml
  # 这个是用实体类去封装好表里的数据,这个是指定实体类所在的包名,未来就会在entity下创建实体类用于封装表的数据
  # 配置之后,在配置文件里引用实体类的时候就不用写包名了
  mybatis.type-aliases-package=com.nowcoder.community.entity
  # 启动自动生成组件 insert id自增长的支持
  mybatis.configuration.useGeneratedKeys=true
  # 表的字段是不区分大小写的,我们建立的实体类的属性和表的字段对应,但是我们一般实体类采用驼峰式命名,而表的字段是全小写,这个选项让实体类和表的字段可以匹配起来
  mybatis.configuration.mapUnderscoreToCamelCase=true
  ```

- mybatis开发一般管数据访问组件(DAO层的组件)叫Mapper,而且只需要写接口,不要自己写实现类

- 可以使用@Repository标识数据访问组件(也就是DAO层的bean),也可以使用mybatis的注解Mapper

- ==要想实现比如写完的UserMapper这个接口,就要给他提供配置文件,配置文件给每个方法提供sql,这样mybatis底层就会帮我们自动生成它的实现类==

- ```java
  package com.nowcoder.community.dao;
  
  import com.nowcoder.community.entity.User;
  import org.apache.ibatis.annotations.Mapper;
  
  // 可以使用@Repository标识数据访问组件(也就是DAO层的bean),也可以使用mybatis的注解Mapper
  //@Repository
  @Mapper
  public interface UserMapper {
  
      User selectById(int id);
  
      User selectByName(String username);
  
      User selectByEmail(String email);
  
      int insertUser(User user);
  
      int updateStatus(int id, int status);
  
      int updateHeader(int id, String headerUrl);
  
      int updatePassword(int id, String password);
  
  }
  
  ```

- xml的写法如下

  ````xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <!DOCTYPE mapper
          PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
          "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
  <mapper namespace="org.mybatis.example.BlogMapper">
      <select id="selectBlog" resultType="Blog">
      select * from Blog where id = #{id}
    </select>
  </mapper>
  ````

- `mybatis.configuration.mapUnderscoreToCamelCase=true`这个properties的作用就能看出来了,比如我们在DiscussPostMapper中写的selectDiscussPosts这个方法的SQL语句是下面的

  - ```xml
        <select id="selectDiscussPosts" resultType="DiscussPost">
            select <include refid="selectFields"></include>
            from discuss_post
            where status!=2
            <if test="userId!=0">
                and user_id = #{userId}
            </if>
            order by type desc,create_time desc
            limit #{offset},#{limit}
        </select>
    ```

  - 可以看出实际上我们并没有将entity内的DiscussPost的成员变量的名字和表的字段设置为相同的,但是通过配置上面所说的配置,我们就是实现了驼峰式命名和表的字段的智能匹配(==**问:如果没有配置匹配这个配置,如何实现自动的匹配呢?**==)

##### Mybatis调试技巧

- Mapper配置文件xml是比较容易出错的
- 我们可以降低日志

### 1.6开发社区首页

#### 开发技巧

- ==一般开发过程中,我们可以先开发DAO(entity实体类其实是最先开发的),然后开发Service,最后开发Controller,按照他们依赖的顺序这样开发是比较舒服的==

- 功能的拆解是很重要的

- 进行冗余的设计,比如帖子的评论数在commet表和discuss_post表都有这个数据,我们可以让他们关联,但是这样会增加数据库的压力,可以采用冗余的方式,直接为discuss_post也单独设计一个评论数的字段

- (**DAO层概念**)@Param注解用于给参数取别名,比如有的参数名比较长,嫌到sql中写的麻烦就起一个别名.如果只有一个参数,并且在<if>里使用,则必须加别名,如果需要动态的拼一个条件,并且这个方法有且只有一个条件,这个时候这个参数之前就必须要取别名

- (**Mapper文件编写技巧**)给DAO接口方法写对应Mapper的sql要注意,比如这个方法返回的是一个List,但是集合里装的不是java自带的类型,而是DiscussPost,所以我们只需要声明DiscussPost,而List不用声明,因为List是java自带的类型

- **区分好SQL中直接写的是表的字段,#{}内写的是方法的参数**

- (**模板引擎**)绝对路径,没什么歧义就可以直接这么写,但是如果是相对路径,开发静态页面时可能是这样的关系,但是引入动态的项目中的话,可能会发生一些变化,但是可以通过thymeleaf的语法来处理

  - ```html
    <link rel="stylesheet" th:href="@{/css/global.css}" />
    ```

  - **上面的语法就可以让thymeleaf去static下去找这个资源,就不会产生歧义,一般以后相对路径的资源,比如css,js都这么处理**

- ==对index.html模板帖子的li使用thymeleaf的语法进行了foreach循环,th:each="map:${discussPosts} 表示我要循环的是discussPosts这个集合,每次循环我将得到一个Map对象,但是这个map只是一个变量名,只是为了逻辑符合==

#### thymeleaf语法解析

##### 解析一下thymeleaf语法

- `th:src="${map.user.headerUrl}" `

- map.user实际上是访问的,map.get("user"),因为thymeleaf可以识别到这个map是一个HashMap类型,然后会调get方法,然后以user为key去访问,然后最终我们得到了一个User对象,然后我们用.headerUrl就是实际调用的这个bean的user.getHeaderUrl()方法,所以说最关键的就是这个`.`实际在底层就是在调用`get`

- ```html
  <a href="#" th:utext="${map.post.title}">
  ```

  - utext语法,比如说a标签内的文本有一个转义字符`&lt`如果是text那么它只会显示这个东西,如果是utext,他会自动把转义字符转换为它该表现的形式,所以说当标题,内容有可能要有转义字符的时候最好使用utext

- `th:text="${#dates.format(map.post.createTime,'yyyy-MM-dd HH:mm:ss')`通过`#`引用thymeleaf内置的工具,具体的写法就是上面的写法(固定的写法内置的api),如果直接使用`map.post.createTime`那么这个日期的格式是西方的那种格式

##### 解释:为什么原本应该将page对象也通过model.addAttribute("discussPosts",discussPosts);添加进model,但是其实无需这么做的原因

- ==首先我们知道,在Spring MVC框架之中,方法的参数其实都是由DispatcherServlet帮我做初始化的,这个Page也可以初始化,并且数据也是由dispatcherServlet注入来的,dispatcherServlet除了帮我们做了这些以外,还会自动的帮我们把Page类型对象装入Model中,所以我们直接可以在thymeleaf中访问page对象了==

##### 解析Thymeleaf的某个语法

- `<li th:class="|page-item ${page.current==1?'disabled':''}|">`

- 先加th: 表示这里面可能有动态的数据处理,当然这里面不只是有固定的数据,page-item就是固定的,而我们希望在current当前页是1的时候,上一页是不可点击的状态,也就是添加一个类选择器disabled,而不是第一页时那就什么也不加,这种既包含动态又包含静态部分的情况,可以使用||括起来,变量的位置添加`${}`

##### 解析Thymeleaf的语法

- `<li th:class="|page-item ${i==page.current?'active':''}|" th:each="i:${#numbers.sequence(page.from,page.to)}">`

- 同时还有技巧就是说,`${}`内是声明动态的部分,像下面代码部分的这种原本就包含在`${}`内的就无须在额外的加`${}`了

  - ```html
    <li th:class="|page-item ${i==page.current?'active':''}|" th:each="i:${#numbers.sequence(page.from,page.to)}">
    ```

  - 上面这部分代码也可以拿来解析一下,原本这个代码使用来生成上一页和下一页之间的页数部分的,我们会要先能确定两边的页码,然后动态的foreach生成两边页码的li标签,所以要用到thymeleaf的each循环,然后通过thymeleaf自带的numbers工具的sequence方法可以生成从某个数到另一个数的数组,然后我们定义一个i保存每一个数,这就是`th:each="i:${#numbers.sequence(page.from,page.to)}"`这部分的解析,然后因为是生成了从page.to到page,from之间的这么多个li标签,其中有一个i是和current当前页码一样的,一样的时候的li需要额外有一个active的类名,动态的加上css样式,所以有了前部分`th:class="|page-item ${i==page.current?'active':''}|"`,也就是说i与current不相等时的li都是没有active状态的

### 1.7项目调试技巧

#### 重定向

- > 请求的资源现在临时从不同的 URI 响应请求。由于这样的重定向是临时的，客户端应当继续向原有地址发送以后的请求。只有在Cache-Control或Expires中进行了指定的情况下，这个响应才是可缓存的

- 比如删除操作之后,我们一般返回都应该是html页面,那删除之后我们应该返回什么呢,一般是返回列表显示那个页面(让客户端看到列表里没有这个了),也就是浏览器请求删除,服务器响应返回查询页面返还给浏览器

-  但像上面所说的方法其实并不好,本来删除和查询是两个独立的操作,现在就依赖在一起了,因为至少在删除的Controller之后要返回一个另一个查询模块的ModelAndView

- 我们可以利用重定向,也就是说浏览器删除操作之后,服务器并不给返回一个页面,而是返回一个建议(状态码302),浏览器就会知道服务器并不想返回一个网页,而是希望浏览器访问另一个页面,这个返回的响应还会带一个建议访问的网址,浏览器会自主的去访问

- 也就是说客户端变成了分两次请求去服务端,这两次请求也是彼此独立的,这就是重定向的价值

- 项目中比较常用重定向的地方,比如注册后返回登录页面

- 总之就是低耦合的方式实现功能之间的跳转

#### 最常见的几种状态码

- 200 表示访问成功
- 302 表示重定向
- 404 表示路径可能不对 访问不到服务器的资源
- 500 服务器无法处理请求 表示服务器程序出了问题

#### 调试技巧

- 打断点 逐行跟踪 同时要理解因为我们即便打上断点,也只是在Controller打上的断点,就算点击Debug启动项目也不会卡在加断点的位置,因为我们只是以Debug模式启动了服务现在,还没有访问对应设置断点的页面,没有触发这个Controller,自然也不会运行这个Controller的代码
- (**新技巧 打两个断点,尤其适用于比如要跳过某个可能循环次数超多的循环,多打一些断点,然后在debug模式下可以直接按f9,继续运行到下一个断点,越过这个循环**)
- IDEA可以统一的帮我们管理断点`ctrl shift f8`打开一个管理断点的界面
- **客户端调试**:就是在浏览器中f12后,在source中的想调试的js文件中设置断点等即可,也有一个breakpoints界面统一管理断点

##### 日志调试(重要)

- Spring Boot默认启用的日志是logback工具

- 为了长久保存日志信息,而不是只是显示在控制台(因为服务器环境不可能安装IDEA之类的ide),还是要保存在文件

- 简单测试可以在application.properties中配置`logging.file=C:/workspace/Coder/Java_Codes/communityProjectData/logs/community.log`输出到一个文件,但是真正开发上线时一般不会只打印日志在这一个文件中(比如error/warn/info的分文件)

- 那么通过application.properties就不是很方便达到更复杂的效果了,解决办法就是用logback原生的配置文件进行配置

- ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <configuration>
  <!--    项目名称-->
      <contextName>community</contextName>
  <!--    日志文件储存路径-->
      <property name="LOG_PATH" value="C:/workspace/Coder/Java_Codes/communityProjectData/logs"/>
  <!--    会在上面存储路径之下创建一个community,然后把日志文件放在这个文件夹里-->
      <property name="APPDIR" value="community"/>
  
      <!-- error file -->
      <appender name="FILE_ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>${LOG_PATH}/${APPDIR}/log_error.log</file>
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
              <fileNamePattern>${LOG_PATH}/${APPDIR}/error/log-error-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
              <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                  <maxFileSize>5MB</maxFileSize>
              </timeBasedFileNamingAndTriggeringPolicy>
              <maxHistory>30</maxHistory>
          </rollingPolicy>
  <!--        追加形式存储,而不是覆盖-->
          <append>true</append>
          <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
              <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
              <charset>utf-8</charset>
          </encoder>
  <!--        过滤器,什么样的日志会被我这个文件记录-->
          <filter class="ch.qos.logback.classic.filter.LevelFilter">
              <level>error</level>
              <onMatch>ACCEPT</onMatch>
              <onMismatch>DENY</onMismatch>
          </filter>
      </appender>
  
      <!-- warn file -->
      <appender name="FILE_WARN" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>${LOG_PATH}/${APPDIR}/log_warn.log</file>
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
              <fileNamePattern>${LOG_PATH}/${APPDIR}/warn/log-warn-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
              <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                  <maxFileSize>5MB</maxFileSize>
              </timeBasedFileNamingAndTriggeringPolicy>
              <maxHistory>30</maxHistory>
          </rollingPolicy>
          <append>true</append>
          <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
              <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
              <charset>utf-8</charset>
          </encoder>
          <filter class="ch.qos.logback.classic.filter.LevelFilter">
              <level>warn</level>
              <onMatch>ACCEPT</onMatch>
              <onMismatch>DENY</onMismatch>
          </filter>
      </appender>
  
      <!-- info file -->
      <appender name="FILE_INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>${LOG_PATH}/${APPDIR}/log_info.log</file>
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
              <fileNamePattern>${LOG_PATH}/${APPDIR}/info/log-info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
              <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                  <maxFileSize>5MB</maxFileSize>
              </timeBasedFileNamingAndTriggeringPolicy>
              <maxHistory>30</maxHistory>
          </rollingPolicy>
          <append>true</append>
          <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
              <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
              <charset>utf-8</charset>
          </encoder>
          <filter class="ch.qos.logback.classic.filter.LevelFilter">
              <level>info</level>
              <onMatch>ACCEPT</onMatch>
              <onMismatch>DENY</onMismatch>
          </filter>
      </appender>
  
      <!-- console -->
      <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          <encoder>
              <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
              <charset>utf-8</charset>
          </encoder>
          <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
              <level>debug</level>
          </filter>
      </appender>
  
      <logger name="com.nowcoder.community" level="debug"/>
  
      <root level="info">
          <appender-ref ref="FILE_ERROR"/>
          <appender-ref ref="FILE_WARN"/>
          <appender-ref ref="FILE_INFO"/>
          <appender-ref ref="STDOUT"/>
      </root>
  
  </configuration>
  ```


## 2 项目开发

### 2.1 邮件模块

#### 启用客户端SMTP服务

- 一般邮箱是不允许调用程序来进行邮件的发送的,我们需要进行配置
- codecx@163.com KCRMQYCKVRVWLHMD
- 注意一点即可就是:**邮箱密码是开通pop3/smtp服务时所给的授权码，而不是你注册邮箱时的真实密码**

#### Spring Email技术(Spring集成的模块)

- 导入jar包
- 配置参数
- 核心:JavaMailSender发送邮件

#### 模板引擎(邮件会携带链接和图片等,使用Thymeleaf发送html邮件)

##### ==如何在测试类或者非Controller(非MVC控制的情况下)中调用Thymeleaf模板引擎?==

- ==先说项目中遇见的这种情况:就是不在Controller下,也就是不需要`@RequestMapping(path = "/index",method = RequestMethod.GET)`配置路径的情况下,仍需要thymeleaf模板引擎,这次是场景是编写发送注册的Service,我们只是需要模板引擎对html模板进行动态的加载,然后我们需要将渲染之后的html以邮件的形式发送出去,所以这个html是不需要配置路径的,因为他只是个html文件,不是任何服务端的路径,所以此时,没有被DispatcherServlet接管控制的register这个方法下是不会自动的配置thymeleaf的,只能我们主动的调用thymeleaf==

- 在MVC的DispatcherServlet的帮助下,我们可以很容易的在Controller配置模板,只要返回一个String路径即可,DispatcherServlet会自动帮我们调用模板
- 但是在Test环境下我们需要主动去调用thymeleaf模板
- 也不难,thymeleaf模板引擎有一个核心的类,这个类是被容器管理起来了,直接注入这个bean即可TemplateEngine

### 2.2 开发注册功能

#### 分析

- 访问注册页面请求
- 提交注册数据请求
  - 通过表单提交数据
  - 服务端验证
  - 激活邮件
- 激活注册账号请求

#### 三层架构开发流程



##### thymeleaf开发register

- 现在html标签添加`<html lang="en" xmlns:th="http://www.thymeleaf.org">`
- 配置好各种相对路径`href="../css/global.css`修改为`th:href="@{/css/login.css}`
- 去index页面配置好点击注册的href,将`href="site/register.html`改为`th:href="@{/register}`(去掉.html和site,前面加上/)
- 想要复用首页的header模版,添加一个fragment`<header class="bg-dark sticky-top" th:fragment="header">`
- 然后在register页面的对应header添加`th:replace="index::header"`,表示用这项内容替换当前标签的内容

##### ==为什么将`href="site/register.html`改为`th:href="@{/register}`?==

- ==首先要明白`href="site/register.html`的写法是可以访问的,只不过这只是在访问服务器静态的资源,而/site/register在Spring MVC的DispatcherServlet的控制下是Thymeleaf的模板设置的视图,通过@RequestMapping配置好的/register才是给客户提供服务的路径,单纯的访问/site/register没有什么意义,而且未来因为路径保护,除了@RequestMapping配置好的路由,其他的静态资源应该是不能通过地址进行访问的,因为这些是服务器的静态资源,而此处的href明显要改成我们想让用户访问服务器的路径,那自然是直接在项目名下加上/register即可,没有site这一层,除非在Controller配置的@RequestMapping的path就是/site/register,想让用户访问的就是`localhost:8080/community/register`==
- 也就是说这些页面在静态页面访问的环境下是正确的,但是如果是通过动态模板加载进来的,那么这个路径就不对了,因为相对路径都会发生改变,所以如果在动态页面的环境下再去点击页面内的图片去访问相对路径的静态资源就可能会出现错误了,比如举一个例子
- 比如我们通过`localhost:8080/community/login`来访问,那么由于配置好了Controller内的@RequestMapping相应的映射,那么Spring MVC的DispatcherServlet就会自动的装配Model和View,此时的view是`/site/login`实际上就是`/site/login.html`,(无需html是因为原本thymeleaf就是以html文件作为模板的),但是此时访问的路径依然是`localhost:8080/community/login`,那么我们看里面的比如一个未修改为动态加载的图片标签的源码为`<img src="../img/captcha.png" style="width:100px;height:40px;" class="mr-2">`,那么项目启动后,`localhost:8080/community/img/captcha.png`这个才是正确的可访问到静态资源的路径,而按照未改变的`src="../img/captcha.png"`实际上是在本地的磁盘的位置来判断路径的,按照Spring的项目结构实际这个写法的真正访问位置是`C:\workspace\Coder\Java_Codes\community\src\main\resources\templates\img\captcha.png`这肯定是错误的路径,而在本地的正确路径是`C:\workspace\Coder\Java_Codes\community\src\main\resources\static\img\captcha.png`,所以这个写法在本项目中无论是怎么静态启动还是启动项目后动态页面都是无法显示的
- 通过在网上搜索相关知识点,static是专门存放静态资源的，如果把html文件也放在static中，传统的引入就可以使用，但是把html放在templates中，就必须采用thymeleaf的语法来引入，另外thymeleaf中的静态页面必须要去调用，无法直接访问，只有启动项目以后才可以访问，不启动无法解析,也就是说只有项目启动后,才能通过访问`localhost:8080/community/img/captcha.png`来访问到图片,这是因为项目本身配置了`server.servlet.context-path=/community`,同时static这一层是默认的,不需要加在路径中,所以才能够通过`<img th:src="@{/img/captcha.png}" style="width:100px;height:40px;" class="mr-2"/>`这样的动态配置来实现路径的正确配置

##### Commons Lang包

- 额外加一个包:主要用于处理一些字符串,集合等空值的情况

##### 本地域名配置

- 域名配置(这个key都是我们自己取名的)方便以后上线的配置
  `community.path.domain=http://localhost:8080`

##### md5加密

- md5密码加密,md5加密特点:只能加密,不能解密

- 比如hello加密 -> abc123456dnf  每次加密都是这个值,但是无法解密,看起来好像挺安全,容易被暴力破解(简单密码库),所以用到了User表的salt相加起来之后再加密

- ```java
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
  ```

##### 准备结束,正式开发

- 发邮件要生成激活码,激活码邮件要包含域名还有项目名

- 域名配了key,项目名很早就server.servlet.context-path=/community配置了

- 但是这两个只是value,而不是bean,需要用到${}表示我用表达式的方式取key的值,**还要注意不要写错,因为这个字符串是不会检查是否正确的**

- ```java
      @Value("server.servlet.context-path")
      private String contextPath;
      @Value("community.path.domain")
      private String domain;
  ```

- ```java
  // 返回值可以设置为整数,表示返回不同的状态,也可以返回String,也可以返回自定义的类,这个方法返回各种错误信息,密码不能为空,账号不能为空等错误信息
     public Map<String,Object> register(User user){
         Map<String,Object> map = new HashMap<>();
   
         // 判断空值
         // 如果用户对象为空,那么肯定是程序出现了问题了
         if(user == null){
             throw new IllegalArgumentException("参数不能为空");
         }
         // 如果用户名是空的,不能报错,账号是空的只是业务上的漏洞,但不是程序的错误,我们就把信息封装起来返回给客户端,告诉其这样是不对的
         if(StringUtils.isBlank(user.getUsername())){
             map.put("usernameMsg","账号不能为空");
             return map;
         }
         if(StringUtils.isBlank(user.getPassword())){
             map.put("passwordMsg","密码不能为空");
             return map;
         }
         if(StringUtils.isBlank(user.getEmail())){
             map.put("emailMsg","邮箱不能为空");
             return map;
         }
   
         // 是否已存在业务逻辑判断
         // 验证账号
         User u = userMapper.selectByName(user.getUsername());
         if(u != null){
             map.put("usernameMsg","该账号已存在");
             return map;
         }
         // 验证邮箱
         u = userMapper.selectByEmail(user.getEmail());
         if(u != null){
             map.put("emailMsg","该邮箱已存在");
             return map;
         }
   
         // 如果上面全通过了,那么意味着确实用户可以成功注册了
         // 注册用户
         // 先随机生成一个字符串salt,用substring做一个五位的长度限制
         user.setSalt(CommunityUtil.generateUUID().substring(0,5));
         user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
         // 普通用户
         user.setType(0);
         // 尚未激活状态
         user.setStatus(0);
         user.setActivationCode(CommunityUtil.generateUUID());
         user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
         user.setCreateTime(new Date());
         // 调用insert语句之后,user对象里面就有id了,mybatis自动的生成id的回填
         // 原因:因为我们配置了mybatis.configuration.useGeneratedKeys=true,使用自动生成id的机制,然后id的字段对应着哪个属性就会给那个属性自动的回填
         userMapper.insertUser(user);
   
         // 激活邮件
         Context context = new Context();
         context.setVariable("email",user.getEmail());
         // 要求激活路径应该是这样的http://localhost:8080/community/activation/101/activateCode
         String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
         context.setVariable("url",url);
         String content = templateEngine.process("/mail/activation",context);
         mailClient.sendMail(user.getEmail(),"牛客网注册激活邮件",content);
   
         // 最后返回的map为空就代表没有问题
         return map;
     }
  ```

- 整个注册Service的业务逻辑就是上面的代码,大体设计是返回一个Map对象,map里面装的是键值对,解释为`消息类型,消息内容`,比如`usernameMsg,"用户名已存在"`,而当最后map为空时就代表整个流程走完了,注册成功,所有的注册业务逻辑都是在Service实现的,包括发送激活邮件,Controller装配Service后,就直接调用Controller作用就是将注册请求表单的信息(包括username,password,email,都是通过register模板的三个输入框的name属性设置同名来完成自动注入到Controller方法的参数User中的)

##### 整理一下整个注册功能的运行流程

- 大体的流程就是按照Controller的逻辑,下面分析一下Controller代码

- ```java
      @Autowired
      private UserService userService;
  
      @RequestMapping(path = "/register",method = RequestMethod.GET)
      public String getRegisterPage(){
          return "/site/register";
      }
  
      @RequestMapping(path = "/register",method = RequestMethod.POST)
      // 这里可以声明三个参数去接收那三个值(账号,密码,邮箱),也可以直接加一个User对象参数,只要传入值与user对象的属性相匹配,DispatcherServelet会自动进行注入
      public String register(Model model, User user){
          Map<String,Object> map = userService.register(user);
          if(map == null || map.isEmpty()){
              // 注册成功使用重定向方式调到首页去
              model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
              model.addAttribute("target","/index");
              return "/site/operate-result";
          }else{
              // 注册失败时,把错误消息返还给发送页面
              model.addAttribute("usernameMsg",map.get("usernameMsg"));
              model.addAttribute("passwordMsg",map.get("passwordMsg"));
              model.addAttribute("emailMsg",map.get("emailMsg"));
              return "/site/register";
          }
      }
  ```

- 首先自动注入了UserService,然后共声明了两个映射到相同url(/register)的@RequestMapping的方法,Spring MVC是通过不同的请求方法来进行区分的,所以这两个相同路径的@RequestMapping是不会冲突的,因为他们要处理的请求方式是不同的,一个是处理`RequestMethod.GET`,一个是处理`RequestMethod.POST`,所以第一次点击通过首页注册页面,那肯定是以GET请求进来的,肯定是由getRegisterPage方法接管,而当通过提交注册按钮之后,肯定是POST请求方式,无论成功与否都会由register方法接管,原因如下

  - 点击type为submit类型的button之后,那么通常情况下,按照前端的语法,直接通过form提交的话,提交后当前页面跳转到form的action所指向的页面,而register.html的这个form表单的action刚好是`<form class="mt-5" method="post" th:action="@{/register}">`,所以就是以POST请求的方式在访问/register路径,那么自然就一直由register的方法来接管

###### ==为什么注册失败之后能够保存刚输入过的用户名和提示错误信息?==

- ==同时当我们跳回错误页面的时候,错误页面是可以直接访问User的,因为在Spring MVC调用这个方法时,User已经存在Model对象中了,而且我们仅是错误返回这个页面的时候才应该有默认值th:value="user.username",而首次user是空的,那么就会空指针异常,所以要有个空判断,而错误信息原本就是在注册失败时就已经保存在了Model对象中了,那么自然可以在转发Forward跳转之后获取uesrnameMsg之类的错误消息了(在网上查,因为Spring MVC默认采取的跳转方式是Forward转发,转发方式下Model的存储的数据不会消失,仍然在一个request Session会话下),具体应该是一个面试题考点:比如**Spring MVC如何传递参数到重定向后的页面?(重定向会丢失会话)**==

###### 输入框样式bootstrap样式保持解释

- `<input type="text" th:class="|form-control ${usernameMsg!=null?'is-invalid':''}|"`
- 还是既包含动态又包含静态的情况,只有在错误注册之后,bootstrap和页面输入框是有一个样式联动的,所以做一个动态的判断,有错误消息,就会加上is-invalid CSS的类选择器

###### 常量接口(项目开发技巧)

- 创建常量接口方便复用,可以让状态数字有含义

- ```java
  package com.nowcoder.community.util;
  
  public interface CommunityConstant {
  
      // 激活账号的三种状态
  
      int ACTIVATION_SUCCESS = 0;
  
      int ACTIVATION_FAILURE = 2;
  
      int ACTIVATION_REPEAT = 1;
  }
  
  ```

- **然后无论是Controller还是Service,哪里要用到这个激活账号所需的常量来判断的话就在这个类实现这个接口即可**

### 2.3 会话管理

#### HTTP Cookie

- HTTP 无状态的 有会话的
- 同一个浏览器访问服务器多次的请求之间是彼此独立的,没有关联的,或者说服务器无法记住浏览器的状态,无法知道你是谁
- cookies解决这个问题
- HTTP Cookie（也叫 Web Cookie 或浏览器 Cookie）是服务器发送到用户浏览器并保存在本地的一小块数据，它会在浏览器下次向同一服务器再发起请求时被携带并发送到服务器上。通常，它用于告知服务端两个请求是否来自同一浏览器，如保持用户的登录状态。Cookie 使基于[无状态](https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview#HTTP_is_stateless_but_not_sessionless)的HTTP协议记录稳定的状态信息成为了可能。
- 通俗来说就是,浏览器请求服务器的时候,服务器创建一个对象(Cookie),并在服务器响应的时候发送给浏览器,cookie对象表面上我们看不见,因为他在响应的头里作为一个参数,之后浏览器就会保存这个数据Cookie,而且会在下次用户请求的时候自动的在请求头里加入这个信息带回给服务器,通过这方式服务器就可以记住用户

#### Cookie测试Demo(AlphaController)

- ```java
      // Cookie相关示例
  
      // 模拟浏览器访问服务器第一次请求,服务器创建Cookie实现
  
      @RequestMapping(path = "/cookie/set" ,method = RequestMethod.GET)
      @ResponseBody
      // 因为返回Cookie需要由Response头携带,所以需要一个Response对象作为参数
      // 测试使用就可以运行项目,访问这个页面,f12查看浏览器network中的set请求,response中就有相关的Set-Cookie
      // 然后还可以测试比如访问index,request header是没有cookie的,而访问有效的/alpha/cookie/get就能查看request header中有了cookie
      // 重新编译项目后也无需先访问set,直接get即可都是保存好的,不会因为重启项目就使cookie消失
      public String getCookie(HttpServletResponse response){
  
          // 创建cookie
          // 必须传入参数,没有无参构造,且一个Cookie对象只能传一组字符串
          Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
          // 设置Cookie生效的范围,只在/community/alpha和其子路径有效
          cookie.setPath("/community/alpha");
          // 浏览器得到cookie会存,默认是存在浏览器的内存中,再次访问就没了,一旦设置生成时间,会放在硬盘中,超出生存时间才会失效
          // 十分钟
          cookie.setMaxAge(60*10);
          response.addCookie(cookie);
  
          return "set cookie";
      }
      @RequestMapping(path = "/cookie/get" ,method = RequestMethod.GET)
      @ResponseBody
      // 有了cookie怎么用,可以服务器要用,取里面的值,也可以模板用
      // 如何在服务端程序得到cookie,可以设置HTTPServletRequest对象参数,然后通过getCookies方法,但这种是直接从宿主获取,cookie数量很多的话还需要遍历从中去找
      // 那么如何获取某一个key的cookie呢? 通过一个注解可以实现@CookieValue("code"),代表从cookie中取key为code的值给这个参数
      public String getCookie(@CookieValue("code") String code){
  
          System.out.println("code = " + code);
          
          return "get Cookie";
      }
  ```

- 













