package id.ac.ui.cs.advprog.b13.hiringgo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.b13.hiringgo.user.config.SecurityConfig; // Import SecurityConfig
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
// import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserResponseDTO; // Tidak digunakan secara langsung di sini
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.security.JwtAuthenticationFilter; // Import filter
import id.ac.ui.cs.advprog.b13.hiringgo.user.security.JwtTokenProvider;       // Import provider
import id.ac.ui.cs.advprog.b13.hiringgo.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // Untuk mengimpor konfigurasi
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Untuk mock user
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
// import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity; // Jika setup manual

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class}) // Impor konfigurasi security & filter
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean // Mock JwtTokenProvider karena JwtAuthenticationFilter membutuhkannya
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUserDosen;
    private User mockUserMahasiswa;
    private UserRequestDTO userRequestDosenDTO;

    // Base path baru untuk controller
    private final String BASE_URL = "/api/v1/user";

    @BeforeEach
    void setUp() {
        userRequestDosenDTO = new UserRequestDTO(
                null,
                "Dr. Ratna Yuwono",
                "ratna@unisinga.ac.id",
                "DOSEN",
                "password123Valid", // Password dibuat valid
                null,
                "197005151998032002"
        );

        mockUserDosen = User.builder()
                .id(1L)
                .namaLengkap("Dr. Ratna Yuwono")
                .email("ratna@unisinga.ac.id")
                .role(Role.DOSEN)
                .password("hashedPassword") // Anggap sudah di-hash
                .nip("197005151998032002")
                .build();

        mockUserMahasiswa = User.builder()
                .id(2L)
                .namaLengkap("Budi Santoso")
                .email("budi@student.ac.id")
                .role(Role.MAHASISWA)
                .password("hashedPasswordBudi") // Anggap sudah di-hash
                .nim("2006123456")
                .build();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN") // Simulasikan user dengan role ADMIN
    void testCreateUserWithValidInputDosen() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(mockUserDosen));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.namaLengkap", is("Dr. Ratna Yuwono")))
                .andExpect(jsonPath("$.user.email", is("ratna@unisinga.ac.id")))
                .andExpect(jsonPath("$.user.role", is("DOSEN")))
                .andExpect(jsonPath("$.user.nip", is("197005151998032002")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testCreateUserWithValidInputMahasiswa() throws Exception {
        UserRequestDTO userRequestMahasiswaDTO = new UserRequestDTO(
                null, "Budi Santoso", "budi@student.ac.id", "MAHASISWA", "pass123Valid", "2006123456", null
        );
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(mockUserMahasiswa));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestMahasiswaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.user.id", is(2)))
                .andExpect(jsonPath("$.user.namaLengkap", is("Budi Santoso")))
                .andExpect(jsonPath("$.user.role", is("MAHASISWA")))
                .andExpect(jsonPath("$.user.nim", is("2006123456"))) // Pastikan NIM ada
                .andExpect(jsonPath("$.user.nip").doesNotExist());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testCreateUserWithDuplicateEmail() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Email sudah terdaftar")));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Email sudah terdaftar")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER") // User biasa tidak boleh create
    void testCreateUser_ForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testListUsers() throws Exception {
        List<User> userList = List.of(mockUserDosen, mockUserMahasiswa);
        when(userService.listUsers()).thenReturn(CompletableFuture.completedFuture(userList));

        mockMvc.perform(get(BASE_URL + "/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andExpect(jsonPath("$.users[0].id", is(1)))
                .andExpect(jsonPath("$.users[0].namaLengkap", is("Dr. Ratna Yuwono")))
                .andExpect(jsonPath("$.users[0].nip", is("197005151998032002")))
                .andExpect(jsonPath("$.users[1].id", is(2)))
                .andExpect(jsonPath("$.users[1].namaLengkap", is("Budi Santoso")))
                .andExpect(jsonPath("$.users[1].nim", is("2006123456"))) // Pastikan NIM ada untuk mahasiswa
                .andExpect(jsonPath("$.users[1].nip").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testListUsersEmpty() throws Exception {
        when(userService.listUsers()).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        mockMvc.perform(get(BASE_URL + "/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(0)));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testUpdateUserRoleSuccess() throws Exception {
        Long userIdToUpdate = 1L;
        String newRole = "ADMIN"; // Role yang valid
        User updatedUser = User.builder()
                .id(userIdToUpdate)
                .namaLengkap(mockUserDosen.getNamaLengkap())
                .email(mockUserDosen.getEmail())
                .role(Role.ADMIN) // Role sudah diubah menjadi ADMIN
                .nip(null) // NIP menjadi null jika role bukan DOSEN
                .build();

        when(userService.updateUserRole(eq(userIdToUpdate), eq(newRole)))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(updatedUser)));

        mockMvc.perform(patch(BASE_URL + "/update-role/" + userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", newRole))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Role pengguna dengan ID " + userIdToUpdate + " berhasil diperbarui")))
                .andExpect(jsonPath("$.user.role", is("ADMIN"))); // Verifikasi role pada user yang dikembalikan
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testUpdateUserRoleUserNotFound() throws Exception {
        Long userIdToUpdate = 99L;
        String newRole = "ADMIN";
        when(userService.updateUserRole(eq(userIdToUpdate), eq(newRole)))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        mockMvc.perform(patch(BASE_URL + "/update-role/" + userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", newRole))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User dengan ID " + userIdToUpdate + " tidak ditemukan")));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testUpdateUserRoleInvalidRole() throws Exception {
        Long userIdToUpdate = 1L;
        String invalidNewRole = "SUPERADMIN"; // Role tidak valid
        when(userService.updateUserRole(eq(userIdToUpdate), eq(invalidNewRole)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Role baru tidak valid: " + invalidNewRole)));

        mockMvc.perform(patch(BASE_URL + "/update-role/" + userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", invalidNewRole))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Role baru tidak valid: " + invalidNewRole)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testDeleteUserSuccess() throws Exception {
        Long userIdToDelete = 1L;
        when(userService.deleteUser(eq(userIdToDelete))).thenReturn(CompletableFuture.completedFuture(true));

        mockMvc.perform(delete(BASE_URL + "/delete/" + userIdToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User dengan ID " + userIdToDelete + " berhasil dihapus")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void testDeleteUserNotFound() throws Exception {
        Long userIdToDelete = 99L;
        when(userService.deleteUser(eq(userIdToDelete))).thenReturn(CompletableFuture.completedFuture(false));

        mockMvc.perform(delete(BASE_URL + "/delete/" + userIdToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User dengan ID " + userIdToDelete + " tidak ditemukan atau gagal dihapus")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN") // Atau sesuaikan dengan logic authorization Anda
    void testGetUserByIdFound() throws Exception {
        Long userIdToFind = 1L;
        when(userService.findById(eq(userIdToFind)))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mockUserDosen)));

        mockMvc.perform(get(BASE_URL + "/" + userIdToFind))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.namaLengkap", is(mockUserDosen.getNamaLengkap())));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN") // Atau sesuaikan
    void testGetUserByIdNotFound() throws Exception {
        Long userIdToFind = 99L;
        when(userService.findById(eq(userIdToFind)))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        mockMvc.perform(get(BASE_URL + "/" + userIdToFind))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User dengan ID " + userIdToFind + " tidak ditemukan")));
    }

    // ----- Test untuk Error Handling di Controller (CompletionException, etc.) -----
    // Pastikan semua endpoint yang dilindungi juga menggunakan @WithMockUser
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createUser_whenServiceThrowsCompletionExceptionWithOtherCause_shouldReturnInternalServerError() throws Exception {
        RuntimeException cause = new RuntimeException("Simulated internal service error");
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(cause)));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan internal saat membuat user: Simulated internal service error"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createUser_whenServiceThrowsCompletionExceptionWithNullCause_shouldReturnInternalServerError() throws Exception {
        String exceptionMessage = "Test CompletionException with null cause";
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(exceptionMessage,null)));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan internal saat membuat user: " + exceptionMessage));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createUser_whenServiceCallItselfThrowsRuntimeException_shouldReturnInternalServerError() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenThrow(new RuntimeException("Unexpected generic exception"));

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDosenDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan server: Unexpected generic exception"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void listUsers_whenServiceThrowsCompletionException_shouldReturnInternalServerError() throws Exception {
        RuntimeException cause = new RuntimeException("Simulated list fetch error");
        when(userService.listUsers())
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(cause)));

        mockMvc.perform(get(BASE_URL + "/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal mengambil daftar user: Simulated list fetch error"));
    }
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void listUsers_whenServiceThrowsCompletionExceptionWithNullCause_shouldReturnInternalServerError() throws Exception {
        String exceptionMessage = "Test list CompletionException with null cause";
        when(userService.listUsers())
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(exceptionMessage, null)));

        mockMvc.perform(get(BASE_URL + "/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal mengambil daftar user: " + exceptionMessage));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void listUsers_whenServiceCallItselfThrowsRuntimeException_shouldReturnInternalServerError() throws Exception {
        when(userService.listUsers())
                .thenThrow(new RuntimeException("Unexpected generic list exception"));

        mockMvc.perform(get(BASE_URL + "/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan server: Unexpected generic list exception"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenRoleInRequestIsNull_shouldReturnBadRequest() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("role", null);
        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Properti 'role' pada request body tidak boleh kosong"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenRoleInRequestIsBlank_shouldReturnBadRequest() throws Exception {
        Map<String, String> body = Map.of("role", "   ");
        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Properti 'role' pada request body tidak boleh kosong"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenRoleInRequestIsEmpty_shouldReturnBadRequest() throws Exception {
        Map<String, String> body = Map.of("role", "");
        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Properti 'role' pada request body tidak boleh kosong"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenServiceThrowsCompletionExceptionWithOtherCause_shouldReturnInternalServerError() throws Exception {
        Map<String, String> body = Map.of("role", "ADMIN");
        RuntimeException cause = new RuntimeException("Simulated internal update error");
        when(userService.updateUserRole(anyLong(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(cause)));

        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal update role user: Simulated internal update error"));
    }
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenServiceThrowsCompletionExceptionWithNullCause_shouldReturnInternalServerError() throws Exception {
        Map<String, String> body = Map.of("role", "ADMIN");
        String exceptionMessage = "Test updateRole CompletionException with null cause";
        when(userService.updateUserRole(anyLong(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(exceptionMessage, null)));

        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal update role user: " + exceptionMessage));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateRole_whenServiceCallItselfThrowsRuntimeException_shouldReturnInternalServerError() throws Exception {
        Map<String, String> body = Map.of("role", "ADMIN");
        when(userService.updateUserRole(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Unexpected generic update exception"));

        mockMvc.perform(patch(BASE_URL + "/update-role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan server: Unexpected generic update exception"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_whenServiceThrowsCompletionException_shouldReturnInternalServerError() throws Exception {
        RuntimeException cause = new RuntimeException("Simulated internal delete error");
        when(userService.deleteUser(anyLong()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(cause)));

        mockMvc.perform(delete(BASE_URL + "/delete/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal menghapus user: Simulated internal delete error"));
    }
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_whenServiceThrowsCompletionExceptionWithNullCause_shouldReturnInternalServerError() throws Exception {
        String exceptionMessage = "Test deleteUser CompletionException with null cause";
        when(userService.deleteUser(anyLong()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(exceptionMessage, null)));

        mockMvc.perform(delete(BASE_URL + "/delete/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal menghapus user: " + exceptionMessage));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_whenServiceCallItselfThrowsRuntimeException_shouldReturnInternalServerError() throws Exception {
        when(userService.deleteUser(anyLong()))
                .thenThrow(new RuntimeException("Unexpected generic delete exception"));

        mockMvc.perform(delete(BASE_URL + "/delete/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan server: Unexpected generic delete exception"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUserById_whenServiceThrowsCompletionException_shouldReturnInternalServerError() throws Exception {
        RuntimeException cause = new RuntimeException("Simulated internal find error");
        when(userService.findById(anyLong()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(cause)));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal mengambil user: Simulated internal find error"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUserById_whenServiceThrowsCompletionExceptionWithNullCause_shouldReturnInternalServerError() throws Exception {
        String exceptionMessage = "Test getUserById CompletionException with null cause";
        when(userService.findById(anyLong()))
                .thenReturn(CompletableFuture.failedFuture(new CompletionException(exceptionMessage, null)));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Gagal mengambil user: " + exceptionMessage));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUserById_whenServiceCallItselfThrowsRuntimeException_shouldReturnInternalServerError() throws Exception {
        when(userService.findById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected generic find exception"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Terjadi kesalahan server: Unexpected generic find exception"));
    }
}