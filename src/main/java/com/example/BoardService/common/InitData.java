package com.example.BoardService.common;

import com.example.BoardService.entity.Post;
import com.example.BoardService.entity.User;
import com.example.BoardService.repository.PostRepository;
import com.example.BoardService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("초기 데이터 생성 시작...");

        // 더미 사용자 2명 생성
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setUserTime(LocalDateTime.now());
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("password"));
        user2.setUserTime(LocalDateTime.now());
        userRepository.save(user2);

        // 더미 게시물 2개 생성
        Post post1 = new Post();
        post1.setPostTitle("첫 번째 게시물 제목입니다");
        post1.setPostContent("첫 번째 게시물 내용입니다. 아무런 내용을 넣어주세요.");
        post1.setUser(user1);
        post1.setPostTime(LocalDateTime.now());
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setPostTitle("두 번째 게시물 제목입니다");
        post2.setPostContent("두 번째 게시물 내용입니다. 테스트를 위한 내용입니다.");
        post2.setUser(user2);
        post2.setPostTime(LocalDateTime.now());
        postRepository.save(post2);

        log.info("초기 데이터 생성 완료!");
    }
}