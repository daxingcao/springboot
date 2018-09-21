package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Controller;

import com.example.springboot.view.MainViewClass;

@Controller
@SpringBootApplication
public class SpringbootApplication {

	public static void main(String[] args) {
//		SpringApplication.run(SpringbootApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(SpringbootApplication.class);
	    builder.headless(false).web(false).run(args);
	    MainViewClass.mainView();
	}

}
