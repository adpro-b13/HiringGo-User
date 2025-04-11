package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

import id.ac.ui.cs.advprog.b13.hiringgo.user.dto.UserRequest;
import lombok.Getter;

@Getter
public class User {
    private String id;
    private String email;
    private String name;
    private String role;

    public User(UserRequest request) {
        if (!UserRole.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        this.id = request.getId();
        this.email = request.getEmail();
        this.name = request.getName();
        this.role = request.getRole();
    }
}
