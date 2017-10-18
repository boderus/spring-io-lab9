package com.example.reservationclient;

import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
@EnableBinding(ReservationBindings.class)
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}


	@Bean
	@ConditionalOnProperty(name = "spring.cloud.discovery.enabled", havingValue = "true", matchIfMissing = true)
	public ApplicationRunner discoveryClientDemo(DiscoveryClient discovery) {
		return args -> {
			try {
				log.info("------------------------------");
				log.info("DiscoveryClient Example");

				discovery.getInstances("reservationservice").forEach(instance -> {
					log.info("Reservation service: ");
					log.info("  ID: {}", instance.getServiceId());
					log.info("  URI: {}", instance.getUri());
					log.info("  Meta: {}", instance.getMetadata());
				});

				log.info("------------------------------");
			} catch (Exception e) {
				log.error("DiscoveryClient Example Error!", e);
			}
		};
	}

	@Bean @LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

interface ReservationBindings {

	@Output("createReservation")
	MessageChannel createReservation();
}

@FeignClient(name = "verifierservice")
interface VerifierClient {

	@PostMapping(path = "/check", consumes = APPLICATION_JSON_UTF8_VALUE)
	VerifierResponse check(@RequestBody VerifierRequest request);
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class VerifierRequest {
	int age;
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class VerifierResponse {
	boolean eligible;
}


@NoArgsConstructor
@AllArgsConstructor
@Data
class ReservationRequest {

	String name;
	int age;

}

@FeignClient(name = "reservationservice", path = "/reservations", fallback = ReservationsFallback.class)
interface ReservationsClient {

	@GetMapping
	Resources<Reservation> findAll();
}

@Component
class ReservationsFallback implements ReservationsClient {

	@Override
	public Resources<Reservation> findAll() {
		return new Resources<>(Stream.of("This", "is", "fallback")
				.map(Reservation::new).collect(toList()));
	}
}

@Slf4j
@RestController
class ReservationsController {

	private final RestTemplate rest;
	private final ReservationsClient reservations;
	private final VerifierClient verifier;

	public ReservationsController(RestTemplate rest, ReservationsClient reservations,
								  VerifierClient verifier) {
		this.rest = rest;
		this.reservations = reservations;
		this.verifier = verifier;
	}

	@GetMapping("/names")
	public List<String> names() {
		log.info("Calling names...");

		ParameterizedTypeReference<Resources<Reservation>> responseType =
				new ParameterizedTypeReference<Resources<Reservation>>() {};

		ResponseEntity<Resources<Reservation>> result = rest.exchange(
				"http://reservationservice/reservations",
				HttpMethod.GET,
				null,
				responseType);

		return result.getBody().getContent().stream()
				.map(Reservation::getName).collect(Collectors.toList());
	}

	@GetMapping("/feign-names")
	public List<String> feignNames() {
		log.info("Calling feign-names...");
		return reservations.findAll().getContent().stream()
				.map(Reservation::getName)
				.collect(toList());
	}

	@Autowired
	private ReservationBindings bindings;

	@PostMapping
	public ResponseEntity<?> create(@RequestBody ReservationRequest request) {
		log.info("Calling create reservation...");
		VerifierResponse response = verifier.check(new VerifierRequest(request.age));
		if (response.eligible) {
			Message<String> message = MessageBuilder.withPayload(request.getName()).build();
			bindings.createReservation().send(message);
			return ResponseEntity.status(CREATED).build();
		} else {
			return ResponseEntity.status(EXPECTATION_FAILED).build();
		}
	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
}
