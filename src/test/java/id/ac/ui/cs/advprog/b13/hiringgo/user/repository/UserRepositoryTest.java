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

        UserRequest req1 = new UserRequest("budi@gmail.com", "Budi Santoso", "MAHASISWA", null);
        UserRequest req2 = new UserRequest("siti@gmail.com", "Siti Aminah", "ADMIN", null);

        user1 = new User(req1);
        user2 = new User(req2);

        userRepository.save(user1);
        userRepository.save(user2);
    }


    @Test
    void testSaveNewUser() {
        UserRequest req3 = new UserRequest("agus@gmail.com", "Agus Salim", "DOSEN", "197005151998032002");
        User user3 = new User(req3);
        User result = userRepository.save(user3);

        assertEquals("agus@gmail.com", result.getEmail());
        assertEquals(3, userRepository.findAll().size());
    }


    @Test
    void testSaveUpdateUser() {
        UserRequest updatedReq = new UserRequest("budi@gmail.com", "Budi Update", "ADMIN", null);
        User updatedUser = new User(updatedReq);
        userRepository.save(updatedUser); // ini hanya add baru karena ID berbeda

        assertEquals(3, userRepository.findAll().size());
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
