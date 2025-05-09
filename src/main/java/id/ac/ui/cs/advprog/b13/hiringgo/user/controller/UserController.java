package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. CREATE USER
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        try {
            // Cek unique email
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email sudah terdaftar"));
            }
            // Cek unique NIP jika dosen
            if (UserRole.DOSEN.name().equalsIgnoreCase(request.getRole())
                    && userRepository.existsByNip(request.getNip())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "NIP sudah terdaftar"));
            }
            User newUser = new User(request);
            userRepository.save(newUser);

            Map<String, Object> userResp = new LinkedHashMap<>();
            userResp.put("id", newUser.getId());
            userResp.put("name", newUser.getName());
            userResp.put("email", newUser.getEmail());
            userResp.put("role", newUser.getRole().name());
            if (newUser.getRole() == UserRole.DOSEN)
                userResp.put("nip", newUser.getNip());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("user", userResp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    // 2. READ/GET USER LIST
    @GetMapping("/list")
    public ResponseEntity<?> listUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    // 3. UPDATE ROLE
    @PatchMapping("/update-role/{userId}")
    public ResponseEntity<?> updateRole(@PathVariable String userId, @RequestBody Map<String, String> req) {
        try {
            String newRole = req.get("role");
            if (!UserRole.contains(newRole)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid role"));
            }

            User user = userRepository.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "User tidak ditemukan"));
            }

            // Simple update logic: create new User with new role and save (karena model immutable)
            UserRequest updateReq = new UserRequest(
                    user.getEmail(),
                    user.getName(),
                    newRole,
                    (UserRole.DOSEN.name().equals(newRole)) ? user.getNip() : null);
            User updated = new User(updateReq) {
                @Override
                public String getId() {
                    return user.getId();
                }
            };

            // Hapus yang lama, simpan baru
            userRepository.deleteById(userId);
            userRepository.save(updated);

            return ResponseEntity.ok(Map.of("message", "Role pengguna berhasil diperbarui"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

    // 4. DELETE USER
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            boolean deleted = userRepository.deleteById(userId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User tidak ditemukan"));
            }
            return ResponseEntity.ok(Map.of("message", "User berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
    }

}