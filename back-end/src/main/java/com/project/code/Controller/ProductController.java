package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryRepository inventoryRepository;

    // 3. Add Product (POST)
    @PostMapping
    public ResponseEntity<Map<String, String>> addProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isValid = serviceClass.validateProduct(product);
            if (!isValid) {
                response.put("message", "A product with this name already exists.");
                return ResponseEntity.badRequest().body(response);
            }
            productRepository.save(product);
            response.put("message", "Product added successfully.");
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: Duplicate or invalid field.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred while adding the product: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 4. Get Product by ID (GET)
    @GetMapping("/product/{id}")
    public ResponseEntity<Map<String, Object>> getProductbyId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Product product = productRepository.findById(id).orElse(null);
        response.put("products", product);
        return ResponseEntity.ok(response);
    }

    // 5. Update Product (PUT)
    @PutMapping
    public ResponseEntity<Map<String, String>> updateProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        productRepository.save(product);
        response.put("message", "Product updated successfully.");
        return ResponseEntity.ok(response);
    }

    // 6. Filter Products by Category and Name (GET)
    @GetMapping("/category/{name}/{category}")
    public ResponseEntity<Map<String, Object>> filterbyCategoryProduct(
            @PathVariable String name,
            @PathVariable String category) {
        
        List<Product> products;

        if ("null".equals(category) && !"null".equals(name)) {
            Product product = productRepository.findByName(name);
            products = product != null ? List.of(product) : List.of();
        } else if (!"null".equals(category) && "null".equals(name)) {
            products = productRepository.findByCategory(category);
        } else if (!"null".equals(category) && !"null".equals(name)) {
            products = productRepository.findByCategory(category).stream()
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
        } else {
            products = productRepository.findAll();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // 7. List All Products (GET)
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProduct() {
        List<Product> products = productRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // 8. Get Products by Category and Store ID (GET)
    @GetMapping("filter/{category}/{storeid}")
    public ResponseEntity<Map<String, Object>> getProductbyCategoryAndStoreId(
            @PathVariable String category,
            @PathVariable Long storeid) {
        
        // Fetch inventory records for the given store and filter products by category
        List<Product> products = inventoryRepository.findByStore_Id(storeid).stream()
                .map(inventory -> inventory.getProduct())
                .filter(p -> p != null && category.equalsIgnoreCase(p.getCategory()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("product", products);
        return ResponseEntity.ok(response);
    }

    // 9. Delete Product and Related Inventory (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();

        boolean exists = serviceClass.validateProductId(id);
        if (!exists) {
            response.put("message", "Product does not exist with ID: " + id);
            return ResponseEntity.status(404).body(response);
        }

        // Delete from inventory first to maintain referential integrity
        inventoryRepository.deleteByProductId(id);

        // Delete the product itself
        productRepository.deleteById(id);

        response.put("message", "Product deleted successfully.");
        return ResponseEntity.ok(response);
    }

    // 10. Search Products by Name (GET)
    @GetMapping("/searchProduct/{name}")
    public ResponseEntity<Map<String, Object>> searchProduct(@PathVariable String name) {
        // Fallback search handling using findByName or filtering repository items if custom subName method isn't explicitly declared
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }
}