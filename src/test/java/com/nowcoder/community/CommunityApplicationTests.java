package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.AlphaDaoHibernateImpl;
import com.nowcoder.community.dao.AlphaDaoMybatisImpl;
import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.Date;

//该测试类目前主要为了测试Spring的各种特性,入门学习,学习Spring的控制反转和容器的运行

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
//同时我们又知道IoC的核心是Spring容器,而容器又是被自动创建的,那么如何得到这个容器呢?
//那就是实现这个接口ApplicationContextAware,ApplicationContext接口实际就是Spring容器,这个接口向上继承自HierarchicalBeanFactory,然后继承自BeanFactory
//也就是说,如果一个类实现了ApplicationContextAware接口的setApplicationContext方法,那么Spring容器就可以检测到他,Spring容器在扫描组件时,会扫描到这个bean,调用setApplicationContext方法,把这对象作为bean传入进去,保存下来
public class CommunityApplicationTests implements ApplicationContextAware {

	//添加一个成员变量记录下来Spring容器
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

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
		System.out.println(alphaDao);
		System.out.println(alphaDao.select());
//		alphaDao = applicationContext.getBean(AlphaDaoHibernateImpl.class);
		alphaDao = applicationContext.getBean("alphaHibernate",AlphaDao.class);
		System.out.println(alphaDao.select());
	}

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
}
