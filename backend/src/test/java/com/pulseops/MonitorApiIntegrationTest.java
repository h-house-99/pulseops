package com.pulseops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulseops.model.EndpointCheckResult;
import com.pulseops.service.EndpointCheckClient;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class MonitorApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getMonitorsInitiallyReturnsEmptyArray() throws Exception {
		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void postMonitorReturnsCreatedMonitor() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"API Docs\",\"url\":\"https://example.com/status\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("API Docs"))
				.andExpect(jsonPath("$.url").value("https://example.com/status"))
				.andExpect(jsonPath("$.status").value("UNKNOWN"))
				.andExpect(jsonPath("$.lastStatusCode").isEmpty())
				.andExpect(jsonPath("$.lastResponseTimeMs").isEmpty())
				.andExpect(jsonPath("$.lastCheckedAt").isEmpty());
	}

	@Test
	void postMonitorThenGetMonitorsReturnsPersistedMonitor() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Payments\",\"url\":\"https://api.example.com/health\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("Payments"))
				.andExpect(jsonPath("$[0].url").value("https://api.example.com/health"))
				.andExpect(jsonPath("$[0].status").value("UNKNOWN"));
	}

	@Test
	void checkMonitorNowReturnsCheckResult() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monitorId").value(1))
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.statusCode").value(200))
				.andExpect(jsonPath("$.responseTimeMs").exists())
				.andExpect(jsonPath("$.checkedAt").exists())
				.andExpect(jsonPath("$.errorMessage").isEmpty());
	}

	@Test
	void checkMonitorNowReturnsNotFoundForUnknownMonitor() throws Exception {
		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isNotFound());
	}

	@Test
	void checkMonitorIsUpdatedAfterCheck() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("HTTPBin"))
				.andExpect(jsonPath("$[0].url").value("https://httpbin.org/status/200"))
				.andExpect(jsonPath("$[0].status").value("UP"))
				.andExpect(jsonPath("$[0].lastStatusCode").value(200))
				.andExpect(jsonPath("$[0].lastResponseTimeMs").exists())
				.andExpect(jsonPath("$[0].lastCheckedAt").exists());
	}

	@Test
	void getMonitorChecksReturnsCheckHistory() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors/1/checks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].monitorId").value(1))
				.andExpect(jsonPath("$[0].status").value("UP"))
				.andExpect(jsonPath("$[0].statusCode").value(200))
				.andExpect(jsonPath("$[0].responseTimeMs").exists())
				.andExpect(jsonPath("$[0].checkedAt").exists())
				.andExpect(jsonPath("$[0].errorMessage").isEmpty());
	}

	@Test
	void getMonitorChecksReturnsNotFoundForUnknownMonitor() throws Exception {
		mockMvc.perform(get("/api/monitors/1/checks"))
				.andExpect(status().isNotFound());
	}

	@Test
	void checkMonitorNowRecordsDownStatusForServerError() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin Error\",\"url\":\"https://httpbin.org/status/500\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monitorId").value(1))
				.andExpect(jsonPath("$.status").value("DOWN"))
				.andExpect(jsonPath("$.statusCode").value(500))
				.andExpect(jsonPath("$.responseTimeMs").exists())
				.andExpect(jsonPath("$.checkedAt").exists())
				.andExpect(jsonPath("$.errorMessage").isEmpty());
	}

	@Test
	void checkMonitorNowRecordsDownStatusForTimeout() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin Timeout\",\"url\":\"https://httpbin.org/timeout\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monitorId").value(1))
				.andExpect(jsonPath("$.status").value("DOWN"))
				.andExpect(jsonPath("$.statusCode").doesNotExist())
				.andExpect(jsonPath("$.responseTimeMs").exists())
				.andExpect(jsonPath("$.checkedAt").exists())
				.andExpect(jsonPath("$.errorMessage").value("Request timed out"));

		mockMvc.perform(get("/api/monitors/1/checks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].monitorId").value(1))
				.andExpect(jsonPath("$[0].status").value("DOWN"))
				.andExpect(jsonPath("$[0].statusCode").doesNotExist())
				.andExpect(jsonPath("$[0].responseTimeMs").exists())
				.andExpect(jsonPath("$[0].checkedAt").exists())
				.andExpect(jsonPath("$[0].errorMessage").value("Request timed out"));
				
	}

	@TestConfiguration
	static class TestEndpointCheckClientConfiguration {
		@Bean
		@Primary
		EndpointCheckClient endpointCheckClient() {
			return url -> {
				if (url.endsWith("/status/500")) {
					return new EndpointCheckResult(500, null);
				}

				if (url.endsWith("/timeout")) {
					return new EndpointCheckResult(null, "Request timed out");
				}

				return new EndpointCheckResult(200, null);
			};
		}
	}

}
