package com.fiuni.mytube_users.controller;

import com.fiuni.mytube.dto.subscription.SubscriptionDTO;
import com.fiuni.mytube.dto.user.UserDTO;
import com.fiuni.mytube.dto.user.UserResult;
import com.fiuni.mytube_users.dto.UserDTOCreate;
import com.fiuni.mytube_users.service.subscription.ISubscriptionService;
import com.fiuni.mytube_users.service.userService.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.fiuni.mytube_users.exception.ResourceNotFoundException;
import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISubscriptionService subscriptionService;
    //endpoint para obtener todos los usuarios
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> findAllUsers(Pageable pageable) {
        try {
            log.info("Fetching all users with pagination");
            UserResult result = userService.getAll(pageable);
            log.info("Successfully fetched paginated users");
            return new ResponseEntity<>(result.getUsers(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //endpoint para guardar usuario
    @PostMapping("/save")
    public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTOCreate userDTO) {
        try {
            log.info("Saving user: {}", userDTO);
            UserDTO savedUser = userService.createUser(userDTO);
            log.info("Successfully saved user: {}", savedUser);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error saving user: {}", userDTO, e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        log.info("Searching user id: {}", id);
        UserDTO userDTO = null;
        try {
            userDTO = userService.getById(id);
            log.info("User found: {}", userDTO);    
            if (userDTO == null) {
                log.warn("User with id {} not found", id);
                throw new ResourceNotFoundException("User with id " + id + " not found");
            }
        } catch (Exception e) {
            log.error("Error while retrieving user with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
    //enpoint para actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        try {
            log.info("Updating user with ID: {}", id);
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            log.info("Successfully updated user: {}", updatedUser);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            log.error("User not found with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //enpoint para actualizar contrasenha de usuario
    @PutMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable Integer id, @RequestBody UserDTOCreate dto) {
        try {
            log.info("Changing password for user ID: {}", id);
            userService.changePassword(id, dto);
            log.info("Successfully changed password for user ID: {}", id);
            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error changing password for user ID: {}", id, e);
            return new ResponseEntity<>("Error updating password", HttpStatus.BAD_REQUEST);
        }
    }
    //endpoint para eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            log.info("Deleting user with ID: {}", id);
            userService.deleteUser(id);
            log.info("Successfully deleted user with ID: {}", id);
            return new ResponseEntity<>("User successfully deleted (soft delete)", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            log.error("User not found with ID: {}", id, e);
            return new ResponseEntity<>(e.getReason(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting user with ID: {}", id, e);
            return new ResponseEntity<>("Error deleting user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //enpoint para obtener suscripciones de un usuario
    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<List<SubscriptionDTO>> getUserSubscriptions(@PathVariable Integer id) {
        try {
            log.info("Fetching subscriptions for user ID: {}", id);
            List<SubscriptionDTO> subscriptions = subscriptionService.getUserSubscriptions(id);
            if (subscriptions.isEmpty()) {
                log.info("No subscriptions found for user ID: {}", id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            log.info("Successfully fetched subscriptions for user ID: {}", id);
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching subscriptions for user ID: {}", id, e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
