package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<User> createUser(UserRequest request); // Ubah return type
    CompletableFuture<List<User>> listUsers(); // Ubah return type
    CompletableFuture<Void> updateUserRole(String userId, String newRole); // Ubah return type
    CompletableFuture<Void> deleteUser(String userId); // Ubah return type
    CompletableFuture<User> findById(String userId); // Ubah return type
}