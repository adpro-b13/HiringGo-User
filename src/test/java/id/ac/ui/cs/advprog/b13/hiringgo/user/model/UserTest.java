package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role; // Pastikan import enum Role sudah benar
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCreateUserWithBuilder() {
        User user = User.builder()
                .id(1L) // ID adalah Long
                .namaLengkap("Oppa Ganteng")
                .email("oppa@dev.id")
                .password("password123") // Entity User memiliki field password
                .role(Role.ADMIN)
                .nim(null) // Contoh untuk Admin
                .nip(null)  // Contoh untuk Admin
                .build();

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Oppa Ganteng", user.getNamaLengkap());
        assertEquals("oppa@dev.id", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertNull(user.getNim());
        assertNull(user.getNip());
    }

    @Test
    void testCreateUserDosenWithBuilder() {
        User user = User.builder()
                .namaLengkap("Dr. Ratna")
                .email("ratna@unisinga.ac.id")
                .password("securepass")
                .role(Role.DOSEN)
                .nip("197005151998032002")
                .build();

        assertNotNull(user);
        assertEquals("Dr. Ratna", user.getNamaLengkap());
        assertEquals(Role.DOSEN, user.getRole());
        assertEquals("197005151998032002", user.getNip());
        assertNull(user.getNim()); // NIM harus null untuk Dosen
    }

    @Test
    void testCreateUserMahasiswaWithBuilder() {
        User user = User.builder()
                .namaLengkap("Budi Santoso")
                .email("budi@student.unisinga.ac.id")
                .password("passwordbudi")
                .role(Role.MAHASISWA)
                .nim("2206123456")
                .build();

        assertNotNull(user);
        assertEquals("Budi Santoso", user.getNamaLengkap());
        assertEquals(Role.MAHASISWA, user.getRole());
        assertEquals("2206123456", user.getNim());
        assertNull(user.getNip()); // NIP harus null untuk Mahasiswa
    }

    // Tidak ada lagi constructor User(UserRequestDTO) secara langsung di model User jika hanya pakai builder/allargs
    // Pengujian validasi input DTO lebih cocok di service layer atau controller test.
    // Jika Anda ingin menguji validasi pada level DTO, itu akan menjadi test untuk DTO itu sendiri.
    // Jika User memiliki @AllArgsConstructor, Anda bisa mengujinya, tapi builder lebih fleksibel.

    // Contoh pengujian setter jika ada (Lombok @Data sudah menyediakan setter)
    @Test
    void testSetRole() {
        User user = User.builder().role(Role.MAHASISWA).build();
        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }
}
