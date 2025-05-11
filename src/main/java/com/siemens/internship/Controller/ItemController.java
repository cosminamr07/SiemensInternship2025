package com.siemens.internship.Controller;

import com.siemens.internship.Service.ItemService;
import com.siemens.internship.Model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;


    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.findAll();
        return new ResponseEntity<>(items,HttpStatus.OK);
    }
    // In this createItem method I check that if result expected has errors
    // it returns bad_request, otherwise it returns Created

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Item itemSaved = itemService.save(item);
        return new ResponseEntity<>(itemSaved, HttpStatus.CREATED);
    }

        // Here added OptionalItem and returned OK status if the item is found OR 404 if isn t found
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Optional<Item> item = itemService.findById(id);
        return item.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // First time I check if the item exists..if it does, I set the id to make sure we update the correct one
    // else I return NOT Found ( updating a non-existing item should not be successfully)
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
                item.setId(id);
            return new
                    ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //Here instead of Conflict I switched with NO_Content -> indicate that the deletion was successful without returning

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    // Since the method is ASYNC, i used .get() to block & wait for proccessing to finish..I wrapped this in a try-catch block
    // and return INTERNAL_ERROR if smthing goes wrong else OK for success
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            List<Item> items = itemService.processItemsAsync().get();
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
