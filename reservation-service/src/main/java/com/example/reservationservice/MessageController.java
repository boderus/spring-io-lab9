package com.example.reservationservice;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RestController
@EnableConfigurationProperties(TestProps.class)
public class MessageController {

//	@Value("${test.message}")
//	public String msg;
//
//	@GetMapping("/test")
//	public String saySth() {
//		return msg;
//	}


//	@Autowired
//	public Environment env;
//
//	@GetMapping("/test")
//	public String saySth() {
//		return "reload: " + env.getRequiredProperty("test.message");
//	}

	@Autowired
	public TestProps props;

	@GetMapping("/test")
	public String saySth() {
		return "reload: " + props.message;
	}

}

@Data
@ConfigurationProperties("test")
class TestProps {

	String message;
}
