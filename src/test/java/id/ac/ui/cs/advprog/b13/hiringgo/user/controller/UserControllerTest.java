package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture; // Import ini
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUserWithValidInput() throws Exception {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");
        User mockUser = new User(userRequest);
        // Menggunakan CompletableFuture.completedFuture() untuk mocking
        when(userService.createUser(any(UserRequest.class))).thenReturn(CompletableFuture.completedFuture(mockUser));

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name", is("Dr. Ratna Yuwono")))
                .andExpect(jsonPath("$.user.email", is("ratna@unisinga.ac.id")))
                .andExpect(jsonPath("$.user.role", is("DOSEN")))
                .andExpect(jsonPath("$.user.nip", is("197005151998032002")));
    }

    @Test
    void testCreateUserWithDuplicateEmail() throws Exception {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");
        // Menggunakan CompletableFuture.failedFuture() untuk mocking exception
        when(userService.createUser(any(UserRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Email sudah terdaftar")));

        mockMvc.perform(post("/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Email sudah terdaftar")));
    }

    @Test
    void testListUsers() throws Exception {
        // Menggunakan CompletableFuture.completedFuture() untuk mocking list kosong
        when(userService.listUsers()).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        mockMvc.perform(get("/user/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)));
    }

    @Test
    void testUpdateUserRoleSuccess() throws Exception {
        String userId = "USR-123ABC";
        String newRole = "ADMIN";
        // Untuk metode void, kembalikan CompletableFuture.completedFuture(null)
        when(userService.updateUserRole(eq(userId), eq(newRole))).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(patch("/user/update-role/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + newRole + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Role pengguna berhasil diperbarui")));
    }

    @Test
    void testUpdateUserRoleInvalidRole() throws Exception {
        String userId = "USR-123ABC";
        String newRole = "SUPERADMIN"; // invalid
        // Menggunakan CompletableFuture.failedFuture() untuk mocking exception
        when(userService.updateUserRole(eq(userId), eq(newRole)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid role")));

        mockMvc.perform(patch("/user/update-role/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + newRole + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid role")));
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        String userId = "USR-123ABC";
        // Untuk metode void, kembalikan CompletableFuture.completedFuture(null)
        when(userService.deleteUser(eq(userId))).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/user/delete/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User berhasil dihapus")));
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        String userId = "USR-123ABC";
        // Menggunakan CompletableFuture.failedFuture() untuk mocking exception
        when(userService.deleteUser(eq(userId)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("User tidak ditemukan")));

        mockMvc.perform(delete("/user/delete/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User tidak ditemukan")));
    }
}