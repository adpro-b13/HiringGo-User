package id.ac.ui.cs.advprog.b13.hiringgo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String email;
    private String name;
    private String role;
    private String nip; // Optional, hanya jika role == DOSEN
}
