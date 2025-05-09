package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testCreateUserSuccess() {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");

        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(false);
        when(userRepository.existsByNip("197005151998032002")).thenReturn(false);

        User mockUser = new User(userRequest);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User user = userService.createUser(userRequest);

        assertEquals("ratna@unisinga.ac.id", user.getEmail());
        assertEquals("Dr. Ratna Yuwono", user.getName());
        assertEquals(UserRole.DOSEN, user.getRole());
        assertEquals("197005151998032002", user.getNip());
    }
    @Test
    void testCreateUserDuplicateEmail() {
        UserRequest userRequest = new UserRequest("ratna@unisinga.ac.id", "Dr. Ratna Yuwono", "DOSEN", "197005151998032002");
        when(userRepository.existsByEmail("ratna@unisinga.ac.id")).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userRequest);
        });
        assertEquals("Email sudah terdaftar", ex.getMessage());
    }

    @Test
void testUpdateUserRoleSuccess() {
    String userId = "USR-123ABC";
    String newRole = "ADMIN";
    User user = new User(new UserRequest("x@y.com", "Nama", "DOSEN", "123"));
    // Override id supaya test stabil
    User userWithFixedId = new User(new UserRequest("x@y.com", "Nama", "DOSEN", "123")) {
        @Override
        public String getId() { return userId; }
    };

    when(userRepository.findById(userId)).thenReturn(userWithFixedId);

    doReturn(userWithFixedId).when(userRepository).save(any());

    assertDoesNotThrow(() -> userService.updateUserRole(userId, newRole));
}

@Test
void testUpdateUserRoleInvalidRole() {
    String userId = "USR-123ABC";
    String newRole = "INVALID";
    when(userRepository.findById(userId)).thenReturn(new User(new UserRequest("a@b.c", "Abc", "ADMIN", null)));

    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
        userService.updateUserRole(userId, newRole);
    });
    assertEquals("Invalid role", ex.getMessage());
}

@Test
void testUpdateUserRoleUserNotFound() {
    String userId = "USR-TIDAKADA";
    when(userRepository.findById(userId)).thenReturn(null);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
        userService.updateUserRole(userId, "ADMIN");
    });
    assertEquals("User tidak ditemukan", ex.getMessage());
}

@Test
void testDeleteUserSuccess() {
    String userId = "USR-123ABC";
    when(userRepository.deleteById(userId)).thenReturn(true);

    assertDoesNotThrow(() -> userService.deleteUser(userId));
}

@Test
void testDeleteUserNotFound() {
    String userId = "USR-999888";
    when(userRepository.deleteById(userId)).thenReturn(false);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
        userService.deleteUser(userId);
    });
    assertEquals("User tidak ditemukan", ex.getMessage());
}
}