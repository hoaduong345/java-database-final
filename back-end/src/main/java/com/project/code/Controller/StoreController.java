package com.project.code.Controller;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class StoreController {

    @GetMapping({ "/validate/store/{id}", "/store/validate/{id}" })
    public ResponseEntity<Map<String, Object>> validateStore(@PathVariable("id") Long storeId) {
        boolean exists = storeRepository.existsById(storeId);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", exists);
        response.put("message", exists ? "Store exists." : "Store not found.");
        return exists ? ResponseEntity.ok(response) : ResponseEntity.status(404).body(response);
    }

    @PostMapping("/store/placeOrder")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody PlaceOrderRequestDTO placeOrderRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            orderService.saveOrder(placeOrderRequest);
            response.put("message", "Order placed successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to place order: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderService orderService;

    // 3. Add Store Method (POST)
    @PostMapping("/store")
    public ResponseEntity<Map<String, String>> addStore(@RequestBody Store store) {
        storeRepository.save(store);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Store created successfully.");
        return ResponseEntity.ok(response);
    }

}
