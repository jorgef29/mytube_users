package com.fiuni.mytube_users.controller;

import com.fiuni.mytube.dto.subscription.SubscriptionDTO;
import com.fiuni.mytube.dto.subscription.SubscriptionResult;
import com.fiuni.mytube_users.service.subscription.ISubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@Slf4j
public class SubscriptionController {

    @Autowired
    private ISubscriptionService subscriptionService;

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionDTO>> getAllSubscriptions(Pageable pageable) {
        log.info("Getting all subscriptions");
        SubscriptionResult result = subscriptionService.getAll(pageable);
        log.info("Subscriptions successfully obtained");
        return new ResponseEntity<>(result.getSubscriptions(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable Integer id) {
        log.info("Getting subscription with ID: {}", id);
        SubscriptionDTO subscription = subscriptionService.getById(id);
        log.info("Subscription obtained: {}", subscription);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<SubscriptionDTO> saveSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
        log.info("Saving new subscription: {}", subscriptionDTO);
        SubscriptionDTO savedSubscription = subscriptionService.save(subscriptionDTO);
        log.info("Subscription saved successfully: {}", savedSubscription);
        return new ResponseEntity<>(savedSubscription, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Integer id) {
        log.info("Deleting subscription with ID: {}", id);
        subscriptionService.deleteSubscription(id);
        log.info("Subscription with ID: {} successfully removed", id);
        return new ResponseEntity<>("Subscription successfully removed", HttpStatus.OK);
    }
}
