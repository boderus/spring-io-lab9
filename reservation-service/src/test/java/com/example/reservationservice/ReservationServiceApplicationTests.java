package com.example.reservationservice;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReservationServiceApplicationTests {

	@Autowired
	WebApplicationContext applicationContext;

	@Autowired
	ObjectMapper json;

	MockMvc mvc;

	@Before
	public void setUp() throws Exception {
		mvc = webAppContextSetup(applicationContext).build();
	}

	@Test
	public void shouldGetAllReservations() throws Exception {
		ResultActions response = mvc
				.perform(MockMvcRequestBuilders.get("/reservations"))
				.andDo(print())

				.andExpect(status().isOk());

		List<Reservation> reservations = getReservations(response);
		assertThat(reservations).hasSize(9);
		assertThat(reservations).containsOnlyOnce(new Reservation("Adam"));
	}

	private List<Reservation> getReservations(ResultActions response) throws java.io.IOException {
		TypeReference<List<Reservation>> resultType = new TypeReference<List<Reservation>>() {};
		return json.readValue(response.andReturn().getResponse().getContentAsString(), resultType);
	}
}
