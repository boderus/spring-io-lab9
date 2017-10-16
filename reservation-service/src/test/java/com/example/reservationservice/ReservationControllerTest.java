package com.example.reservationservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

	public void shouldCreateNewReservation() throws Exception {

	}

	public void shouldNotCreateDuplicatedReservation() throws Exception {

	}
}
