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
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,  "/login", "/join", "/main","/*.html","/api/posts").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/login","/api/join","/joinProc").permitAll()
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                // 인증되지 않은 사용자가 보호된 리소스에 접근 시 /main으로 리디렉션
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.sendRedirect("/main");
                                })
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
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}