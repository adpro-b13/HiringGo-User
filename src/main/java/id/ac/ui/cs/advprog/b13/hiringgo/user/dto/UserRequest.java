package id.ac.ui.cs.advprog.b13.hiringgo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRequest {
    private String id;
    private String email;
    private String name;
    private String role;
}
