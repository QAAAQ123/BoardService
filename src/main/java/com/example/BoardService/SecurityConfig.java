package com.example.BoardService;

import com.example.BoardService.repository.UserRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,  "/login", "/join", "/posts","/*.html","/api/posts").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/login","/api/join","/joinProc").permitAll()
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                // 인증되지 않은 사용자가 보호된 리소스에 접근 시 /main으로 리디렉션
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.sendRedirect("/posts");
                                })
                )
                .formLogin(auth -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/api/login")
                        .defaultSuccessUrl("/posts", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(auth -> auth
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl("/posts")
                        .invalidateHttpSession(true)
                )
                .csrf(auth->auth.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.BoardService.entity.User user = userRepository.findByUsername(username);
            if(user == null){
                throw new UsernameNotFoundException("유저를 찾을 수 없습니다. 유저이름: " + username);
            }
            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}