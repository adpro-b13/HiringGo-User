package id.ac.ui.cs.advprog.b13.hiringgo.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // Spring Security User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority;


import java.io.IOException;
import java.util.Collection;

@Component // Daftarkan sebagai Spring Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Tidak memerlukan UserDetailsService yang load dari DB user-service
    // karena info user (email, roles) akan diambil dari JWT.

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromJwt(jwt); // Ini email
                Collection<? extends GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromJwt(jwt);
                Long userId = tokenProvider.getUserIdFromJwt(jwt); // Ambil userId juga

                // Buat UserDetails dari Spring Security, bukan dari model JPA kita
                // Kita bisa pass userId jika perlu dengan membuat custom UserDetails
                // Untuk sekarang, username (email) dan authorities sudah cukup
                UserDetails userDetails = new User(username, "", authorities);
                // Jika Anda ingin menyimpan userId di Principal, Anda bisa membuat custom UserDetails
                // atau menggunakan atribut dalam UsernamePasswordAuthenticationToken.
                // Contoh: CustomUserDetails customUserDetails = new CustomUserDetails(username, "", authorities, userId);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Anda bisa menyimpan userId dalam details jika diperlukan
                // Map<String, Object> details = new HashMap<>();
                // details.put("userId", userId);
                // authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request, details));
                // Atau lebih sederhana, set request attribute jika hanya diperlukan di controller
                // request.setAttribute("userId", userId);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}