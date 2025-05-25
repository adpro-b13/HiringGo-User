package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap; // Digunakan di kode asli Anda
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException; // Untuk menangani error dari .join()
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user") // Base URL tetap /user
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Helper untuk membuat respons Map dari User object
    // Disesuaikan agar menggunakan field dari User entity yang sudah JPA (namaLengkap, Long id)
    private Map<String, Object> createUserResponseMap(User user) {
        if (user == null) {
            return null;
        }
        Map<String, Object> userResp = new LinkedHashMap<>();
        userResp.put("id", user.getId()); // ID sekarang Long
        userResp.put("namaLengkap", user.getNamaLengkap()); // Menggunakan namaLengkap
        userResp.put("email", user.getEmail());
        userResp.put("role", user.getRole().name());
        if (user.getRole() == Role.DOSEN) { // Menggunakan enum Role
            userResp.put("nip", user.getNip());
        }
        return userResp;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO request) { // Menggunakan UserRequestDTO
        try {
            User user = userService.createUser(request).join();
            Map<String, Object> responseMap = createUserResponseMap(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("user", responseMap)); // Struktur respons Map tetap
        } catch (CompletionException e) { // Menangkap CompletionException dari .join()
            Throwable cause = e.getCause(); // Mendapatkan exception asli
            if (cause instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", cause.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan internal saat membuat user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) { // Catch-all untuk error lain yang mungkin tidak terduga
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers() {
        try {
            List<User> allUsers = userService.listUsers().join();
            List<Map<String, Object>> usersRespList = allUsers.stream()
                    .map(this::createUserResponseMap) // Menggunakan helper untuk konsistensi
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("users", usersRespList)); // Struktur respons Map tetap
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal mengambil daftar user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    // DTO untuk request update role (bisa diletakkan di package dto jika belum ada)
    // Saya akan gunakan Map<String, String> req seperti kode asli Anda untuk minimal perubahan di sini.
    // Namun, menggunakan DTO khusus lebih disarankan.
    // public static class RoleUpdateRequest {
    //     private String role;
    //     public String getRole() { return role; }
    //     public void setRole(String role) { this.role = role; }
    // }

    @PatchMapping("/update-role/{userId}")
    public ResponseEntity<?> updateRole(@PathVariable Long userId, @RequestBody Map<String, String> req) { // userId diubah ke Long
        try {
            String newRole = req.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Properti 'role' pada request body tidak boleh kosong"));
            }
            // UserServiceImpl.updateUserRole sekarang mengembalikan CompletableFuture<Optional<User>>
            Optional<User> updatedUserOptional = userService.updateUserRole(userId, newRole).join();

            if (updatedUserOptional.isPresent()) {
                // Jika ingin mengembalikan data user yang diupdate:
                // Map<String, Object> responseMap = createUserResponseMap(updatedUserOptional.get());
                // return ResponseEntity.ok(Map.of("user", responseMap, "message", "Role pengguna berhasil diperbarui"));
                return ResponseEntity.ok(Map.of("message", "Role pengguna dengan ID " + userId + " berhasil diperbarui"));
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
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) { // userId diubah ke Long
        try {
            // UserServiceImpl.deleteUser sekarang mengembalikan CompletableFuture<Boolean>
            boolean deleted = userService.deleteUser(userId).join();
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "User dengan ID " + userId + " berhasil dihapus"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User dengan ID " + userId + " tidak ditemukan atau gagal dihapus"));
            }
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            // Jika ada exception spesifik dari service yang ingin ditangani berbeda
            // if (cause instanceof SomeSpecificExceptionForDelete) { ... }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal menghapus user: " + (cause != null ? cause.getMessage() : e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    // Opsional: Endpoint untuk mendapatkan user berdasarkan ID, jika dibutuhkan
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) { // userId diubah ke Long
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
