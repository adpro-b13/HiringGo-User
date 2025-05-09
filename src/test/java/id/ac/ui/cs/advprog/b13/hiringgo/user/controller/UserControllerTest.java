package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testCreateUserWithValidInput() throws Exception {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");

        // Mocking repository behavior
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(false);
        when(userRepository.existsByNip("197005151998032002")).thenReturn(false);

        // Simulate saving user: expected to return the user (usually an entity, simplified here)
        // For better test: you could mock userRepository.save(any()) to return a 'User', but for this example, it's simple

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

        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(true);

        mockMvc.perform(post("/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Email sudah terdaftar")));
    }

    @Test
    void testListUsers() throws Exception {
        // biarkan kosong, asalkan .findAll() tidak error
        when(userRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/user/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)));
    }

    // Lanjutkan dengan test PATCH dan DELETE sesuai kebutuhanmu!
}