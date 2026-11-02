package com.example.insuranceapi;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InsurancePurchaseFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServePurchaseConsole() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("InsuranceAPI Demo Console")
                ));
    }

    @Test
    void shouldCompletePurchaseFlowAndKeepPaymentIdempotent() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        String requestBody = """
                {
                  "productId": 1,
                  "applicant": {
                    "name": "张三",
                    "idCard": "110101199001011234",
                    "phone": "13800138000"
                  },
                  "insuredPerson": {
                    "name": "李四",
                    "idCard": "110101199502021234",
                    "relationship": "SPOUSE"
                  }
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.premium").value(19.90))
                .andExpect(jsonPath("$.coverageAmount").value(100000.00))
                .andReturn();

        String orderNo = JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.orderNo"
        );

        mockMvc.perform(get("/api/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value(orderNo));

        MvcResult paymentResult = mockMvc.perform(
                        post("/api/orders/{orderNo}/pay", orderNo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").value(orderNo))
                .andReturn();

        String policyNo = JsonPath.read(
                paymentResult.getResponse().getContentAsString(),
                "$.policyNo"
        );

        mockMvc.perform(post("/api/orders/{orderNo}/pay", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNo").value(policyNo));

        mockMvc.perform(get("/api/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/policies/{policyNo}", policyNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNo").value(policyNo))
                .andExpect(jsonPath("$.orderNo").value(orderNo));
    }

    @Test
    void shouldReturnConsistentClientErrors() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        String invalidRequest = """
                {
                  "productId": 1,
                  "applicant": {
                    "name": "张三",
                    "idCard": "110101199001011234",
                    "phone": "123"
                  },
                  "insuredPerson": {
                    "name": "李四",
                    "idCard": "110101199502021234",
                    "relationship": "SPOUSE"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("applicant.phone: 投保人手机号格式不正确"));
    }
}
