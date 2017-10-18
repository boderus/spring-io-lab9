package com.example.verifierservice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class VerifierServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VerifierServiceApplication.class, args);
	}
}

@Slf4j
@RestController
class VerifierController {

	@RequestMapping(path = "/check", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public VerificationResult check(@RequestBody VerificationRequest request) {
		log.info("GOT REQUEST: {}", request);
		if (request.getAge() >= 20) {
			return new VerificationResult(true);
		} else {
			return new VerificationResult(false);
		}
	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
class VerificationRequest {
	int age;
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
class VerificationResult {
	boolean eligible;
}
