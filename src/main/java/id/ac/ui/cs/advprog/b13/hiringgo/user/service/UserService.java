package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<User> createUser(UserRequestDTO request);
    CompletableFuture<List<User>> listUsers();
    CompletableFuture<Optional<User>> updateUserRole(Long userId, String newRole);
    CompletableFuture<Boolean> deleteUser(Long userId);
    CompletableFuture<Optional<User>> findById(Long userId);
}