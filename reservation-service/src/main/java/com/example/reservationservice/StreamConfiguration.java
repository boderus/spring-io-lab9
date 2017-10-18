package com.example.reservationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;

@Configuration
@EnableBinding(ReservationBindings.class)
public class StreamConfiguration {

}

interface ReservationBindings {

	@Input("reservationRequests")
	SubscribableChannel reservationRequests();
}

@Slf4j
@MessageEndpoint
class ReservationServiceActivator {

	@Autowired
	private ReservationRepository reservationRepository;

	@ServiceActivator(inputChannel = "reservationRequests")
	public void createReservation(String reservationName) {
		log.info("Creating reservation from message {}", reservationName);
		reservationRepository.save(new Reservation(reservationName));
	}
}

