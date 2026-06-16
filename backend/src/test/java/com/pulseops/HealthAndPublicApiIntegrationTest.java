package com.pulseops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulseops.config.CuratedMonitorDefinition;

@AutoConfigureMockMvc
@SpringBootTest
class HealthAndPublicApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthReturnsOk() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void publicApisReturnsCuratedApis() throws Exception {
		mockMvc.perform(get("/api/public-apis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(CuratedMonitorDefinition.all().size()))
				.andExpect(jsonPath("$[0].name").value("GitHub API"))
				.andExpect(jsonPath("$[0].url").value("https://api.github.com"))
				.andExpect(jsonPath("$[1].name").value("OpenAI status"))
				.andExpect(jsonPath("$[1].url").value("https://status.openai.com/api/v2/status.json"))
				.andExpect(jsonPath("$[2].name").value("Discord status"))
				.andExpect(jsonPath("$[2].url").value("https://discordstatus.com/api/v2/status.json"));
	}

}
