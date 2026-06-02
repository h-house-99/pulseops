package com.pulseops;

import java.util.concurrent.atomic.AtomicInteger;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulseops.model.EndpointCheckResult;
import com.pulseops.service.EndpointCheckClient;
import com.pulseops.service.MonitorService;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class MonitorApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MonitorService monitorService;

	@Test
	void getMonitorsInitiallyReturnsEmptyArray() throws Exception {
		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getMonitorsReturnsMonitors() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"API Docs\",\"url\":\"https://example.com/status\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].name").value("API Docs"))
				.andExpect(jsonPath("$[0].url").value("https://example.com/status"));
	}

	@Test
	void checkAllMonitorsChecksAllMonitors() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"API Docs\",\"url\":\"https://example.com/status\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"API Docs 2\",\"url\":\"https://example.com/status/500\"}"))
			.andExpect(status().isOk());

		monitorService.checkAllMonitors();

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("API Docs"))
				.andExpect(jsonPath("$[0].status").value("UP"))
				.andExpect(jsonPath("$[0].lastStatusCode").value(200))
				.andExpect(jsonPath("$[1].name").value("API Docs 2"))
				.andExpect(jsonPath("$[1].status").value("DOWN"))
				.andExpect(jsonPath("$[1].lastStatusCode").value(500));

		mockMvc.perform(get("/api/monitors/1/checks/recent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("UP"))
				.andExpect(jsonPath("$[0].statusCode").value(200));

		mockMvc.perform(get("/api/monitors/2/checks/recent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("DOWN"))
				.andExpect(jsonPath("$[0].statusCode").value(500));
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
	void postMonitorReturnsBadRequestWhenNameIsBlank() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"url\":\"https://example.com/status\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postMonitorReturnsBadRequestWhenUrlIsInvalid() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"API Docs\",\"url\":\"invalid-url\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postMonitorReturnsBadRequestWhenUrlIsBlank() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"API Docs\",\"url\":\"\"}"))
				.andExpect(status().isBadRequest());
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

		mockMvc.perform(get("/api/monitors/1/checks/recent"))
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
	void getMonitorChecksReturnsLengthFiveAfterSixChecks() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors/1/checks/recent"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(5))
				.andExpect(jsonPath("$[0].id").value(6))
				.andExpect(jsonPath("$[0].monitorId").value(1))
				.andExpect(jsonPath("$[1].id").value(5))
				.andExpect(jsonPath("$[1].monitorId").value(1));
	}

	@Test
	void getMonitorChecksReturnsNotFoundForUnknownMonitor() throws Exception {
		mockMvc.perform(get("/api/monitors/1/checks/recent"))
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

		mockMvc.perform(get("/api/monitors/1/checks/recent"))
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

	@Test
	void getMonitorsReturnsEmptySummaryStatsForNewMonitor() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].totalChecks").value(0))
				.andExpect(jsonPath("$[0].uptimePercentage").doesNotExist())
				.andExpect(jsonPath("$[0].averageResponseTimeMs").isEmpty())
				.andExpect(jsonPath("$[0].fastestResponseTimeMs").isEmpty())
				.andExpect(jsonPath("$[0].slowestResponseTimeMs").isEmpty())
				.andExpect(jsonPath("$[0].latestErrorMessage").isEmpty())
				.andExpect(jsonPath("$[0].lastFailureAt").isEmpty());
	}

	@Test
	void getMonitorsReturnsSummaryStatsAfterSuccessfulCheck() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());			
				
		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].totalChecks").value(1))
				.andExpect(jsonPath("$[0].uptimePercentage").value(100))
				.andExpect(jsonPath("$[0].averageResponseTimeMs").exists())
				.andExpect(jsonPath("$[0].fastestResponseTimeMs").exists())
				.andExpect(jsonPath("$[0].slowestResponseTimeMs").exists())
				.andExpect(jsonPath("$[0].latestErrorMessage").isEmpty())
				.andExpect(jsonPath("$[0].lastFailureAt").isEmpty());
	}

	@Test
	void getMonitorsReturnsLatestErrorMessageAndLastFailureAtAfterTimeout() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/timeout\"}"))
				.andExpect(status().isOk());				
				
		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].latestErrorMessage").value("Request timed out"))
				.andExpect(jsonPath("$[0].lastFailureAt").exists());
	}

	@Test
	void getMonitorsReturnsLastFailureAtWithoutLatestErrorMessageAfterHttpFailure() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/500\"}"))
				.andExpect(status().isOk());				
				
		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].latestErrorMessage").isEmpty())
				.andExpect(jsonPath("$[0].lastFailureAt").exists());
	}

	@Test
	void getMonitorsReturnsCorrectUptimeAfterFlakyChecks() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/flaky\"}"))
				.andExpect(status().isOk());			
		
		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].totalChecks").value(2))
				.andExpect(jsonPath("$[0].lastFailureAt").exists())
				.andExpect(jsonPath("$[0].uptimePercentage").value(50));
	}

	@Test
	void deleteMonitorRemovesMonitorAndCheckResults() throws Exception {
		mockMvc.perform(post("/api/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/monitors/1/check-now"))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/monitors/1"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/monitors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(get("/api/monitors/1/checks/recent"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteMonitorReturnsNotFoundForUnknownMonitor() throws Exception {
		mockMvc.perform(delete("/api/monitors/1"))
				.andExpect(status().isNotFound());
	}

	@TestConfiguration
	static class TestEndpointCheckClientConfiguration {
		@Bean
		@Primary
		EndpointCheckClient endpointCheckClient() {
			AtomicInteger checkCount = new AtomicInteger(0);
			return url -> {
				if (url.endsWith("/flaky")) {
					int count = checkCount.incrementAndGet();
					if (count == 1) {
						return new EndpointCheckResult(200, null);
					}
					return new EndpointCheckResult(500, null);
				}
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
