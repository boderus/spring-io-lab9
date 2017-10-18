package com.example.reservationclient;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
//		"spring.cloud.discovery.enabled=false",
//		"spring.cloud.config.enabled=false",
//		"spring.cloud.config.discovery.enabled=false",
//		"ribbon.eureka.enabled=false"
//})
//@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebMvcTest(ReservationsController.class)
public class ReservationAgeVerificationTest {

	@MockBean RestTemplate rest;
	@MockBean ReservationsClient client;
	@Autowired ObjectMapper json;
	@Autowired MockMvc mvc;

	@Test
	public void should_allow_reservation_if_old_enough() throws Exception {
		ReservationRequest jane = new ReservationRequest("Jane", 25);

		mvc.perform(post("/")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json.writeValueAsString(jane)))
			.andDo(print())
			.andExpect(status().isCreated());
	}

	@Test
	public void should_prevent_reservation_if_too_young() throws Exception {
		ReservationRequest john = new ReservationRequest("John", 17);

		mvc.perform(post("/")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json.writeValueAsString(john)))
			.andDo(print())
			.andExpect(status().isExpectationFailed());
	}
}
