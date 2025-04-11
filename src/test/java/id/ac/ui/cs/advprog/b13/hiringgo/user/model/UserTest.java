package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

import dto.UserRequest;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCreateUserWithValidRequest() {
        UserRequest req = new UserRequest("1337", "oppa@dev.id", "Oppa", "ADMIN");
        User user = new User(req);

        assertEquals("1337", user.getId());
        assertEquals("oppa@dev.id", user.getEmail());
        assertEquals("Oppa", user.getName());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void testCreateUserWithInvalidRole() {
        UserRequest req = new UserRequest("1337", "oppa@dev.id", "Oppa", "HACKER");

        assertThrows(IllegalArgumentException.class, () -> new User(req));
    }
}
