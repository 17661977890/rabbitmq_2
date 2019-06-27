package com.debug.steadyjack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@MapperScan(basePackages = "com.debug.steadyjack.mapper")
@ImportResource(locations = {"classpath:spring/spring-jdbc.xml"})
public class SpringbootRabbitmq01Application extends SpringBootServletInitializer{

	@Bean
	public ObjectMapper objectMapper(){
		ObjectMapper objectMapper=new ObjectMapper();
		return objectMapper;
	}

	//不使用spring boot内嵌tomcat启动方式
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SpringbootRabbitmq01Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootRabbitmq01Application.class, args);
	}
}
