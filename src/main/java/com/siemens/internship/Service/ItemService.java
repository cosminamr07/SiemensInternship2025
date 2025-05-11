package com.siemens.internship.Service;

import com.siemens.internship.Model.Item;
import com.siemens.internship.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    // Deleted the list with processedItems and counter for processed
    /**

      Problems/Errors in the original implementation:
        - returned List<Item> directly, before async tasks was finished...always incomplete
        - used shared mutable fields (processedItems & processedCount) -> is not thread-safe
        - exceptions were caught and only printed
        - used static ExecutorService instead of Spring's task abstraction
        - marked with @Async but still returned a synchronous List<Item>


         Fixes applied:
        - changed return type to CompletableFuture<List<Item>> for Spring to handle it async
        - used local thread-safe list (CopyOnWriteArrayList) instead of shared fields
        - I composed tasks using CompletableFuture.allOf to ensure all finish before returning
        - removed static mutable state soo all state is now local for method scope
     */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        //fetched all item Ids to process them asynchronously by Id instead of Loading all in memory
        List<Long> itemIds = itemRepository.findAllIds();

        //used a thread-safe list to store results( for threads to write to it)
        List<Item> successfullyProcessed = new CopyOnWriteArrayList<>();

            // here I created futures for each async task...I can wait for all to complete later
            List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);
                        // fetched & updated the item safely inside the async task
                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if (optionalItem.isPresent() ) {
                        Item item = optionalItem.get();

                        item.setStatus("PROCESSED");
                        Item saved = itemRepository.save(item);
                        successfullyProcessed.add(saved);
                    }

                } catch (Exception e) {
                    System.err.println("Failed to process item with id " + id + ": " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }
        // I waited for all async task to be completed and then returned the processed Item List
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> successfullyProcessed);
    }    }


