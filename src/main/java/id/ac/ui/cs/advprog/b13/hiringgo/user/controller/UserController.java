package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture; // Import ini
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        try {
            // Panggil metode async dan join() untuk mendapatkan hasilnya
            User user = userService.createUser(request).join();
            Map<String, Object> userResp = new LinkedHashMap<>();
            userResp.put("id", user.getId());
            userResp.put("name", user.getName());
            userResp.put("email", user.getEmail());
            userResp.put("role", user.getRole().name());
            if (user.getRole() == UserRole.DOSEN)
                userResp.put("nip", user.getNip());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("user", userResp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Tangani exception dari CompletableFuture jika ada
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers() {
        try {
            List<User> allUsers = userService.listUsers().join(); // Join untuk mendapatkan hasil
            List<Map<String, Object>> usersResp = allUsers.stream().map(user -> {
                Map<String, Object> u = new LinkedHashMap<>();
                u.put("id", user.getId());
                u.put("name", user.getName());
                u.put("email", user.getEmail());
                u.put("role", user.getRole().name());
                return u;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("users", usersResp));
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    @PatchMapping("/update-role/{userId}")
    public ResponseEntity<?> updateRole(@PathVariable String userId, @RequestBody Map<String, String> req) {
        try {
            String newRole = req.get("role");
            userService.updateUserRole(userId, newRole).join(); // Join untuk menunggu operasi selesai
            return ResponseEntity.ok(Map.of("message", "Role pengguna berhasil diperbarui"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId).join(); // Join untuk menunggu operasi selesai
            return ResponseEntity.ok(Map.of("message", "User berhasil dihapus"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }
}