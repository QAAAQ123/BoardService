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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // GET 요청에 대한 접근 허용 설정 (페이지 뷰)
                        .requestMatchers(HttpMethod.GET, "/", "/login", "/join", "/posts", "/posts/create", "/posts/{postId}").permitAll()
                        // 회원가입 API POST 요청 접근 허용
                        .requestMatchers(HttpMethod.POST, "/api/join").permitAll()
                        // 정적 리소스 접근 허용
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // 사용자 정의 로그인 페이지
                        .loginPage("/login")
                        // 로그인 form action URL (login.html과 일치)
                        .loginProcessingUrl("/api/login")
                        // 로그인 성공 시 이동할 기본 URL
                        .defaultSuccessUrl("/posts", true)
                        // 로그인 실패 시 이동할 URL
                        .failureUrl("/login?error=true")
                        // 로그인에 사용할 파라미터 이름
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                )
                // API 통신을 위해 CSRF 보호 비활성화 (실제 서비스에서는 보안 설정 필요)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                com.example.BoardService.entity.User user = userRepository.findByUsername(username);
                if (user == null) {
                    throw new UsernameNotFoundException("유저를 찾을 수 없습니다: " + username);
                }
                return User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build();
            } catch (Exception e) {
                throw new UsernameNotFoundException("사용자 조회 중 오류가 발생했습니다: " + username, e);
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}