package com.example.reservationservice;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationController {

	@GetMapping
	public List<Reservation> list() {
		return Arrays.stream("Konrad,Mariusz,Adam,Michal,Lukasz,Przemek,Adam,Kamil,Marcin,Maciek".split(","))
				.map(Reservation::new)
				.collect(toList());
	}
}

@Data
@AllArgsConstructor
class Reservation {

	String name;
}
