package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async; // Import ini
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture; // Import ini

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Async // Anotasi @Async untuk menjalankan metode ini di thread terpisah
    @Override
    public CompletableFuture<User> createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        if (UserRole.DOSEN.name().equalsIgnoreCase(request.getRole())
                && userRepository.existsByNip(request.getNip())) {
            throw new IllegalArgumentException("NIP sudah terdaftar");
        }
        User user = new User(request);
        return CompletableFuture.completedFuture(userRepository.save(user));
    }

    @Async
    @Override
    public CompletableFuture<List<User>> listUsers() {
        return CompletableFuture.completedFuture(userRepository.findAll());
    }

    @Async
    @Override
    public CompletableFuture<Void> updateUserRole(String userId, String newRole) {
        if (!UserRole.contains(newRole)) {
            throw new IllegalArgumentException("Invalid role");
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }
        String nip = UserRole.DOSEN.name().equalsIgnoreCase(newRole) ? user.getNip() : null;
        User updated = new User(new UserRequest(user.getEmail(), user.getName(), newRole, nip)) {
            @Override
            public String getId() {
                return user.getId();
            }
        };
        userRepository.deleteById(userId);
        userRepository.save(updated);
        return CompletableFuture.completedFuture(null); // Mengembalikan CompletableFuture<Void>
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteUser(String userId) {
        if (!userRepository.deleteById(userId)) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }
        return CompletableFuture.completedFuture(null); // Mengembalikan CompletableFuture<Void>
    }

    @Async
    @Override
    public CompletableFuture<User> findById(String userId) {
        return CompletableFuture.completedFuture(userRepository.findById(userId));
    }
}