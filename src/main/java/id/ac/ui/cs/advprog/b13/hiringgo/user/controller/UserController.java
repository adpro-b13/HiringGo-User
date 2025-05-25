package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // BARU
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Map<String, Object> createUserResponseMap(User user) {
        if (user == null) {
            return null;
        }
        Map<String, Object> userResp = new LinkedHashMap<>();
        userResp.put("id", user.getId());
        userResp.put("namaLengkap", user.getNamaLengkap());
        userResp.put("email", user.getEmail());
        userResp.put("role", user.getRole().name());
        if (user.getRole() == Role.DOSEN) {
            userResp.put("nip", user.getNip());
        }
        if (user.getRole() == Role.MAHASISWA) { // Tambahkan ini untuk NIM
            userResp.put("nim", user.getNim());
        }
        return userResp;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Hanya ADMIN yang bisa create
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO request) {
        try {
            User user = userService.createUser(request).join();
            Map<String, Object> responseMap = createUserResponseMap(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User created successfully", "user", responseMap));
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan internal saat membuat user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Hanya ADMIN yang bisa lihat semua user
    public ResponseEntity<?> listUsers() {
        try {
            List<User> allUsers = userService.listUsers().join();
            List<Map<String, Object>> usersRespList = allUsers.stream()
                    .map(this::createUserResponseMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("users", usersRespList));
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal mengambil daftar user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @PatchMapping("/update-role/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Hanya ADMIN yang bisa update role
    public ResponseEntity<?> updateRole(@PathVariable Long userId, @RequestBody Map<String, String> req) {
        try {
            String newRole = req.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Properti 'role' pada request body tidak boleh kosong"));
            }
            Optional<User> updatedUserOptional = userService.updateUserRole(userId, newRole).join();

            if (updatedUserOptional.isPresent()) {
                Map<String, Object> responseMap = createUserResponseMap(updatedUserOptional.get());
                return ResponseEntity.ok(Map.of("message", "Role pengguna dengan ID " + userId + " berhasil diperbarui", "user", responseMap));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User dengan ID " + userId + " tidak ditemukan"));
            }
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal update role user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Hanya ADMIN yang bisa delete
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            boolean deleted = userService.deleteUser(userId).join();
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "User dengan ID " + userId + " berhasil dihapus"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User dengan ID " + userId + " tidak ditemukan atau gagal dihapus"));
            }
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal menghapus user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userService.findById(userId).join();
            if (userOptional.isPresent()) {
                Map<String, Object> responseMap = createUserResponseMap(userOptional.get());
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User dengan ID " + userId + " tidak ditemukan"));
            }
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal mengambil user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }
}