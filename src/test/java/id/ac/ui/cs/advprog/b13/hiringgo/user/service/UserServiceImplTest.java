package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserResponseDTO; // Ditambahkan untuk tes convertToUserResponse
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections; // Untuk list kosong
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUserSuccessDosen() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dr. Ratna Yuwono", "ratna@unisinga.ac.id", "DOSEN", "password123", null, "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(false);
        when(userRepository.existsByNip("197005151998032002")).thenReturn(false); // Pastikan NIP belum ada

        User expectedUser = User.builder()
                .id(1L)
                .namaLengkap("Dr. Ratna Yuwono")
                .email("ratna@unisinga.ac.id")
                .role(Role.DOSEN)
                .password("PLAIN_PASSWORD_PLACEHOLDER")
                .nip("197005151998032002")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("ratna@unisinga.ac.id", user.getEmail());
        assertEquals(Role.DOSEN, user.getRole());
        assertEquals("197005151998032002", user.getNip());
        assertNull(user.getNim()); // NIM harus null untuk DOSEN
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserSuccessMahasiswa() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Budi Santoso", "budi@ui.ac.id", "MAHASISWA", "password123", "2006123456", null);
        when(userRepository.existsByEmail("budi@ui.ac.id")).thenReturn(false);
        when(userRepository.existsByNim("2006123456")).thenReturn(false); // Pastikan NIM belum ada

        User.UserBuilder userBuilder = User.builder()
                .namaLengkap(userRequest.getNamaLengkap())
                .email(userRequest.getEmail())
                .role(Role.MAHASISWA)
                .password("PLAIN_PASSWORD_PLACEHOLDER")
                .nim(userRequest.getNim());
        User userToSave = userBuilder.build(); // User sebelum di-save
        User savedUser = userBuilder.id(2L).build(); // User setelah di-save dengan ID

        when(userRepository.save(argThat(u -> u.getNim().equals("2006123456") && u.getNip() == null)))
                .thenReturn(savedUser);


        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("budi@ui.ac.id", user.getEmail());
        assertEquals(Role.MAHASISWA, user.getRole());
        assertEquals("2006123456", user.getNim());
        assertNull(user.getNip()); // NIP harus null untuk MAHASISWA
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserSuccessAdmin() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Admin Utama", "admin@hiringgo.com", "ADMIN", "adminpass", null, null);
        when(userRepository.existsByEmail("admin@hiringgo.com")).thenReturn(false);
        // Tidak perlu cek NIP/NIM untuk ADMIN

        User expectedUser = User.builder()
                .id(3L)
                .namaLengkap("Admin Utama")
                .email("admin@hiringgo.com")
                .role(Role.ADMIN)
                .password("PLAIN_PASSWORD_PLACEHOLDER")
                .build(); // NIP dan NIM default null oleh builder jika tidak diset
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("admin@hiringgo.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
        assertNull(user.getNip());
        assertNull(user.getNim());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void testCreateUserDuplicateEmail() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dr. Ratna Yuwono", "ratna@unisinga.ac.id", "DOSEN", "password123", null, "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);

        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email sudah terdaftar", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserInvalidRole() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Test User", "test@example.com", "INVALID_ROLE", "password123", null, null);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);

        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Role tidak valid: INVALID_ROLE", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserRequestNull() {
        CompletableFuture<User> futureUser = userService.createUser(null);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Request tidak boleh null", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserEmailNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama", null, "DOSEN", "pass", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, dan role tidak boleh null dalam request", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserRoleNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama", "email@test.com", null, "pass", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, dan role tidak boleh null dalam request", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserNamaLengkapNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, null, "email@test.com", "DOSEN", "pass", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, dan role tidak boleh null dalam request", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserDosenNipExists() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dosen Baru", "dosenbaru@test.com", "DOSEN", "pass", null, "NIP001");
        when(userRepository.existsByEmail("dosenbaru@test.com")).thenReturn(false);
        when(userRepository.existsByNip("NIP001")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("NIP sudah terdaftar", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserDosenNipNull() { // NIP null untuk Dosen seharusnya tidak gagal di check existsByNip
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dosen Tanpa NIP", "dosen.no.nip@test.com", "DOSEN", "pass", null, null);
        when(userRepository.existsByEmail("dosen.no.nip@test.com")).thenReturn(false);
        // Tidak ada mock untuk existsByNip karena request.getNip() akan null

        User expectedUser = User.builder().id(1L).namaLengkap("Dosen Tanpa NIP").email("dosen.no.nip@test.com").role(Role.DOSEN).build();
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();
        assertNotNull(user);
        assertEquals(Role.DOSEN, user.getRole());
        assertNull(user.getNip()); // NIP akan null
    }


    @Test
    void testCreateUserMahasiswaNimExists() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Mhs Baru", "mhsbaru@test.com", "MAHASISWA", "pass", "NIM001", null);
        when(userRepository.existsByEmail("mhsbaru@test.com")).thenReturn(false);
        when(userRepository.existsByNim("NIM001")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("NIM sudah terdaftar", ex.getCause().getMessage());
    }

    @Test
    void testCreateUserMahasiswaNimNull() { // NIM null untuk Mahasiswa seharusnya tidak gagal di check existsByNim
        UserRequestDTO userRequest = new UserRequestDTO(null, "Mhs Tanpa NIM", "mhs.no.nim@test.com", "MAHASISWA", "pass", null, null);
        when(userRepository.existsByEmail("mhs.no.nim@test.com")).thenReturn(false);

        User expectedUser = User.builder().id(1L).namaLengkap("Mhs Tanpa NIM").email("mhs.no.nim@test.com").role(Role.MAHASISWA).build();
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();
        assertNotNull(user);
        assertEquals(Role.MAHASISWA, user.getRole());
        assertNull(user.getNim()); // NIM akan null
    }


    // --- Tes yang sudah ada untuk updateUserRole ---
    @Test
    void testUpdateUserRoleSuccess_DosenToAdmin() { // Nama diubah agar lebih spesifik
        Long userId = 1L;
        String newRoleStr = "ADMIN";
        User existingUser = User.builder().id(userId).namaLengkap("Test User").email("test@example.com").role(Role.DOSEN).nip("123").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // userToSave.setRole(Role.ADMIN); // Ini sudah dilakukan di service
            // userToSave.setNip(null); // Ini sudah dilakukan di service
            return userToSave; // Kembalikan user yang sudah dimodifikasi oleh service
        });

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        assertEquals(Role.ADMIN, optionalUser.get().getRole());
        assertNull(optionalUser.get().getNip(), "NIP should be null when role changes from DOSEN to ADMIN");
        assertNull(optionalUser.get().getNim(), "NIM should be null for ADMIN");
        verify(userRepository).save(any(User.class));
    }

    // --- Tes Tambahan untuk updateUserRole ---
    @Test
    void testUpdateUserRole_AdminToDosen() {
        Long userId = 1L;
        String newRoleStr = "DOSEN";
        User existingUser = User.builder().id(userId).namaLengkap("Test Admin").email("admin@example.com").role(Role.ADMIN).build();
        // NIP dan NIM untuk existingUser adalah null

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        User updatedUser = optionalUser.get();
        assertEquals(Role.DOSEN, updatedUser.getRole());
        // NIP tidak di-set karena tidak ada di DTO update, dan tidak di-null-kan karena role baru adalah DOSEN
        assertNull(updatedUser.getNip());
        assertNull(updatedUser.getNim(), "NIM should be null when role changes to DOSEN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_AdminToMahasiswa() {
        Long userId = 1L;
        String newRoleStr = "MAHASISWA";
        User existingUser = User.builder().id(userId).namaLengkap("Test Admin").email("admin@example.com").role(Role.ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        User updatedUser = optionalUser.get();
        assertEquals(Role.MAHASISWA, updatedUser.getRole());
        assertNull(updatedUser.getNip(), "NIP should be null when role changes to MAHASISWA");
        // NIM tidak di-set karena tidak ada di DTO update, dan tidak di-null-kan karena role baru adalah MAHASISWA
        assertNull(updatedUser.getNim());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void testUpdateUserRoleUserNotFound() {
        Long userId = 99L;
        String newRole = "ADMIN";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRole);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertFalse(optionalUser.isPresent());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserRoleInvalidNewRole() {
        Long userId = 1L;
        String newRoleStr = "INVALID_ROLE";

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);

        CompletionException ex = assertThrows(CompletionException.class, futureOptionalUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Role baru tidak valid: " + newRoleStr, ex.getCause().getMessage());
    }


    @Test
    void testDeleteUserSuccess() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        CompletableFuture<Boolean> futureDeleted = userService.deleteUser(userId);
        assertTrue(futureDeleted.join());
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteUserNotFound() {
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        CompletableFuture<Boolean> futureDeleted = userService.deleteUser(userId);
        assertFalse(futureDeleted.join());
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void testListUsersSuccess() {
        User user1 = User.builder().id(1L).namaLengkap("Test User 1").email("test1@mail.com").role(Role.MAHASISWA).build();
        User user2 = User.builder().id(2L).namaLengkap("Test User 2").email("test2@mail.com").role(Role.DOSEN).nip("12345").build();
        List<User> mockUsers = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(mockUsers);

        CompletableFuture<List<User>> futureUsers = userService.listUsers();
        List<User> users = futureUsers.join();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Test User 1", users.get(0).getNamaLengkap());
    }

    @Test
    void testFindByIdSuccess() {
        Long userId = 1L;
        User foundUser = User.builder().id(userId).namaLengkap("Found User").email("find@mail.com").role(Role.MAHASISWA).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(foundUser));

        CompletableFuture<Optional<User>> futureOptionalUser = userService.findById(userId);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        assertEquals(userId, optionalUser.get().getId());
    }

    @Test
    void testFindByIdNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CompletableFuture<Optional<User>> futureOptionalUser = userService.findById(userId);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertFalse(optionalUser.isPresent());
    }

    @Test
    void testConvertToUserResponse_NullUser() {
        UserResponseDTO dto = userService.convertToUserResponse(null);
        assertNull(dto);
    }

    @Test
    void testConvertToUserResponse_DosenUser() {
        User dosen = User.builder().id(1L).namaLengkap("Prof. X").email("prof.x@example.com").role(Role.DOSEN).nip("NIP007").build();
        UserResponseDTO dto = userService.convertToUserResponse(dosen);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Prof. X", dto.getNamaLengkap());
        assertEquals("prof.x@example.com", dto.getEmail());
        assertEquals("DOSEN", dto.getRole());
        assertEquals("NIP007", dto.getNip());
        assertNull(dto.getNim());
    }

    @Test
    void testConvertToUserResponse_MahasiswaUser() {
        User mahasiswa = User.builder().id(2L).namaLengkap("Budi").email("budi@example.com").role(Role.MAHASISWA).nim("NIM008").build();
        UserResponseDTO dto = userService.convertToUserResponse(mahasiswa);
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Budi", dto.getNamaLengkap());
        assertEquals("MAHASISWA", dto.getRole());
        assertEquals("NIM008", dto.getNim());
        assertNull(dto.getNip());
    }

    @Test
    void testConvertToUserResponse_AdminUser() {
        User admin = User.builder().id(3L).namaLengkap("Admin").email("admin@example.com").role(Role.ADMIN).build();
        UserResponseDTO dto = userService.convertToUserResponse(admin);
        assertNotNull(dto);
        assertEquals(3L, dto.getId());
        assertEquals("Admin", dto.getNamaLengkap());
        assertEquals("ADMIN", dto.getRole());
        assertNull(dto.getNip());
        assertNull(dto.getNim());
    }


    @Test
    void testFindAllUsersAsResponseAsync_EmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        CompletableFuture<List<UserResponseDTO>> futureDtos = userService.findAllUsersAsResponseAsync();
        List<UserResponseDTO> dtos = futureDtos.join();
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void testFindAllUsersAsResponseAsync_WithUsers() {
        User dosen = User.builder().id(1L).namaLengkap("Prof. X").email("prof.x@example.com").role(Role.DOSEN).nip("NIP007").build();
        User mahasiswa = User.builder().id(2L).namaLengkap("Budi").email("budi@example.com").role(Role.MAHASISWA).nim("NIM008").build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(dosen, mahasiswa));

        CompletableFuture<List<UserResponseDTO>> futureDtos = userService.findAllUsersAsResponseAsync();
        List<UserResponseDTO> dtos = futureDtos.join();

        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        UserResponseDTO dosenDto = dtos.stream().filter(d -> d.getRole().equals("DOSEN")).findFirst().orElse(null);
        assertNotNull(dosenDto);
        assertEquals("Prof. X", dosenDto.getNamaLengkap());
        assertEquals("NIP007", dosenDto.getNip());

        UserResponseDTO mahasiswaDto = dtos.stream().filter(d -> d.getRole().equals("MAHASISWA")).findFirst().orElse(null);
        assertNotNull(mahasiswaDto);
        assertEquals("Budi", mahasiswaDto.getNamaLengkap());
        assertEquals("NIM008", mahasiswaDto.getNim());
    }
}