package com.web.submission_portal.config;

import com.web.submission_portal.security.CustomAuthenticationSuccessHandler;
import com.web.submission_portal.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomUserDetailsService customUserDetailsService;

    SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                   CustomUserDetailsService customUserDetailsService) {
        this.successHandler = successHandler;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/").permitAll()
                        .requestMatchers("/auth/**","/contact","/","/auth/register").permitAll()
                        .requestMatchers("/ws/**").permitAll()
			            .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/cr/**").hasAuthority("ROLE_CR")
                        .requestMatchers("/student/**").hasAnyAuthority("ROLE_STUDENT","ROLE_CR")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**")
                )
                .formLogin(form->form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(successHandler)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout( logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session ->session
			            .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
