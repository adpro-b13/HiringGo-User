package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        if (UserRole.DOSEN.name().equalsIgnoreCase(request.getRole())
                && userRepository.existsByNip(request.getNip())) {
            throw new IllegalArgumentException("NIP sudah terdaftar");
        }
        User user = new User(request);
        return userRepository.save(user);
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUserRole(String userId, String newRole) {
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
    }

    @Override
    public void deleteUser(String userId) {
        if (!userRepository.deleteById(userId)) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }
    }

    @Override
    public User findById(String userId) {
        return userRepository.findById(userId);
    }
}