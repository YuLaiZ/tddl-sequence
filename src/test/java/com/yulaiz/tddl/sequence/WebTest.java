package com.yulaiz.tddl.sequence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class WebTest {
    @Autowired
    private MockMvc mockMvc;
    private static final String sequenceName = "sequenceName";
    private static final String sequenceNameValue = "tddl_test_seq";

    @Test
    void getHttpSuccess() throws Exception {
        String response =
                mockMvc.perform(
                                MockMvcRequestBuilders.post("/rest-inner-api/v1/nextValue")
                                        .queryParam(sequenceName, sequenceNameValue)
                        )
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        log.debug("response:{}", response);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String code = jsonNode.get("code").asText();
        String data = jsonNode.get("data").asText();
        Assertions.assertEquals("0", code);
        Assertions.assertDoesNotThrow(() -> {
            Integer.parseInt(data);
        }, "data is not a number: " + data);
    }

    @Test
    void getHttpFails() throws Exception {
        String response =
                mockMvc.perform(
                                MockMvcRequestBuilders.post("/rest-inner-api/v1/nextValue")
                                        .queryParam(sequenceName, "")
                        )
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        log.debug("response:{}", response);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String code = jsonNode.get("code").asText();
        Assertions.assertNotEquals("0", code);
    }

    @Test
    void getHttpListSuccess() throws Exception {
        int step = 25;
        String response =
                mockMvc.perform(
                                MockMvcRequestBuilders.post("/rest-inner-api/v1/nextValueList")
                                        .queryParam(sequenceName, sequenceNameValue)
                                        .queryParam("step", String.valueOf(step))
                        )
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        log.debug("response:{}", response);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String code = jsonNode.get("code").asText();
        JsonNode data = jsonNode.get("data");
        Assertions.assertTrue(data.isArray(), "data is not an array");
        List<String> list = new ArrayList<>();
        for (JsonNode seq : data) {
            list.add(seq.asText());
        }
        Assertions.assertEquals("0", code);
        for (String seq : list) {
            Assertions.assertDoesNotThrow(() -> {
                Integer.parseInt(seq);
            }, "seq is not a number: " + seq);
        }
        Assertions.assertEquals(step, new HashSet<>(list).size());
    }
}
