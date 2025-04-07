package com.humber.sleepPlanRepeat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    // Constructor injection.
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Password hash encryption using BCrypt.
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Manages encryption of user details.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder()) // Utilize BCrypt here.
                .and()
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        // Public pages that be accessed without having to log-in.
                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/sleepplanrepeat/landing", "/sleepplanrepeat/", "/login").permitAll()
                        .requestMatchers("/sleepplanrepeat/day").permitAll()
                        .requestMatchers("/sleepplanrepeat/calendar", "/sleepplanrepeat/calendar/**").permitAll()
                        .requestMatchers("/sleepplanrepeat/events/view/**").permitAll()

                        // Private pages that require user authentication / log-in.
                        .requestMatchers(
                                "/sleepplanrepeat/events/create", "/sleepplanrepeat/events/create-global",
                                "/sleepplanrepeat/events/edit/**", "/sleepplanrepeat/events/delete/**")
                        .authenticated()
                        .anyRequest()
                        .authenticated()
                )

                // Log-in form success/failure redirection.
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/sleepplanrepeat/calendar")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Log-out form redirection.
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?message=You have been logged out")
                        .permitAll()
                );

        return http.build();
    }
}