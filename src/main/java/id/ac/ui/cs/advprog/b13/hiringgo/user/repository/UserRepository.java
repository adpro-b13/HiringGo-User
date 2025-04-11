package id.ac.ui.cs.advprog.b13.hiringgo.user.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private final List<User> userData = new ArrayList<>();

    public User save(User user) {
        for (int i = 0; i < userData.size(); i++) {
            if (userData.get(i).getId().equals(user.getId())) {
                userData.set(i, user);
                return user;
            }
        }
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
}
