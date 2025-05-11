package com.siemens.internship;

import com.siemens.internship.Model.Item;
import com.siemens.internship.Repository.ItemRepository;
import com.siemens.internship.Service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {


    // I used @Mock to simulate the repo so i can control responses without accessing a real DB

    @Mock
    private ItemRepository itemRepository;
    // used mocks injection to inject the mocked repository into service automatically
    // this also allows me to get the logic of ItemService isolate, without using real data
    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessItemsAsyncProcessesItemsSuccessfully() throws Exception {
        Item item = new Item(1L, "Item nr1", "Test", "PENDING", "cosmin14@example.com");

        when(itemRepository.findAllIds()).thenReturn(List.of(1L));
          when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
             when(itemRepository.save(any())).thenAnswer(inv -> inv.getArguments()[0]);

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
         List<Item> res= future.get();

        assertEquals(1, res.size());
        assertEquals("PROCESSED", res.get(0).getStatus());
    }
}
