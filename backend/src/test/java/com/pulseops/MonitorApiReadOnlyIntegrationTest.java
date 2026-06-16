package com.pulseops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "pulseops.read-only-mode=true")
class MonitorApiReadOnlyIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createMonitorNotAllowedInReadOnlyMode() throws Exception {
        mockMvc.perform(post("/api/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"HTTPBin\",\"url\":\"https://httpbin.org/status/200\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkMonitorNowNotAllowedInReadOnlyMode() throws Exception {
        mockMvc.perform(post("/api/monitors/1/check-now"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMonitorNotAllowedInReadOnlyMode() throws Exception {
        mockMvc.perform(delete("/api/monitors/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMonitorIsAllowedInReadOnlyMode() throws Exception {
        mockMvc.perform(get("/api/monitors"))
                .andExpect(status().isOk());
    }
}
