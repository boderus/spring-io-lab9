package com.example.reservationclient;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ConfigurationBasedServerList;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.discovery.enabled=false",
		"ribbon.eureka.enabled=false",
		"verifierservice.ribbon.listOfServers=127.0.0.1:9989"
})
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
		workOffline = true,
		ids = "com.example:verifier-service:+:stubs:9989"
)
//@ActiveProfiles("test")
//@WebMvcTest(ReservationsController.class)
public class ReservationAgeVerificationTest {

//	@MockBean RestTemplate rest;
//	@MockBean ReservationsClient reservations;
//	@MockBean VerifierClient verifier;
	@Autowired ObjectMapper json;
	@Autowired MockMvc mvc;

	@Test
	public void should_allow_reservation_if_old_enough() throws Exception {
		ReservationRequest jane = new ReservationRequest("Jane", 25);
//		when(verifier.check(new VerifierRequest(25)))
//				.thenReturn(new VerifierResponse(true));

		mvc.perform(post("/")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json.writeValueAsString(jane)))
			.andDo(print())
			.andExpect(status().isCreated());
	}

	@Test
	public void should_prevent_reservation_if_too_young() throws Exception {
		ReservationRequest john = new ReservationRequest("John", 17);
//		when(verifier.check(new VerifierRequest(17)))
//				.thenReturn(new VerifierResponse(false));

		mvc.perform(post("/")
				.contentType(APPLICATION_JSON_UTF8)
				.content(json.writeValueAsString(john)))
			.andDo(print())
			.andExpect(status().isExpectationFailed());
	}

	@RibbonClients({
		@RibbonClient(name = "verifierservice", configuration = RibbonClientTestConfig.class)
	})
	@TestConfiguration
	public static class RibbonTestConfig {
		@Bean
		public ServerList<Server> ribbonServerList(IClientConfig clientConfig) {
			ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
			serverList.initWithNiwsConfig(clientConfig);
			return serverList;
		}
	}
}

@TestConfiguration
class RibbonClientTestConfig {
	@Bean
	public ServerList<Server> ribbonServerList(IClientConfig clientConfig) {
		ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
		serverList.initWithNiwsConfig(clientConfig);
		return serverList;
	}
}
