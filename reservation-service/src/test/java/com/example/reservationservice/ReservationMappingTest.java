package com.example.reservationservice;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@JsonTest
public class ReservationMappingTest {

	@Autowired
	JacksonTester<Reservation> json;

	@Test
	public void shouldSerializeReservation() throws Exception {
		Reservation reservation = new Reservation("John");

		JsonContent<Reservation> result = json.write(reservation);

		assertThat(result).extractingJsonPathStringValue("@.name")
				.isEqualTo("John");
	}

	@Test
	public void shouldDeserializeReservation() throws Exception {
		String data = "{\"name\":\"Jane\"}";

		ObjectContent<Reservation> result = json.parse(data);

		assertThat(result.getObject().getName()).isEqualTo("Jane");
	}
}
