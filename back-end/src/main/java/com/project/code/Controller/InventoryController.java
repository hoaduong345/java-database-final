package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
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
    private StoreRepository storeRepository;

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
                inventory.getStore().getId());

        if (existingInventory != null) {
            existingInventory.setStockLevel(inventory.getStockLevel());
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

    @GetMapping("/filter/{category}/{name}/{storeId}")
    public ResponseEntity<Map<String, Object>> filterInventory(
            @PathVariable String category,
            @PathVariable String name,
            @PathVariable Long storeId) {
        Map<String, Object> response = new HashMap<>();
        if (storeId == null || storeId <= 0 || category.isBlank() || name.isBlank()) {
            response.put("message", "Category, name, and a positive store ID are required.");
            return ResponseEntity.badRequest().body(response);
        }
        if (!storeRepository.existsById(storeId)) {
            response.put("message", "Store not found with ID: " + storeId);
            return ResponseEntity.status(404).body(response);
        }

        boolean allCategories = "null".equalsIgnoreCase(category);
        boolean allNames = "null".equalsIgnoreCase(name);
        List<Inventory> inventories = inventoryRepository.findByStore_Id(storeId).stream()
                .filter(inventory -> inventory.getProduct() != null)
                .filter(inventory -> allCategories
                        || category.equalsIgnoreCase(inventory.getProduct().getCategory()))
                .filter(inventory -> allNames || (inventory.getProduct().getName() != null
                        && inventory.getProduct().getName().equalsIgnoreCase(name)))
                .collect(Collectors.toList());
        response.put("inventory", inventories);
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
    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public ResponseEntity<Map<String, Object>> validateQuantity(
            @PathVariable int quantity,
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        if (quantity <= 0 || storeId == null || storeId <= 0 || productId == null || productId <= 0) {
            response.put("message", "Quantity and IDs must be positive values.");
            return ResponseEntity.badRequest().body(response);
        }
        if (!storeRepository.existsById(storeId) || !productRepository.existsById(productId)) {
            response.put("message", "Store or product was not found.");
            return ResponseEntity.status(404).body(response);
        }

        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
        if (inventory == null) {
            response.put("message", "No inventory record exists for this product and store.");
            return ResponseEntity.status(404).body(response);
        }
        int available = inventory.getStockLevel() == null ? 0 : inventory.getStockLevel();
        boolean availableForOrder = available >= quantity;
        response.put("valid", availableForOrder);
        response.put("availableQuantity", available);
        response.put("message", availableForOrder
                ? "Requested quantity is available."
                : "Insufficient stock for the requested quantity.");
        return ResponseEntity.ok(response);
    }
}
