package com.example.reservationclient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
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
	@Autowired MockMvc mvc;

	@Test
	public void should_allow_reservation_if_old_enough() throws Exception {

	}

	@Test
	public void should_prevent_reservation_if_too_young() throws Exception {

	}

}
