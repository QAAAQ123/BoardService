package com.example.BoardService;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
//gemini
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .requestMatchers(HttpMethod.GET,"/", "/login", "/join", "/main","/*.html").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/login","/api/join").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(auth->auth.disable())
                .formLogin(auth -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .defaultSuccessUrl("/main", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl("/main")
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}