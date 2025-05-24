package id.ac.ui.cs.advprog.b13.hiringgo.user.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final List<User> userData = new ArrayList<>();

    public User save(User user) {
        deleteById(user.getId());
        userData.add(user);
        return user;
    }

    public User findById(String id) {
        return userData.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User findByEmail(String email) {
        return userData.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public List<User> findAll() {
        return new ArrayList<>(userData);
    }

    public boolean deleteById(String id) {
        return userData.removeIf(user -> user.getId().equals(id));
    }

    public boolean existsByEmail(String email) {
        return userData.stream().anyMatch(user -> user.getEmail().equals(email));
    }

    public boolean existsByNip(String nip) {
        return userData.stream().anyMatch(user -> nip != null && nip.equals(user.getNip()));
    }
}
