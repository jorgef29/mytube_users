package com.fiuni.mytube_users.controller;

import com.fiuni.mytube.dto.subscription.SubscriptionDTO;
import com.fiuni.mytube.dto.subscription.SubscriptionResult;
import com.fiuni.mytube_users.service.subscription.ISubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@Slf4j
public class SubscriptionController {

    @Autowired
    private ISubscriptionService subscriptionService;

    // Método para guardar una nueva suscripción
    @PostMapping("/save")
    public ResponseEntity<SubscriptionDTO> saveSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
        try {
            log.info("Saving new subscription: {}", subscriptionDTO);
            // Guardar la suscripción
            SubscriptionDTO savedSubscription = subscriptionService.save(subscriptionDTO);
            log.info("Subscription saved successfully:{}", savedSubscription);
            return new ResponseEntity<>(savedSubscription, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al guardar la suscripción:{}", subscriptionDTO, e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Método para obtener una suscripción por ID
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable Integer id) {
        try {
            log.info("Getting subscription with ID:{}", id);
            // Obtener la suscripción por ID
            SubscriptionDTO subscription = subscriptionService.getById(id);
            log.info("Subscription obtained:{}", subscription);
            return new ResponseEntity<>(subscription, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting subscription with ID: {}", id, e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Método para obtener todas las suscripciones

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionDTO>> getAllSubscriptions(Pageable pageable) {
        try {
            log.info("Getting all subscriptions");
            // Obtener todas las suscripciones
            SubscriptionResult result = subscriptionService.getAll(pageable);
            log.info("Subscriptions successfully obtained");
            return new ResponseEntity<>(result.getSubscriptions(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting all subscriptions", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Integer id) {
        try {
            log.info("Deleting subscription with ID: {}", id);
            // Llamar al servicio para eliminar la suscripción
            subscriptionService.deleteSubscription(id);
            log.info("Subscription with ID: {} successfully removed", id);
            return new ResponseEntity<>("Subscription successfully removed", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            log.error("Subscription not found with ID: {}", id, e);
            return new ResponseEntity<>(e.getReason(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting subscription with ID:{}", id, e);
            return new ResponseEntity<>("Error deleting subscription", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
