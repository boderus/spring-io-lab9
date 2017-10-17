package com.example.reservationclient;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}


	@Bean
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

@FeignClient(name = "reservationservice/reservations")
interface ReservationsClient {

	@GetMapping
	Resources<Reservation> findAll();
}

@Slf4j
@RestController
class ReservationsController {

	private final RestTemplate rest;
	private final ReservationsClient client;

	public ReservationsController(RestTemplate rest, ReservationsClient client) {
		this.rest = rest;
		this.client = client;
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
		return client.findAll().getContent().stream()
				.map(Reservation::getName)
				.collect(toList());
	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
}
