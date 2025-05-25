package id.ac.ui.cs.advprog.b13.hiringgo.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String jwtSecret;
    private SecretKey jwtSecretKey;
    private long jwtExpirationInMs;

    @BeforeEach
    void setUp() {
        // Gunakan secret yang cukup panjang untuk HS256 (minimal 256 bits / 32 bytes)
        jwtSecret = "IniAdalahContohSecretKeyYangSangatPanjangDanAmanUntukTesting";
        jwtExpirationInMs = TimeUnit.HOURS.toMillis(1); // 1 jam

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecretString", jwtSecret);
        jwtTokenProvider.init(); // Panggil init secara manual

        // Dapatkan secret key yang sudah diinisialisasi untuk membuat token tes
        jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private String generateTestToken(String subject, Long userId, String roles, Date expirationDate, SecretKey key) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(key) // Gunakan key yang sama dengan yang di-init provider
                .compact();
    }

    @Test
    void testGetUsernameFromJwt_ValidToken() {
        String username = "testuser@example.com";
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken(username, 1L, "ROLE_USER", expiryDate, jwtSecretKey);

        String extractedUsername = jwtTokenProvider.getUsernameFromJwt(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetAuthoritiesFromJwt_ValidTokenWithRoles() {
        String roles = "ROLE_USER,ROLE_ADMIN";
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken("testuser", 1L, roles, expiryDate, jwtSecretKey);

        Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJwt(token);
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        List<String> authorityStrings = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        assertTrue(authorityStrings.contains("ROLE_USER"));
        assertTrue(authorityStrings.contains("ROLE_ADMIN"));
    }

    @Test
    void testGetAuthoritiesFromJwt_TokenWithNullRoles() {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken("testuser", 1L, null, expiryDate, jwtSecretKey);

        Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJwt(token);
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testGetAuthoritiesFromJwt_TokenWithEmptyRolesString() {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken("testuser", 1L, "", expiryDate, jwtSecretKey);

        Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJwt(token);
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testGetUserIdFromJwt_ValidToken() {
        Long userId = 123L;
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken("testuser", userId, "ROLE_USER", expiryDate, jwtSecretKey);

        Long extractedUserId = jwtTokenProvider.getUserIdFromJwt(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken_ValidToken() {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        String token = generateTestToken("testuser", 1L, "ROLE_USER", expiryDate, jwtSecretKey);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        Date expiryDate = new Date(System.currentTimeMillis() - jwtExpirationInMs); // Sudah kedaluwarsa
        String token = generateTestToken("testuser", 1L, "ROLE_USER", expiryDate, jwtSecretKey);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_MalformedToken() {
        String malformedToken = "this.is.not.a.jwt";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    
    @Test
    void testValidateToken_EmptyClaims() {
        // IllegalArgumentException biasanya jika string token adalah null atau kosong
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(" "));
    }
}