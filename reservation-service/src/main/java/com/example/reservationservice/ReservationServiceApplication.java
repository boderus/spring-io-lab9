package com.example.reservationservice;

import static java.lang.String.*;
import static java.lang.System.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static javax.persistence.GenerationType.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.lang.String;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.codahale.metrics.MetricRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
@EnableDiscoveryClient
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

@Slf4j
@Component
@RepositoryEventHandler
class ReservationEventHandler {

	private final CounterService counter;

	public ReservationEventHandler(CounterService counter) {
		this.counter = counter;
	}

	@HandleAfterCreate(Reservation.class)
	public void create(Reservation reservation) {
		log.info("Created reservation for {}.", reservation.getName());
		counter.increment("count");
		counter.increment("create");
	}

	@HandleAfterSave(Reservation.class)
	public void save(Reservation reservation) {
		log.info("Updated reservation for {}.", reservation.getName());
		counter.increment("save");
	}

	@HandleAfterDelete(Reservation.class)
	public void delete(Reservation reservation) {
		log.info("Removed reservation for {}.", reservation.getName());
		counter.decrement("count");
		counter.increment("delete");
	}
}

@Slf4j
@Configuration
class ReservationsExtras {

	@Bean
	public ApplicationRunner init(ReservationRepository reservations, MetricRegistry metricRegistry) {
		return args -> {
			long count = Arrays
					.stream("Konrad,Mariusz,Adam,Michal,Lukasz,Przemek,Adam,Kamil,Marcin,Maciek".split(","))
					.map(Reservation::new)
					.map(reservations::save)
					.collect(toList())
					.size();

			metricRegistry.counter("counter.count").inc(count);
			metricRegistry.counter("counter.create").inc(count);

			log.info("Added {} reservations.", count);
		};
	}

	private final Random rng = new Random();

	@Bean
	public HealthIndicator reservationsHealthIndicator() {
		return () -> (rng.nextBoolean() ? Health.up() : Health.down())
				.withDetail("spring", "boot")
				.build();
	}

	@Bean
	public InfoContributor reservationsInfoContributor() {
		return builder -> builder
				.withDetail("currentTime", currentTimeMillis()).build();
	}
}


@Component
class ReservationResourceProcessor implements ResourceProcessor<Resource<Reservation>> {

	@org.springframework.beans.factory.annotation.Value("${info.instanceId}")
	String instanceId;

	@Override
	public Resource<Reservation> process(Resource<Reservation> resource) {
		Reservation reservation = resource.getContent();
		String url = format("https://www.google.pl/search?tbm=isch&q=%s",
				reservation.getName());
		resource.add(new Link(url, "photo"));
		reservation.setName(reservation.getName() + "-" + instanceId);
		return resource;
	}
}

@RepositoryRestResource(path = "/reservations")
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "/byName")
	List<Reservation> findByName(@Param("name") String name);

	@RestResource(exported = false)
	@Override
	void deleteAll();
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
	public Resources<Resource<Reservation>> list() {
		return Resources.wrap(reservations.keySet().stream().sorted()
				.map(reservations::get)
				.collect(toList()));
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
