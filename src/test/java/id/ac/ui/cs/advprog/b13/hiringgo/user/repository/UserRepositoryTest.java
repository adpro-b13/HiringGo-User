package id.ac.ui.cs.advprog.b13.hiringgo.user.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();

        UserRequest req1 = new UserRequest("1", "budi@gmail.com", "Budi Santoso", "MAHASISWA");
        UserRequest req2 = new UserRequest("2", "siti@gmail.com", "Siti Aminah", "ADMIN");

        user1 = new User(req1);
        user2 = new User(req2);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    void testSaveNewUser() {
        UserRequest req3 = new UserRequest("3", "agus@gmail.com", "Agus Salim", "DOSEN");
        User user3 = new User(req3);
        User result = userRepository.save(user3);

        assertEquals("agus@gmail.com", result.getEmail());
        assertEquals(3, userRepository.findAll().size());
    }

    @Test
    void testSaveUpdateUser() {
        UserRequest updatedReq = new UserRequest("1", "budi@gmail.com", "Budi Update", "ADMIN");
        User updatedUser = new User(updatedReq);
        User result = userRepository.save(updatedUser);

        assertEquals("Budi Update", result.getName());
        assertEquals("ADMIN", result.getRole());
        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    void testFindByIdIfFound() {
        User result = userRepository.findById("1");
        assertNotNull(result);
        assertEquals("budi@gmail.com", result.getEmail());
    }

    @Test
    void testFindByIdIfNotFound() {
        User result = userRepository.findById("999");
        assertNull(result);
    }

    @Test
    void testFindByEmailIfFound() {
        User result = userRepository.findByEmail("siti@gmail.com");
        assertNotNull(result);
        assertEquals("Siti Aminah", result.getName());
    }

    @Test
    void testFindByEmailIfNotFound() {
        User result = userRepository.findByEmail("nonexistent@email.com");
        assertNull(result);
    }

    @Test
    void testFindAllUsers() {
        List<User> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
    }
}
