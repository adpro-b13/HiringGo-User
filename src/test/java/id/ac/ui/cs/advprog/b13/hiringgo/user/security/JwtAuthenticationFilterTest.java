package id.ac.ui.cs.advprog.b13.hiringgo.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
// import java.util.List; // Tidak lagi digunakan secara langsung untuk variabel authorities

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void doFilterInternal_validToken_shouldSetAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        Long userId = 1L;
        // Deklarasikan tipe eksplisit yang akan dikembalikan
        Collection<? extends GrantedAuthority> authoritiesToReturn = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJwt(token)).thenReturn(username);

        // PERUBAHAN DI SINI: Gunakan doReturn(...).when(...)
        doReturn(authoritiesToReturn).when(tokenProvider).getAuthoritiesFromJwt(token);

        when(tokenProvider.getUserIdFromJwt(token)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

        verify(filterChain).doFilter(request, response);
    }

    // ... sisa tes Anda tetap sama ...

    @Test
    void doFilterInternal_noToken_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenNotBearer_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic somecredentials");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyTokenAfterBearer_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_invalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenProviderThrowsException_shouldNotSetAuthenticationAndLog() throws ServletException, IOException {
        String token = "problematic.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJwt(token)).thenThrow(new RuntimeException("Error parsing username"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenProviderValidateTokenThrowsException_shouldNotSetAuthentication() throws ServletException, IOException {
        String token = "problematic.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void getJwtFromRequest_validBearerToken_returnsToken() {
        String token = "my.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        String extractedToken = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "getJwtFromRequest", request);
        assertEquals(token, extractedToken);
    }

    @Test
    void getJwtFromRequest_noAuthorizationHeader_returnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        String extractedToken = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "getJwtFromRequest", request);
        assertNull(extractedToken);
    }

    @Test
    void getJwtFromRequest_notBearerToken_returnsNull() {
        when(request.getHeader("Authorization")).thenReturn("Basic someothertoken");
        String extractedToken = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "getJwtFromRequest", request);
        assertNull(extractedToken);
    }

    @Test
    void getJwtFromRequest_bearerTokenEmptyString_returnsEmptyString() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        String extractedToken = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "getJwtFromRequest", request);
        assertEquals("", extractedToken);
    }

    @Test
    void getJwtFromRequest_bearerTokenOnlyBearer_returnsNull() {
        when(request.getHeader("Authorization")).thenReturn("BearerTokenWithoutSpace");
        String extractedToken = ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "getJwtFromRequest", request);
        assertNull(extractedToken);
    }
}