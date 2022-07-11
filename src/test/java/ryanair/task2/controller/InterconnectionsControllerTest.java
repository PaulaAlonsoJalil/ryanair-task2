package ryanair.task2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class InterconnectionsControllerTest {


    @Autowired
    MockMvc mockMvc;

    String departure = "MAD";
    String arrival = "FCO";
    String departureDateTime = "2022-08-27T21:00";
    String arrivalDateTime = "2022-08-27T21:00";


    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getInterconnectionsShouldReturnOk() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/interconnections")
                        .param("departure", departure)
                        .param("arrival", arrival)
                        .param("departureDateTime", departureDateTime)
                        .param("arrivalDateTime", arrivalDateTime)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    public void getInterconnectionsShouldReturn404() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/interconnections")
                        .param("departure", departure)
                        .param("arrival", arrival)
                        .param("departureDateTime", "2022-08-2")
                        .param("arrivalDateTime", arrivalDateTime)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }


}
