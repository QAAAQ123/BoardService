package com.example.BoardService.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
@Slf4j
public class Controller {

    @GetMapping("/main")
    public String showPostsPage(){
        log.info("main page");
        return "forward:main.html";
    }

    @GetMapping("/login")
    public String loginPage(){
        log.info("login page");
        return "forward:login.html";
    }

    @GetMapping("/main/{postId}")
    public String showPostPage(){
        log.info("post page");
        return "foward:post.html";
    }

    @GetMapping("/join")
    public String joinPage(){
        log.info("join page");
        return "foward:join.html";
    }
}
