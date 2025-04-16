package empik.complaints_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import empik.complaints_service.controller.dto.ComplaintResponse;
import empik.complaints_service.repository.ComplaintRepository;
import empik.complaints_service.service.ClockService;
import empik.complaints_service.service.country.CountryProviderService;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.Base64;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComplaintControllerTest {

	private static final String EXAMPLE_PL_IP = "196.247.180.132";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ComplaintRepository complaintRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ClockService clockService;

	@MockitoBean
	private CountryProviderService countryProviderService;

	@BeforeEach
	void setup() {
		complaintRepository.deleteAll();
		when(countryProviderService.getCountry(EXAMPLE_PL_IP)).thenReturn("pl");
	}

	@Test
	void createsComplaint() throws Exception {
		// when
		when(clockService.getClock()).thenReturn(tenthOfApril2025h1200());
		// create complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"my complaint! ą,ć,ę,ł,Ü,ß\", \"productId\":\"1\"}"))
				.andExpect(status().isOk());

		// then
		mockMvc.perform(get("/complaint")
						.header("Authorization", buildAuthHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].productId").value(1))
				.andExpect(jsonPath("$[0].content").value("my complaint! ą,ć,ę,ł,Ü,ß"))
				.andExpect(jsonPath("$[0].complaintant").value("user"))
				.andExpect(jsonPath("$[0].creationTimestamp").value("2025-04-10T12:00:00+02:00"))
				.andExpect(jsonPath("$[0].country").value("pl"))
				.andExpect(jsonPath("$[0].counter").value("1"));
	}

	@Test
	void createsDuplicateComplaint() throws Exception {
		// when
		when(clockService.getClock()).thenReturn(tenthOfApril2025h1200());
		// create complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"my complaint!\", \"productId\":\"1\"}"))
				.andExpect(status().isOk());

		when(clockService.getClock()).thenReturn(tenthOfApril2025h1201());
		// create duplicate complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"duplicate\", \"productId\":\"1\"}"))
				.andExpect(status().isOk());

		// then
		mockMvc.perform(get("/complaint")
						.header("Authorization", buildAuthHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].productId").value(1))
				.andExpect(jsonPath("$[0].content").value("my complaint!"))
				.andExpect(jsonPath("$[0].complaintant").value("user"))
				.andExpect(jsonPath("$[0].creationTimestamp").value("2025-04-10T12:00:00+02:00"))
				.andExpect(jsonPath("$[0].country").value("pl"))
				.andExpect(jsonPath("$[0].counter").value("2"));
	}

	@Test
	void createsTwoDifferentComplaints() throws Exception {
		// when
		when(clockService.getClock()).thenReturn(tenthOfApril2025h1200());
		// create complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"my complaint!\", \"productId\":\"1\"}"))
				.andExpect(status().isOk());

		when(clockService.getClock()).thenReturn(tenthOfApril2025h1201());
		// create different complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"my other complaint\", \"productId\":\"2\"}"))
				.andExpect(status().isOk());

		// then
		mockMvc.perform(get("/complaint")
						.header("Authorization", buildAuthHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].productId").value(2))
				.andExpect(jsonPath("$[0].content").value("my other complaint"))
				.andExpect(jsonPath("$[0].complaintant").value("user"))
				.andExpect(jsonPath("$[0].creationTimestamp").value("2025-04-10T12:01:00+02:00"))
				.andExpect(jsonPath("$[0].country").value("pl"))
				.andExpect(jsonPath("$[0].counter").value("1"))
				.andExpect(jsonPath("$[1].productId").value(1))
				.andExpect(jsonPath("$[1].content").value("my complaint!"))
				.andExpect(jsonPath("$[1].complaintant").value("user"))
				.andExpect(jsonPath("$[1].creationTimestamp").value("2025-04-10T12:00:00+02:00"))
				.andExpect(jsonPath("$[0].country").value("pl"))
				.andExpect(jsonPath("$[1].counter").value("1"));
	}

	@Test
	void failsWith4xxWhenTryingToCreateComplaintThatIsTooLong() throws Exception {
		// when
		// complaint is too long
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"" + Strings.repeat("a", 1025) + "\", \"productId\":\"1\"}"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void updatesComplaint() throws Exception {
		// given
		when(clockService.getClock()).thenReturn(tenthOfApril2025h1200());
		// create complaint
		mockMvc.perform(post("/complaint")
						.header("Authorization", buildAuthHeader())
						.header("X-Forwarded-For", EXAMPLE_PL_IP)
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"my complaint!\", \"productId\":\"1\"}"))
				.andExpect(status().isOk());

		// when
		// update complaint
		mockMvc.perform(put("/complaint/" + getComplaintId())
						.header("Authorization", buildAuthHeader())
						.contentType(APPLICATION_JSON)
						.content("{\"content\":\"updated complaint\"}"))
				.andExpect(status().isOk());

		//then
		mockMvc.perform(get("/complaint")
						.header("Authorization", buildAuthHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].productId").value(1))
				.andExpect(jsonPath("$[0].content").value("updated complaint"))
				.andExpect(jsonPath("$[0].complaintant").value("user"))
				.andExpect(jsonPath("$[0].creationTimestamp").value("2025-04-10T12:00:00+02:00"))
				.andExpect(jsonPath("$[0].country").value("pl"))
				.andExpect(jsonPath("$[0].counter").value("1"));
	}

	private Long getComplaintId() throws Exception {
		var responseBody = mockMvc.perform(get("/complaint")
						.header("Authorization", buildAuthHeader()))
				.andReturn().getResponse().getContentAsString();
		var complaints = objectMapper.readValue(responseBody, ComplaintResponse[].class);
		Assertions.assertThat(complaints).hasSize(1);
		return complaints[0].getId();
	}

	private static Clock tenthOfApril2025h1200() {
		return Clock.fixed(
				ZonedDateTime.of(
						LocalDateTime.of(2025, Month.APRIL, 10, 12, 0),
						ZoneId.systemDefault()
				).toInstant(),
				ZoneId.systemDefault());
	}

	private static Clock tenthOfApril2025h1201() {
		return Clock.fixed(
				ZonedDateTime.of(
						LocalDateTime.of(2025, Month.APRIL, 10, 12, 1),
						ZoneId.systemDefault()
				).toInstant(),
				ZoneId.systemDefault());
	}

	private static String buildAuthHeader() {
		return "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
	}
}
