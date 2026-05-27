package com.yzh.campushub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("com.yzh.campushub.mapper")
@EnableAsync
@SpringBootApplication
public class CampusHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusHubApplication.class, args);
	}

}
