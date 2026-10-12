package com.project.code.Controller;

import com.project.code.DTO.CombinedRequest; // Assuming a DTO for combined requests
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    // 3. Update Inventory (PUT)
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateInventory(@RequestBody CombinedRequest combinedRequest) {
        Map<String, String> response = new HashMap<>();
        
        Product product = combinedRequest.getProduct();
        Inventory inventory = combinedRequest.getInventory();

        if (product == null || !serviceClass.validateProductId(product.getId())) {
            response.put("message", "Invalid or non-existent product ID.");
            return ResponseEntity.badRequest().body(response);
        }

        Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                product.getId(), 
                inventory.getStore().getId()
        );

        if (existingInventory != null) {
            existingInventory.setStockQuantity(inventory.getStockQuantity());
            inventoryRepository.save(existingInventory);
            response.put("message", "Inventory updated successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "No inventory data available to update.");
            return ResponseEntity.status(404).body(response);
        }
    }

    // 4. Save Inventory (POST)
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();

        boolean isNew = serviceClass.validateInventory(inventory);
        if (!isNew) {
            response.put("message", "Inventory record already exists for this product and store.");
            return ResponseEntity.badRequest().body(response);
        }

        inventoryRepository.save(inventory);
        response.put("message", "Inventory saved successfully.");
        return ResponseEntity.ok(response);
    }

    // 5. Get All Products by Store ID (GET)
    @GetMapping("/store/{storeId}/products")
    public ResponseEntity<Map<String, List<Product>>> getAllProducts(@PathVariable Long storeId) {
        // Fetch inventory list for the store and extract associated products
        List<Inventory> inventories = inventoryRepository.findByStore_Id(storeId);
        List<Product> products = inventories.stream()
                .map(Inventory::getProduct)
                .collect(Collectors.toList());

        Map<String, List<Product>> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // 6. Filter Products by Category and Name (GET)
    @GetMapping("/filter")
    public ResponseEntity<Map<String, List<Product>>> getProductName(
            @RequestParam(required = false, defaultValue = "null") String category,
            @RequestParam(required = false, defaultValue = "null") String name) {
        
        List<Product> products;

        if ("null".equals(category) && !"null".equals(name)) {
            Product product = productRepository.findByName(name);
            products = product != null ? List.of(product) : List.of();
        } else if (!"null".equals(category) && "null".equals(name)) {
            products = productRepository.findByCategory(category);
        } else if (!"null".equals(category) && !"null".equals(name)) {
            products = productRepository.findByCategory(category).stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
        } else {
            products = productRepository.findAll();
        }

        Map<String, List<Product>> response = new HashMap<>();
        response.put("product", products);
        return ResponseEntity.ok(response);
    }

    // 7. Search Products by Name within a Specific Store (GET)
    @GetMapping("/search")
    public ResponseEntity<Map<String, List<Product>>> searchProduct(
            @RequestParam String name, 
            @RequestParam Long storeId) {
        
        List<Product> products = productRepository.findByNameLike(storeId, name);

        Map<String, List<Product>> response = new HashMap<>();
        response.put("product", products);
        return ResponseEntity.ok(response);
    }

    // 8. Remove Product and Related Inventory (DELETE)
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Map<String, String>> removeProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();

        if (!serviceClass.validateProductId(id)) {
            response.put("message", "Product does not exist.");
            return ResponseEntity.status(404).body(response);
        }

        // Delete related inventory first to maintain referential integrity
        inventoryRepository.deleteByProductId(id);
        
        // Delete the product itself
        productRepository.deleteById(id);

        response.put("message", "Product and associated inventory deleted successfully.");
        return ResponseEntity.ok(response);
    }

    // 9. Validate Quantity Availability (GET)
    @GetMapping("/validate-quantity")
    public ResponseEntity<Boolean> validateQuantity(
            @RequestParam Long productId, 
            @RequestParam Long storeId, 
            @RequestParam int requestedQuantity) {
        
        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
        
        if (inventory == null || inventory.getStockQuantity() < requestedQuantity) {
            return ResponseEntity.ok(false);
        }
        
        return ResponseEntity.ok(true);
    }
}
