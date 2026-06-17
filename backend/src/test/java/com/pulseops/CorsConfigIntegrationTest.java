package com.pulseops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "pulseops.cors-allowed-origins=http://good-origin.com")
public class CorsConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCorsConfig() throws Exception {
        mockMvc.perform(get("/api/health")
                .header("Origin", "http://good-origin.com"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://good-origin.com"));

        mockMvc.perform(get("/api/health")
                .header("Origin", "http://bad-origin.com"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));

    }
    
}
