package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	// 用于管理bean的生命周期的
	// 主要用于管理bean的初始化方法
	// 由这个注解所修饰的方法,会在这个构造器调用之后被执行
	// 所以在这里处理ES和Redis的冲突是很合理的,意味这个bean是最早加载的
	@PostConstruct
	public void init(){
		// 解决Netty启动冲突问题
		// 从Netty4Utils和NettyRuntime理解这个异常
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
