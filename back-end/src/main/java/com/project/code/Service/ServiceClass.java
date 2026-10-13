package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {

    public boolean validateInventory(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return false;
        }
        Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId());
        return existingInventory == null;
    }

    public Inventory getInventoryId(Long productId, Long storeId) {
        if (productId == null || storeId == null) {
            return null;
        }
        return inventoryRepository.findByProductIdAndStoreId(productId, storeId);
    }

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // 2. validateProduct Method:
    // Checks if a product exists by its name.
    // Returns `false` if a product with the same name exists, otherwise `true`.
    public boolean validateProduct(Product product) {
        if (product == null || product.getName() == null) {
            return true;
        }
        Product existingProduct = productRepository.findByName(product.getName());
        // Returns false if a product with the same name exists, otherwise true
        return existingProduct == null;
    }

    // 3. ValidateProductId Method:
    // Checks if a product exists by its ID.
    // Returns `false` if the product does not exist with the given ID, otherwise
    // `true`.
    public boolean validateProductId(long id) {
        // existsById returns true if found, false if not found
        return productRepository.existsById(id);
    }

}
