package id.ac.ui.cs.advprog.b13.hiringgo.user.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.user.model.User;
import id.ac.ui.cs.advprog.b13.hiringgo.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Untuk setup data

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Anotasi untuk testing JPA components (menggunakan H2 in-memory by default)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Helper untuk persist dan flush entitas

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindById() {
        User user = User.builder()
                .namaLengkap("Budi Santoso")
                .email("budi.jpa@test.com")
                .password("password123")
                .role(Role.MAHASISWA)
                .nim("1234567890")
                .build();

        User savedUser = entityManager.persistFlushFind(user); // Simpan dan dapatkan managed entity

        Optional<User> foundUserOptional = userRepository.findById(savedUser.getId());
        assertTrue(foundUserOptional.isPresent());
        assertEquals("budi.jpa@test.com", foundUserOptional.get().getEmail());
    }

    @Test
    void testFindByEmail_IfFound() {
        User user = User.builder()
                .namaLengkap("Siti Aminah")
                .email("siti.jpa@test.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
        entityManager.persistAndFlush(user);

        Optional<User> foundUserOptional = userRepository.findByEmail("siti.jpa@test.com");
        assertTrue(foundUserOptional.isPresent());
        assertEquals("Siti Aminah", foundUserOptional.get().getNamaLengkap());
    }

    @Test
    void testFindByEmail_IfNotFound() {
        Optional<User> foundUserOptional = userRepository.findByEmail("nonexistent.jpa@test.com");
        assertFalse(foundUserOptional.isPresent());
    }

    @Test
    void testExistsByEmail_IfTrue() {
        User user = User.builder()
                .namaLengkap("Agus Salim")
                .email("agus.jpa@test.com")
                .password("password123")
                .role(Role.DOSEN)
                .nip("NIPAGUS")
                .build();
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByEmail("agus.jpa@test.com"));
    }

    @Test
    void testExistsByEmail_IfFalse() {
        assertFalse(userRepository.existsByEmail("tidakada.jpa@test.com"));
    }

    @Test
    void testExistsByNim_IfTrue() {
        User user = User.builder()
                .namaLengkap("Mahasiswa Nim")
                .email("mahasiswa.nim@test.com")
                .password("password123")
                .role(Role.MAHASISWA)
                .nim("NIMUNIK123")
                .build();
        entityManager.persistAndFlush(user);
        assertTrue(userRepository.existsByNim("NIMUNIK123"));
    }

    @Test
    void testExistsByNip_IfTrue() {
        User user = User.builder()
                .namaLengkap("Dosen Nip")
                .email("dosen.nip@test.com")
                .password("password123")
                .role(Role.DOSEN)
                .nip("NIPUNIK123")
                .build();
        entityManager.persistAndFlush(user);
        assertTrue(userRepository.existsByNip("NIPUNIK123"));
    }


    @Test
    void testFindAllUsers() {
        User user1 = User.builder().namaLengkap("User Satu").email("satu.jpa@test.com").password("p").role(Role.ADMIN).build();
        User user2 = User.builder().namaLengkap("User Dua").email("dua.jpa@test.com").password("p").role(Role.MAHASISWA).nim("NIMDUA").build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        List<User> allUsers = userRepository.findAll();
        // Jumlah user bisa lebih dari 2 jika ada test lain yang persist data dan tidak di-rollback
        // @DataJpaTest biasanya me-rollback transaksi setelah setiap test.
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2); // Cek apakah minimal 2 user yang baru ditambahkan ada
    }

    @Test
    void testDeleteUser() {
        User user = User.builder()
                .namaLengkap("User Untuk Dihapus")
                .email("hapus.jpa@test.com")
                .password("password123")
                .role(Role.MAHASISWA)
                .nim("NIMHAPUS")
                .build();
        User savedUser = entityManager.persistFlushFind(user);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush(); // Pastikan delete di-commit ke DB (in-memory)

        Optional<User> deletedUserOptional = userRepository.findById(userId);
        assertFalse(deletedUserOptional.isPresent());
    }
}
