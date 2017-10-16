package com.example.demo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(DemoApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.run(args);
	}
}

@EnableConfigurationProperties(DemoProps.class)
@RestController
class Hello {

	private final DemoProps props;

	public Hello(DemoProps props) {
		this.props = props;
	}

	@RequestMapping("/hello/{name}")
	String hello(@PathVariable("name") String name) {
		return props.getGreeting() + " " + name;
	}
}

@Data
@ConfigurationProperties("demo")
class DemoProps {

	/** My awesome greeting */
	private String greeting = "Hello";
}
