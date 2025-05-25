package id.ac.ui.cs.advprog.b13.hiringgo.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String namaLengkap;
    private String email;
    private String role;
    private String nim;
    private String nip;
}
