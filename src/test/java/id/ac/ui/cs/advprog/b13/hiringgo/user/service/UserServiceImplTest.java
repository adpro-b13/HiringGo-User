package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync; // Penting untuk enable async di test context
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture; // Import ini
import java.util.concurrent.CompletionException; // Import ini

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Anotasi ini akan memuat Spring Boot context penuh untuk test ini
@SpringBootTest
class UserServiceImplTest {

    @MockBean // Mock UserRepository agar tidak berinteraksi dengan database sesungguhnya
    private UserRepository userRepository;

    @Autowired // Inject UserService yang sudah di-proxy oleh Spring
    private UserService userService;

    // Tambahkan konfigurasi untuk mengaktifkan @Async di test context
    @Configuration
    @EnableAsync
    @Import(UserServiceImpl.class) // Import UserServiceImpl agar menjadi bean
    static class TestConfig {}


    @Test
    void testCreateUserSuccess() {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(false);
        when(userRepository.existsByNip("197005151998032002")).thenReturn(false);
        User mockUser = new User(userRequest);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);
        User user = futureUser.join(); // `.join()` akan memblokir dan mengembalikan hasil

        assertEquals("ratna@unisinga.ac.id", user.getEmail());
        assertEquals("Dr. Ratna Yuwono", user.getName());
        assertEquals(UserRole.DOSEN, user.getRole());
        assertEquals("197005151998032002", user.getNip());
    }

    @Test
    void testCreateUserDuplicateEmail() {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(true);

        CompletableFuture<User> futureUser = userService.createUser(userRequest);

        // Harapannya, CompletionException yang membungkus IllegalArgumentException
        CompletionException ex = assertThrows(CompletionException.class, futureUser::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Email sudah terdaftar", ex.getCause().getMessage());
    }

    @Test
    void testUpdateUserRoleSuccess() {
        String userId = "USR-123ABC";
        String newRole = "ADMIN";
        User userWithFixedId = new User(new UserRequest("x@y.com", "Nama", "DOSEN", "123")) {
            @Override
            public String getId() { return userId; }
        };
        when(userRepository.findById(userId)).thenReturn(userWithFixedId);
        when(userRepository.save(any(User.class))).thenReturn(userWithFixedId);
        when(userRepository.deleteById(userId)).thenReturn(true);

        CompletableFuture<Void> future = userService.updateUserRole(userId, newRole);
        assertDoesNotThrow(future::join);
    }

    @Test
    void testUpdateUserRoleInvalidRole() {
        String userId = "USR-123ABC";
        String newRole = "INVALID";
        when(userRepository.findById(userId)).thenReturn(new User(new UserRequest("a@b.c", "Abc", "ADMIN", null)));

        CompletableFuture<Void> future = userService.updateUserRole(userId, newRole);

        CompletionException ex = assertThrows(CompletionException.class, future::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("Invalid role", ex.getCause().getMessage());
    }

    @Test
    void testUpdateUserRoleUserNotFound() {
        String userId = "USR-TIDAKADA";
        when(userRepository.findById(userId)).thenReturn(null);

        CompletableFuture<Void> future = userService.updateUserRole(userId, "ADMIN");

        CompletionException ex = assertThrows(CompletionException.class, future::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("User tidak ditemukan", ex.getCause().getMessage());
    }

    @Test
    void testDeleteUserSuccess() {
        String userId = "USR-123ABC";
        when(userRepository.deleteById(userId)).thenReturn(true);

        CompletableFuture<Void> future = userService.deleteUser(userId);
        assertDoesNotThrow(future::join);
    }

    @Test
    void testDeleteUserNotFound() {
        String userId = "USR-999888";
        when(userRepository.deleteById(userId)).thenReturn(false);

        CompletableFuture<Void> future = userService.deleteUser(userId);

        CompletionException ex = assertThrows(CompletionException.class, future::join);
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertEquals("User tidak ditemukan", ex.getCause().getMessage());
    }

    @Test
    void testListUsersSuccess() {
        UserRequest userRequest1 = new UserRequest("test1@mail.com", "Test User 1", "MAHASISWA", null);
        User user1 = new User(userRequest1);
        UserRequest userRequest2 = new UserRequest("test2@mail.com", "Test User 2", "DOSEN", "12345");
        User user2 = new User(userRequest2);

        List<User> mockUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(mockUsers);

        CompletableFuture<List<User>> futureUsers = userService.listUsers();
        List<User> users = futureUsers.join();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Test User 1", users.get(0).getName());
        assertEquals("Test User 2", users.get(1).getName());
    }

    @Test
    void testFindByIdSuccess() {
        String userId = "USR-FIND";
        UserRequest userRequest = new UserRequest("find@mail.com", "Found User", "MAHASISWA", null);
        User foundUser = new User(userRequest) {
            @Override
            public String getId() { return userId; }
        };

        when(userRepository.findById(userId)).thenReturn(foundUser);

        CompletableFuture<User> futureUser = userService.findById(userId);
        User user = futureUser.join();

        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals("Found User", user.getName());
    }

    @Test
    void testFindByIdNotFound() {
        String userId = "USR-NOTFOUND";
        when(userRepository.findById(userId)).thenReturn(null);

        CompletableFuture<User> futureUser = userService.findById(userId);
        User user = futureUser.join();

        assertNull(user);
    }
}