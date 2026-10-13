package com.project.code.Service;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 1. saveOrder Method: Processes a customer's order, including saving order
     * details and items.
     */
    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {

        // 2. Retrieve or Create the Customer
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        // 3. Retrieve the Store
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + placeOrderRequest.getStoreId()));

        // 4. Create and Save OrderDetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setOrderDate(LocalDateTime.now());

        orderDetails = orderDetailsRepository.save(orderDetails);

        // 5. Create and Save OrderItems & Update Inventory Stock
        // Assuming PlaceOrderRequestDTO contains a list of items with productId and
        // quantity
        if (placeOrderRequest.getPurchaseProduct() != null) {
            for (var itemDto : placeOrderRequest.getPurchaseProduct()) {

                // Find corresponding product
                if (itemDto.getId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Each order item needs a product ID and a positive quantity.");
                }
                Product product = productRepository.findById(itemDto.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getId()));

                // Find inventory for this product and store
                Inventory inventory = inventoryRepository.findByProductIdAndStoreId(product.getId(), store.getId());
                if (inventory == null) {
                    throw new RuntimeException("Inventory record not found for product ID " + product.getId()
                            + " at store ID " + store.getId());
                }

                // Check stock levels
                if (inventory.getStockLevel() == null || inventory.getStockLevel() < itemDto.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                // Update stock levels
                inventory.setStockLevel(inventory.getStockLevel() - itemDto.getQuantity());
                inventoryRepository.save(inventory);

                // Create and save OrderItem
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(orderDetails);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(product.getPrice()); // Or price from request/snapshot

                orderItemRepository.save(orderItem);
            }
        }
    }
}
