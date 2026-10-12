package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // 3. Define the `getReviews` Method
    @GetMapping("/{storeId}/{productId}")
    public ResponseEntity<Map<String, Object>> getReviews(
            @PathVariable Long storeId,
            @PathVariable Long productId) {

        // Fetch reviews for the specific store and product from MongoDB/JPA repository
        List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);

        // Filter and transform reviews to include comment, rating, and customer name
        List<Map<String, Object>> filteredReviews = reviews.stream().map(review -> {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("comment", review.getComment());
            reviewData.put("rating", review.getRating());

            // Retrieve customer details using customerId
            Customer customer = customerRepository.findById(review.getCustomerId()).orElse(null);
            String customerName = (customer != null) ? customer.getName() : "Anonymous";
            reviewData.put("customerName", customerName);

            return reviewData;
        }).collect(Collectors.toList());

        // Prepare final response map
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", filteredReviews);

        return ResponseEntity.ok(response);
    }
}