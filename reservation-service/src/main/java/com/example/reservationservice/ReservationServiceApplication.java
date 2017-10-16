package com.example.reservationservice;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static javax.persistence.GenerationType.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

	@Autowired
	ReservationRepository reservations;

	@Bean
	public ApplicationRunner init() {
		return args -> {
			long count = Arrays
					.stream("Konrad,Mariusz,Adam,Michal,Lukasz,Przemek,Adam,Kamil,Marcin,Maciek".split(","))
					.map(Reservation::new)
					.map(reservations::save)
					.collect(toList())
					.size();
			log.info("Added {} reservations.", count);
		};
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

}

@Slf4j
@RestController
@RequestMapping("/myreservations")
class ReservationController {

	private Map<String, Reservation> reservations = Arrays
			.stream("Konrad,Mariusz,Adam,Michal,Lukasz,Przemek,Adam,Kamil,Marcin,Maciek".split(","))
			.distinct()
			.map(Reservation::new)
			.collect(Collectors.toMap(Reservation::getName, identity()));


	@GetMapping
	public List<Reservation> list() {
		return reservations.keySet().stream().sorted()
				.map(reservations::get)
				.collect(toList());
	}

	@GetMapping("/{name}")
	public ResponseEntity<?> findOne(@PathVariable("name") String name) {
		return reservations.containsKey(name) ?
				ok(reservations.get(name)) :
				notFound().build();
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody Reservation request) {
		if (reservations.containsKey(request.getName())) {
			throw new AlreadyReserved(request.getName());
		}
		reservations.put(request.getName(), request);
		return created(selfUri(request)).build();
	}

	private URI selfUri(Reservation request) {
		return linkTo(methodOn(ReservationController.class)
				.findOne(request.getName())).toUri();
	}

	@ExceptionHandler
	@ResponseStatus(CONFLICT)
	public ErrorMessage handleAlreadyReserved(AlreadyReserved e) {
		log.debug("Reservation conflict", e);
		return new ErrorMessage(e.getMessage());
	}
}

class AlreadyReserved extends RuntimeException {

	AlreadyReserved(String name) {
		super(String.format("Reservation for %s already exists!", name));
	}
}

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
class Reservation {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private String name;

	Reservation(String name) {
		this.name = name;
	}
}

@Value
@AllArgsConstructor
class ErrorMessage {

	String message;
}
