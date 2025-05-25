package id.ac.ui.cs.advprog.b13.hiringgo.user.config;

import id.ac.ui.cs.advprog.b13.hiringgo.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Untuk @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Meskipun user-service mungkin tidak melakukan login username/password sendiri,
        // PasswordEncoder tetap dibutuhkan untuk service yang menyimpan User entity (seperti createUser)
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF untuk API stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Gunakan session stateless
                )
                .authorizeHttpRequests(authz -> authz
                        // Endpoint publik (jika ada, misal health check)
                        // .requestMatchers("/public/**").permitAll()

                        // Semua endpoint di bawah /user/** memerlukan otentikasi
                        // dan akan dicek role-nya dengan @PreAuthorize di controller
                        .requestMatchers("/user/**").authenticated()

                        // Endpoint untuk Swagger (jika Anda akan menambahkannya)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated() // Semua request lain juga harus terotentikasi
                )
                // Tambahkan filter JWT sebelum filter username/password standar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}