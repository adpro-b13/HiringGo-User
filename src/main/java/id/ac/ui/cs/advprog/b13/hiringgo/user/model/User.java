package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.UserRole;
import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
    private final String id;
    private final String email;
    private final String name;
    private final UserRole role;
    private final String nip;

    public User(UserRequest request) {
        if (!UserRole.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        this.id = generateId(); // misal: USR-XXXXXX
        this.email = request.getEmail();
        this.name = request.getName();
        this.role = UserRole.valueOf(request.getRole());

        if (this.role == UserRole.DOSEN && (request.getNip() == null || request.getNip().isEmpty())) {
            throw new IllegalArgumentException("NIP is required for role DOSEN");
        }

        this.nip = this.role == UserRole.DOSEN ? request.getNip() : null;
    }

    private String generateId() {
        return "USR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
