package repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();

        user1 = new User("1", "budi", "Budi Santoso", UserRole.CUSTOMER.getValue());
        user2 = new User("2", "siti", "Siti Aminah", UserRole.ADMIN.getValue());

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    void testSaveNewUser() {
        User user3 = new User("3", "agus", "Agus Salim", UserRole.CUSTOMER.getValue());
        User result = userRepository.save(user3);

        assertEquals("agus", result.getUsername());
        assertEquals(3, userRepository.findAll().size());
    }

    @Test
    void testSaveUpdateUser() {
        User updatedUser = new User("1", "budi", "Budi Update", UserRole.ADMIN.getValue());
        User result = userRepository.save(updatedUser);

        assertEquals("Budi Update", result.getName());
        assertEquals(UserRole.ADMIN.getValue(), result.getRole());
        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    void testFindByIdIfFound() {
        User result = userRepository.findById("1");
        assertNotNull(result);
        assertEquals("budi", result.getUsername());
    }

    @Test
    void testFindByIdIfNotFound() {
        User result = userRepository.findById("999");
        assertNull(result);
    }

    @Test
    void testFindByUsernameIfFound() {
        User result = userRepository.findByUsername("siti");
        assertNotNull(result);
        assertEquals("Siti Aminah", result.getName());
    }

    @Test
    void testFindByUsernameIfNotFound() {
        User result = userRepository.findByUsername("nonexistent");
        assertNull(result);
    }

    @Test
    void testFindAllUsers() {
        List<User> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
    }
}
