package com.web.submission_portal.config;

import com.web.submission_portal.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/css/**","/js/**","/images/**","/").permitAll()
                        .requestMatchers("/auth/**","/login","/forget-password","reset-password").permitAll()
                        .requestMatchers("/cr/**").hasAuthority("ROLE_CR")
                        .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form->form
                        .loginPage("auth/login")
                        .loginProcessingUrl("auth/login")
                        .defaultSuccessUrl("/default-dashboard",true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }
}
