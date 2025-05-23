package com.humber.sleepPlanRepeat.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import HttpMethod
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

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/sleepplanrepeat/events/ai-create")
//                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/sleepplanrepeat/landing", "/sleepplanrepeat/", "/login").permitAll()
                        .requestMatchers("/sleepplanrepeat/day").permitAll()
                        .requestMatchers("/sleepplanrepeat/calendar", "/sleepplanrepeat/calendar/**").permitAll()
                        .requestMatchers("/sleepplanrepeat/events/view/**").permitAll()
                        .requestMatchers("/sleepplanrepeat/events/question").permitAll()

                        // Allow POST requests to /sleepplanrepeat/events/ai-create for all users
                        .requestMatchers(HttpMethod.POST, "/sleepplanrepeat/events/ai-create").permitAll()

                        // Restrict access to the AI functionality (GET requests) to users with the "USER" role or higher.
                        .requestMatchers("/sleepplanrepeat/create-event-from-ai", "/sleepplanrepeat/suggest-schedule")
                        .hasRole("USER")

                        .requestMatchers(
                                "/sleepplanrepeat/events/create", "/sleepplanrepeat/events/create-global",
                                "/sleepplanrepeat/events/edit/**", "/sleepplanrepeat/events/delete/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/sleepplanrepeat/calendar")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?message=You have been logged out")
                        .permitAll()
                );

        return http.build();
    }
}