package com.example.BoardService.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
@Slf4j
public class Controller {

    @GetMapping("/posts")
    public String showPostsPage(){
        log.info("main page");
        return "forward:posts.html";
    }

    //25/08/08-로그인 안되는 문제를 해결
    //문제 원인: 로그인은 Spring security가 username과 password를 가로채서 인증하도록 securityConfig에 설정해야 하는데
    // 로그인을 restController와 service계층을 통해 수동으로 하도록 만들어 놓음.
    // 이 security 자동 로그인과 Restcontroller/Service의 수동 로그인 로직이 충돌하여 로그인 되지 않았던 거였음
    @GetMapping("/login")
    public String loginPage(){
        log.info("login page");
        return "forward:login.html";
    }

    @GetMapping("/posts/{postId}")
    public String showPostPage(){
        log.info("post page");
        return "forward:post.html";
    }

    @GetMapping("/join")
    public String joinPage(){
        log.info("join page");
        return "forward:join.html";
    }

    @GetMapping("/posts/create")
    public String createPostPage(){
        log.info("create 'Post' page");
        return "forward:create_post.html";
    }


}
