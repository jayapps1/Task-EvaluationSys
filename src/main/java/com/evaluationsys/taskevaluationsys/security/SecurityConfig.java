package com.evaluationsys.taskevaluationsys.security;

import com.evaluationsys.taskevaluationsys.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Custom authentication success handler for role-based redirects
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/dashboard");
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"))) {
                response.sendRedirect("/supervisor/dashboard");
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                response.sendRedirect("/staff/dashboard");
            } else {
                response.sendRedirect("/auth/login?error");
            }
        };
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // ✅ PUBLIC - Login page and static files
                        .requestMatchers("/auth/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // ✅ PUBLIC - Forgot Password Flow
                        .requestMatchers("/auth/forgot-password").permitAll()
                        .requestMatchers("/auth/verify-otp").permitAll()
                        .requestMatchers("/auth/reset-password").permitAll()
                        .requestMatchers("/auth/password/**").permitAll()

                        // ✅ PUBLIC - Index/Home page
                        .requestMatchers("/", "/index").permitAll()

                        // ✅ AUTHENTICATED - Profile pages (any logged-in user)
                        .requestMatchers("/profile/**").authenticated()

                        // ✅ ADMIN ONLY
                        .requestMatchers("/auth/register").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ✅ SUPERVISOR
                        .requestMatchers("/supervisor/**").hasRole("SUPERVISOR")

                        // ✅ STAFF
                        .requestMatchers("/staff/**").hasRole("STAFF")

                        // EVERYTHING ELSE MUST BE AUTHENTICATED
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("staffCode")
                        .passwordParameter("password")
                        .successHandler(successHandler())
                        .failureUrl("/auth/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .invalidSessionUrl("/auth/login")
                        .maximumSessions(1)
                )

                .rememberMe(rm -> rm
                        .key("uniqueAndSecretKey123")
                        .tokenValiditySeconds(86400)
                        .userDetailsService(userDetailsService)
                )

                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}