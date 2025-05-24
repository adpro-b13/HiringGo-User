package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCreateUserWithValidRequest() {
        UserRequest req = new UserRequest("oppa@dev.id", "Oppa", "ADMIN", null);
        User user = new User(req);

        assertNotNull(user.getId()); // id sekarang auto-generated
        assertEquals("oppa@dev.id", user.getEmail());
        assertEquals("Oppa", user.getName());
        assertEquals("ADMIN", user.getRole().name());
    }


    @Test
    void testCreateUserWithInvalidRole() {
        UserRequest req = new UserRequest("1337", "oppa@dev.id", "Oppa", "HACKER");

        assertThrows(IllegalArgumentException.class, () -> new User(req));
    }
}
