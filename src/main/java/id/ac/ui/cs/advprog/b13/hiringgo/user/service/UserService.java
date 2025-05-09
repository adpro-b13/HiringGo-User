package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequest request);
    List<User> listUsers();
    void updateUserRole(String userId, String newRole);
    void deleteUser(String userId);
    User findById(String userId);
}