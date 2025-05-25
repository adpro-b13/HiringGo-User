package id.ac.ui.cs.advprog.b13.hiringgo.user.service;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequestDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import id.ac.ui.cs.advprog.b13.hiringgo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired; // Tambahkan ini
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder; // Tambahkan ini
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Tambahkan ini

    @Autowired // Tambahkan @Autowired atau modifikasi constructor
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // Inisialisasi
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<User> createUser(UserRequestDTO request) {
        if (request == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Request tidak boleh null"));
        }
        if (request.getEmail() == null || request.getRole() == null || request.getNamaLengkap() == null || request.getPassword() == null) { // Password juga wajib
            return CompletableFuture.failedFuture(new IllegalArgumentException("Email, nama, role, dan password tidak boleh null dalam request"));
        }
        if (request.getPassword().length() < 8) { // Contoh validasi password
            return CompletableFuture.failedFuture(new IllegalArgumentException("Password minimal 8 karakter"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Email sudah terdaftar"));
        }

        Role roleEnum;
        try {
            roleEnum = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Role tidak valid: " + request.getRole()));
        }

        if (roleEnum == Role.DOSEN && request.getNip() != null && userRepository.existsByNip(request.getNip())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("NIP sudah terdaftar"));
        }
        if (roleEnum == Role.MAHASISWA && request.getNim() != null && userRepository.existsByNim(request.getNim())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("NIM sudah terdaftar"));
        }

        User user = User.builder()
                .namaLengkap(request.getNamaLengkap())
                .email(request.getEmail())
                .role(roleEnum)
                // HASH PASSWORD SEBELUM DISIMPAN
                .password(passwordEncoder.encode(request.getPassword()))
                .nip(roleEnum == Role.DOSEN ? request.getNip() : null)
                .nim(roleEnum == Role.MAHASISWA ? request.getNim() : null)
                .build();

        User savedUser = userRepository.save(user);
        return CompletableFuture.completedFuture(savedUser);
    }

    @Async
    @Override
    public CompletableFuture<List<User>> listUsers() {
        List<User> users = userRepository.findAll();
        return CompletableFuture.completedFuture(users);
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<Optional<User>> updateUserRole(Long userId, String newRoleStr) {
        Role newRoleEnum;
        try {
            newRoleEnum = Role.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Role baru tidak valid: " + newRoleStr));
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setRole(newRoleEnum);
            if (newRoleEnum != Role.DOSEN) user.setNip(null);
            if (newRoleEnum != Role.MAHASISWA) user.setNim(null);
            User updatedUser = userRepository.save(user);
            return CompletableFuture.completedFuture(Optional.of(updatedUser));
        } else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<Boolean> deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return CompletableFuture.completedFuture(true);
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async
    @Override
    public CompletableFuture<Optional<User>> findById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return CompletableFuture.completedFuture(user);
    }

    public UserResponseDTO convertToUserResponse(User user) {
        if (user == null) return null;
        return new UserResponseDTO(
                user.getId(),
                user.getNamaLengkap(),
                user.getEmail(),
                user.getRole().name(),
                user.getNim(),
                user.getNip()
        );
    }

    @Async
    public CompletableFuture<List<UserResponseDTO>> findAllUsersAsResponseAsync() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> dtos = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(dtos);
    }
}
