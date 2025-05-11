package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.Controller.ItemController;
import com.siemens.internship.Model.Item;
import com.siemens.internship.Service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    // User MockMVC for simulating HTTP Requests and the controller's endpoints
    @Autowired private MockMvc mockMvc;

    //this helped me to convert Java object to JSON and vice-versa during test
    @Autowired private ObjectMapper objectMapper;

    //I mocked the service layer for isolate controller behavior and control responses for each test
    @MockBean private ItemService itemService;

    private final Item validItem = new Item(1L, "Item", "TExt", "NEW", "test@example.com");

    @Test
    void createItem_Valid_ShouldReturnCreated() throws Exception {
        Mockito.when(itemService.save(any())).thenReturn(validItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isCreated());
    }

    @Test
    void createItem_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        Item invalidItem = new Item(1L, "Item", "TExt", "NEW", "invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_Found_ShouldReturnOk() throws Exception {
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(validItem));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getItemById_NotFound_ShouldReturn404() throws Exception {
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }
}
