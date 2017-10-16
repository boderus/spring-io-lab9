package com.example.reservationservice;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

	@Autowired
	MockMvc mvc;

	@Test
	public void shouldFindExistingReservation() throws Exception {
		mvc.perform(get("/reservations/{name}", "Adam"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("@.name").value("Adam"));
	}

	@Test
	public void shouldNotFindNotExistingReservation() throws Exception {
		mvc.perform(get("/reservations/{name}", "test"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

	@Autowired
	ObjectMapper json;

	@Test
	public void shouldCreateNewReservation() throws Exception {
		mvc.perform(post("/reservations")
			.contentType(APPLICATION_JSON_UTF8)
			.content(json.writeValueAsString(new Reservation("John"))))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "http://localhost/reservations/John"));
	}

	@Test
	public void shouldNotCreateDuplicatedReservation() throws Exception {
		mvc.perform(post("/reservations")
			.contentType(APPLICATION_JSON_UTF8)
			.content(json.writeValueAsString(new Reservation("Adam"))))
			.andDo(print())
			.andExpect(status().isConflict())
			.andExpect(jsonPath("@.message").value("Reservation for Adam already exists!"));
	}
}
