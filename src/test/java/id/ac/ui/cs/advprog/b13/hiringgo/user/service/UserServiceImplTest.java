package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private final String MOCKED_HASHED_PASSWORD = "hashedPasswordViaEncoder";

    @BeforeEach
    void setUp() {
        // Jadikan lenient agar tidak error jika tidak semua tes menggunakan stubbing ini
        lenient().when(passwordEncoder.encode(anyString())).thenReturn(MOCKED_HASHED_PASSWORD);
    }

    @Test
    void testCreateUserSuccessDosen() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dr. Ratna Yuwono", "ratna@unisinga.ac.id", "DOSEN", "password123Valid", null, "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(false);
        when(userRepository.existsByNip("197005151998032002")).thenReturn(false);
        // Spesifik stubbing untuk encode jika diperlukan, atau biarkan global setUp yang handle
        when(passwordEncoder.encode("password123Valid")).thenReturn(MOCKED_HASHED_PASSWORD);


        User expectedUser = User.builder()
                .id(1L)
                .namaLengkap("Dr. Ratna Yuwono")
                .email("ratna@unisinga.ac.id")
                .role(Role.DOSEN)
                .password(MOCKED_HASHED_PASSWORD)
                .nip("197005151998032002")
                .build();
        when(userRepository.save(argThat(user -> user.getPassword().equals(MOCKED_HASHED_PASSWORD)))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("ratna@unisinga.ac.id", user.getEmail());
        assertEquals(Role.DOSEN, user.getRole());
        assertEquals("197005151998032002", user.getNip());
        assertEquals(MOCKED_HASHED_PASSWORD, user.getPassword());
        assertNull(user.getNim());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123Valid");
    }

    @Test
    void testCreateUserSuccessMahasiswa() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Budi Santoso", "budi@ui.ac.id", "MAHASISWA", "password123Valid", "2006123456", null);
        when(userRepository.existsByEmail("budi@ui.ac.id")).thenReturn(false);
        when(userRepository.existsByNim("2006123456")).thenReturn(false);
        when(passwordEncoder.encode("password123Valid")).thenReturn(MOCKED_HASHED_PASSWORD);


        User savedUser = User.builder()
                .id(2L)
                .namaLengkap(userRequest.getNamaLengkap())
                .email(userRequest.getEmail())
                .role(Role.MAHASISWA)
                .password(MOCKED_HASHED_PASSWORD)
                .nim(userRequest.getNim())
                .build();

        when(userRepository.save(argThat(u -> u.getNim().equals("2006123456") && u.getNip() == null && u.getPassword().equals(MOCKED_HASHED_PASSWORD))))
                .thenReturn(savedUser);


        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("budi@ui.ac.id", user.getEmail());
        assertEquals(Role.MAHASISWA, user.getRole());
        assertEquals("2006123456", user.getNim());
        assertEquals(MOCKED_HASHED_PASSWORD, user.getPassword());
        assertNull(user.getNip());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123Valid");
    }

    @Test
    void testCreateUserSuccessAdmin() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Admin Utama", "admin@hiringgo.com", "ADMIN", "adminpassValid", null, null);
        when(userRepository.existsByEmail("admin@hiringgo.com")).thenReturn(false);
        when(passwordEncoder.encode("adminpassValid")).thenReturn(MOCKED_HASHED_PASSWORD);

        User expectedUser = User.builder()
                .id(3L)
                .namaLengkap("Admin Utama")
                .email("admin@hiringgo.com")
                .role(Role.ADMIN)
                .password(MOCKED_HASHED_PASSWORD)
                .build();
        when(userRepository.save(argThat(user -> user.getPassword().equals(MOCKED_HASHED_PASSWORD)))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals("admin@hiringgo.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(MOCKED_HASHED_PASSWORD, user.getPassword());
        assertNull(user.getNip());
        assertNull(user.getNim());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("adminpassValid");
    }


    @Test
    void testCreateUserDuplicateEmail() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dr. Ratna Yuwono", "ratna@unisinga.ac.id", "DOSEN", "password123Valid", null, "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);

        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email sudah terdaftar", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserInvalidRole() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Test User", "test@example.com", "INVALID_ROLE", "password123Valid", null, null);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);

        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Role tidak valid: INVALID_ROLE", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserRequestNull() {
        CompletableFuture<User> futureUser = userService.createUser(null);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Request tidak boleh null", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserEmailNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama", null, "DOSEN", "passwordValid", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, role, dan password tidak boleh null dalam request", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserRoleNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama", "email@test.com", null, "passwordValid", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, role, dan password tidak boleh null dalam request", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserNamaLengkapNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, null, "email@test.com", "DOSEN", "passwordValid", null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, role, dan password tidak boleh null dalam request", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserPasswordNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama Valid", "email@valid.com", "DOSEN", null, null, "nip123");
        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email, nama, role, dan password tidak boleh null dalam request", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserPasswordTooShort() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Nama Valid", "email@valid.com", "DOSEN", "short", null, "nip123");
        // Hapus stubbing userRepository.existsByEmail karena tidak akan tercapai
        // when(userRepository.existsByEmail("email@valid.com")).thenReturn(false);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Password minimal 8 karakter", ex.getCause().getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }


    @Test
    void testCreateUserDosenNipExists() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dosen Baru", "dosenbaru@test.com", "DOSEN", "passwordPanjangValid", null, "NIP001");
        when(userRepository.existsByEmail("dosenbaru@test.com")).thenReturn(false);
        when(userRepository.existsByNip("NIP001")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("NIP sudah terdaftar", ex.getCause().getMessage());
        // Encoder tidak akan dipanggil karena exception NIP sudah ada terjadi sebelumnya
        verify(passwordEncoder, never()).encode("passwordPanjangValid");
    }

    @Test
    void testCreateUserDosenNipNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Dosen Tanpa NIP", "dosen.no.nip@test.com", "DOSEN", "passwordPanjangValid", null, null);
        when(userRepository.existsByEmail("dosen.no.nip@test.com")).thenReturn(false);
        when(passwordEncoder.encode("passwordPanjangValid")).thenReturn(MOCKED_HASHED_PASSWORD);


        User expectedUser = User.builder().id(1L).namaLengkap("Dosen Tanpa NIP").email("dosen.no.nip@test.com").role(Role.DOSEN).password(MOCKED_HASHED_PASSWORD).build();
        when(userRepository.save(argThat(user -> user.getPassword().equals(MOCKED_HASHED_PASSWORD)))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();
        assertNotNull(user);
        assertEquals(Role.DOSEN, user.getRole());
        assertEquals(MOCKED_HASHED_PASSWORD, user.getPassword());
        assertNull(user.getNip());
        verify(passwordEncoder).encode("passwordPanjangValid");
    }


    @Test
    void testCreateUserMahasiswaNimExists() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Mhs Baru", "mhsbaru@test.com", "MAHASISWA", "passwordPanjangValid", "NIM001", null);
        when(userRepository.existsByEmail("mhsbaru@test.com")).thenReturn(false);
        when(userRepository.existsByNim("NIM001")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("NIM sudah terdaftar", ex.getCause().getMessage());
        // Encoder tidak akan dipanggil karena exception NIM sudah ada terjadi sebelumnya
        verify(passwordEncoder, never()).encode("passwordPanjangValid");
    }

    @Test
    void testCreateUserMahasiswaNimNull() {
        UserRequestDTO userRequest = new UserRequestDTO(null, "Mhs Tanpa NIM", "mhs.no.nim@test.com", "MAHASISWA", "passwordPanjangValid", null, null);
        when(userRepository.existsByEmail("mhs.no.nim@test.com")).thenReturn(false);
        when(passwordEncoder.encode("passwordPanjangValid")).thenReturn(MOCKED_HASHED_PASSWORD);

        User expectedUser = User.builder().id(1L).namaLengkap("Mhs Tanpa NIM").email("mhs.no.nim@test.com").role(Role.MAHASISWA).password(MOCKED_HASHED_PASSWORD).build();
        when(userRepository.save(argThat(user -> user.getPassword().equals(MOCKED_HASHED_PASSWORD)))).thenReturn(expectedUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join();
        assertNotNull(user);
        assertEquals(Role.MAHASISWA, user.getRole());
        assertEquals(MOCKED_HASHED_PASSWORD, user.getPassword());
        assertNull(user.getNim());
        verify(passwordEncoder).encode("passwordPanjangValid");
    }

    @Test
    void testUpdateUserRoleSuccess_DosenToAdmin() {
        Long userId = 1L;
        String newRoleStr = "ADMIN";
        User existingUser = User.builder().id(userId).namaLengkap("Test User").email("test@example.com").role(Role.DOSEN).nip("123").password("someOldHashedPassword").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            assertEquals("someOldHashedPassword", userToSave.getPassword());
            return userToSave;
        });

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        assertEquals(Role.ADMIN, optionalUser.get().getRole());
        assertEquals("someOldHashedPassword", optionalUser.get().getPassword());
        assertNull(optionalUser.get().getNip(), "NIP should be null when role changes from DOSEN to ADMIN");
        assertNull(optionalUser.get().getNim(), "NIM should be null for ADMIN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_AdminToDosen() {
        Long userId = 1L;
        String newRoleStr = "DOSEN";
        User existingUser = User.builder().id(userId).namaLengkap("Test Admin").email("admin@example.com").role(Role.ADMIN).password("anotherHashedPassword").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        User updatedUser = optionalUser.get();
        assertEquals(Role.DOSEN, updatedUser.getRole());
        assertEquals("anotherHashedPassword", updatedUser.getPassword());
        assertNull(updatedUser.getNip());
        assertNull(updatedUser.getNim(), "NIM should be null when role changes to DOSEN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_AdminToMahasiswa() {
        Long userId = 1L;
        String newRoleStr = "MAHASISWA";
        User existingUser = User.builder().id(userId).namaLengkap("Test Admin").email("admin@example.com").role(Role.ADMIN).password("hashedPassAdmin").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Optional<User>> futureOptionalUser = userService.updateUserRole(userId, newRoleStr);
        Optional<User> optionalUser = futureOptionalUser.join();

        assertTrue(optionalUser.isPresent());
        User updatedUser = optionalUser.get();
        assertEquals(Role.MAHASISWA, updatedUser.getRole());
        assertEquals("hashedPassAdmin", updatedUser.getPassword());
        assertNull(updatedUser.getNip(), "NIP should be null when role changes to MAHASISWA");
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
        // Tidak perlu mock findById karena validasi role terjadi lebih dulu
        // when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().build()));


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
        User user1 = User.builder().id(1L).namaLengkap("Test User 1").email("test1@mail.com").role(Role.MAHASISWA).password("pass1").build();
        User user2 = User.builder().id(2L).namaLengkap("Test User 2").email("test2@mail.com").role(Role.DOSEN).nip("12345").password("pass2").build();
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
        User foundUser = User.builder().id(userId).namaLengkap("Found User").email("find@mail.com").role(Role.MAHASISWA).password("passFound").build();
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
        User dosen = User.builder().id(1L).namaLengkap("Prof. X").email("prof.x@example.com").role(Role.DOSEN).nip("NIP007").password("passDosen").build();
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
        User mahasiswa = User.builder().id(2L).namaLengkap("Budi").email("budi@example.com").role(Role.MAHASISWA).nim("NIM008").password("passMhs").build();
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
        User admin = User.builder().id(3L).namaLengkap("Admin").email("admin@example.com").role(Role.ADMIN).password("passAdmin").build();
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
        User dosen = User.builder().id(1L).namaLengkap("Prof. X").email("prof.x@example.com").role(Role.DOSEN).nip("NIP007").password("passDosenAsync").build();
        User mahasiswa = User.builder().id(2L).namaLengkap("Budi").email("budi@example.com").role(Role.MAHASISWA).nim("NIM008").password("passMhsAsync").build();
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