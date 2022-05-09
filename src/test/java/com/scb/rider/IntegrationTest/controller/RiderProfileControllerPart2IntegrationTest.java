package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderMannerScoreHistory;
import com.scb.rider.model.document.RiderSuspendHistory;
import com.scb.rider.repository.RiderMannerScoreHistoryRepository;
import com.scb.rider.repository.RiderSuspendHistoryRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderProfileControllerPart2IntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private RiderSuspendHistoryRepository riderSuspendHistoryRepository;
    @Autowired
    private RiderMannerScoreHistoryRepository riderMannerScoreHistoryRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private static RiderSuspendHistory riderSuspendHistory;
    private static RiderMannerScoreHistory riderMannerScoreHistory;
    private static String URL = "/profile";
    private static String RIDER_ID = "RX00000";

    @BeforeAll
    static void setUp() {
        riderSuspendHistory = RiderSuspendHistory.builder()
                .riderId(RIDER_ID)
                .createdDate(LocalDateTime.now())
                .suspensionReason(Arrays.asList("integration test"))
                .createdBy("integration test")
                .build();

        riderMannerScoreHistory = RiderMannerScoreHistory.builder()
                .riderId(RIDER_ID)
                .createdBy("user")
                .createdDate(LocalDateTime.now())
                .actionType("ADD")
                .reason(Arrays.asList("integration test"))
                .additionalComment("integration test")
                .currentScore(9)
                .finalScore(10)
                .actionScore(1)
                .build();

    }

    @Test
    @Order(1)
    void deleteData() {
        Optional<RiderSuspendHistory> suspendHistory = riderSuspendHistoryRepository.findByRiderId(RIDER_ID);
        Optional<RiderMannerScoreHistory> mannerScoreHistory = riderMannerScoreHistoryRepository.findByRiderId(RIDER_ID);
        if (suspendHistory.isPresent() && StringUtils.isNotBlank(suspendHistory.get().getId())) {
            riderSuspendHistoryRepository.delete(suspendHistory.get());
        }
        if (mannerScoreHistory.isPresent() && StringUtils.isNotBlank(mannerScoreHistory.get().getId())) {
            riderMannerScoreHistoryRepository.delete((mannerScoreHistory.get()));
        }
    }

    @Test
    @Order(2)
    void saveSuspendHistory() {
        riderSuspendHistory = riderSuspendHistoryRepository.save(riderSuspendHistory);
    }

    @Test
    @Order(3)
    void saveMannerScoreHistory() {
        riderMannerScoreHistory = riderMannerScoreHistoryRepository.save(riderMannerScoreHistory);
    }

    @Test()
    @Order(4)
    void getRiderSuspendHistoryThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, RIDER_ID, 0, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test()
    @Order(5)
    void getRiderMannerScoreHistoryThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, RIDER_ID, 0, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_747_getRiderSuspendHistoryWhenRequestWithoutRiderIdThenError400() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?&page=%d&size=%d", url, 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Missing servlet request parameter"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("riderId parameter is missing"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_748_getRiderSuspendHistoryWhenRequestRiderIdIsNULLThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=null&page=%d&size=%d", url, 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_749_getRiderSuspendHistoryWhenRequestRiderIdIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, "", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_750_getRiderSuspendHistoryWhenRequestRiderIdIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, " ", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_751_getRiderSuspendHistoryWhenRequestRiderIdIsNotExistThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, "riderNotExist", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_752_getRiderSuspendHistoryWhenRequestPageIsNULLThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=null&size=%d", url, RIDER_ID, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestPageIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, "", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestPageIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, " ", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestPageIsStringThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, "test", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_756_getRiderSuspendHistoryWhenRequestSizeIsNULLThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=null", url, RIDER_ID, 0);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestSizeIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, "");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestSizeIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, " ");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderSuspendHistoryWhenRequestSizeIsStringThenSuccess200() throws Exception {
        String url = URL + "/suspension-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, "test");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_774_getRiderMannerScoreHistoryWhenRequestWithoutRiderIdThenError400() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?page=%d&size=%d", url, 0, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Missing servlet request parameter"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("riderId parameter is missing"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_775_getRiderMannerScoreHistoryWhenRequestRiderIdIsNULLThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=null&page=%d&size=%d", url, 0, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_776_getRiderMannerScoreHistoryWhenRequestRiderIdIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, "", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_777_getRiderMannerScoreHistoryWhenRequestRiderIdIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, " ", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_778_getRiderMannerScoreHistoryWhenRequestRiderIdIsNotExistThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%d", url, "riderNotExist", 1, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("0"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_779_getRiderMannerScoreHistoryWhenRequestPageIsNULLThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=null&size=%d", url, RIDER_ID, 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestPageIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, "", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestPageIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, " ", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestPageIsStringThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%s&size=%d", url, RIDER_ID, "test", 50);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void RDTC_783_getRiderMannerScoreHistoryWhenRequestSizeIsNULLThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=null", url, RIDER_ID, 0);

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestSizeIsEMPTYThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, "");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestSizeIsSPACEThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, " ");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getRiderMannerScoreHistoryWhenRequestSizeIsStringThenSuccess200() throws Exception {
        String url = URL + "/manner-score-history";
        String fullURL = String.format("%s/?riderId=%s&page=%d&size=%s", url, RIDER_ID, 0, "test");

        mockMvc.perform(MockMvcRequestBuilders.get(fullURL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value("1"))
                .andDo(print())
                .andReturn();
    }
}
