package com.vetrifresh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.vetrifresh.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // ✅ This tells Spring Security exactly HOW to authenticate
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService); // your service
        provider.setPasswordEncoder(passwordEncoder());           // BCrypt
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider()) 
            .authorizeHttpRequests(auth -> auth
             .requestMatchers("/admin/**").hasRole("ADMIN")   
                .requestMatchers(
                    "/", "/home", "/products", "/category/**",
                    "/css/**", "/js/**", "/images/**",
                    "/login", "/register" ,
                     "/contact", "/contact/submit",
                    "/about","/blog","/search" ,
                    "/blog/** " ,"/shop", "/shop/**",
                    "/faq", "/terms", "/privacy"
                ).permitAll()

                .requestMatchers("/images/profiles/**").permitAll()
               
                .requestMatchers("/cart/**", "/checkout", "/checkout/**", "/orders/**" ,
                                             "/order/**" , "/payment/**" ,"/profile", "/wishlist/**" ,
                                              "/my-orders/**", "/orders").authenticated()
               
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
            .successHandler(customSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );

        return http.build();
    }

    @Bean
public AuthenticationSuccessHandler customSuccessHandler() {
    return (request, response, authentication) -> {
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/home");
        }
    };
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}