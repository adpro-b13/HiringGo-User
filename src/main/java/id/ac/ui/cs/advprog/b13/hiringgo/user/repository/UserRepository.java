package id.ac.ui.cs.advprog.b13.hiringgo.user.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    private List<User> userData = new ArrayList<>();

    public User save(User user) {
        return null;
    }

    public User findById(String id) {
        return null;
    }

    public User findByEmail(String email) {
        return null;
    }

    public List<User> findAll() {
        return new ArrayList<>();
    }
}
