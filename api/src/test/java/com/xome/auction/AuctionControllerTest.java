package com.xome.auction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingReturnsOk() throws Exception {
        mockMvc.perform(get("/api/auction/ping"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void statusReturnsDefaultAuctionState() throws Exception {
        mockMvc.perform(get("/api/auction/status"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.currentPrice").value(250000))
               .andExpect(jsonPath("$.leadingBidder").isEmpty())
               .andExpect(jsonPath("$.reserveMet").value(false));
    }

    @Test
    void placingBidAboveCurrentPriceIsAccepted() throws Exception {
        mockMvc.perform(post("/api/auction/bids")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"bidderId\":\"bidder-1\",\"amount\":260000,\"requestId\":\"r-1\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.accepted").value(true))
               .andExpect(jsonPath("$.status.leadingBidder").value("bidder-1"))
               .andExpect(jsonPath("$.status.currentPrice").value(250000));
    }

    @Test
    void priceMovesToBeatTheNextHighestMaximum() throws Exception {
        mockMvc.perform(post("/api/auction/bids")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"bidderId\":\"alice\",\"amount\":300000,\"requestId\":\"r-1\"}"));

        mockMvc.perform(post("/api/auction/bids")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"bidderId\":\"bob\",\"amount\":280000,\"requestId\":\"r-2\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.accepted").value(true))
               .andExpect(jsonPath("$.status.leadingBidder").value("alice"))
               .andExpect(jsonPath("$.status.currentPrice").value(285000));
    }
}
