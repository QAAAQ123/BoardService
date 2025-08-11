package com.example.BoardService.controller;

import com.example.BoardService.dto.PostAndMediaAndCommentDTO;
import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.service.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@org.springframework.stereotype.Controller
@Slf4j
public class Controller {

    @Autowired
    private Service service; // PostService를 주입받기 위해 추가

    @GetMapping("/posts")
    public String showPostsPage(Model model) {
        log.info("게시물 목록 페이지 요청");
        try {
            // Service에서 게시물 목록을 가져와 Model에 추가
            List<PostDTO> posts = service.showPostsService();
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            log.error("게시물 목록을 불러오는 중 오류 발생", e);
            model.addAttribute("posts", List.of()); // 오류 발생 시 빈 리스트를 전달
        }
        return "posts"; // posts.html 파일을 Thymeleaf 뷰로 반환
    }


    //25/08/08-로그인 안되는 문제를 해결
    //문제 원인: 로그인은 Spring security가 username과 password를 가로채서 인증하도록 securityConfig에 설정해야 하는데
    // 로그인을 restController와 service계층을 통해 수동으로 하도록 만들어 놓음.
    // 이 security 자동 로그인과 Restcontroller/Service의 수동 로그인 로직이 충돌하여 로그인 되지 않았던 거였음
    @GetMapping("/login")
    public String loginPage() {
        log.info("로그인 페이지 요청");
        return "login"; // login.html 파일을 Thymeleaf 뷰로 반환
    }

    @GetMapping("/posts/{postId}")
    public String showPostPage(@PathVariable Long postId, Model model) {
        log.info("특정 게시물 상세 페이지 요청 - postId: {}", postId);
        try {
            PostAndMediaAndCommentDTO postDetails = service.showPost(postId);
            model.addAttribute("post", postDetails); // postDetails를 "post"라는 이름으로 전달
        } catch (Exception e) {
            log.error("게시물 상세 정보를 불러오는 중 오류 발생: {}", postId, e);
            model.addAttribute("post", null); // 오류 발생 시 null 전달
        }
        return "post"; // post.html 파일을 Thymeleaf 뷰로 반환
    }

    @GetMapping("/join")
    public String joinPage() {
        log.info("회원가입 페이지 요청");
        return "join"; // join.html 파일을 Thymeleaf 뷰로 반환
    }

    @GetMapping("/posts/create")
    public String createPostPage() {
        log.info("새 게시물 작성 페이지 요청");
        return "create_post"; // create_post.html 파일을 Thymeleaf 뷰로 반환
    }

    @GetMapping("/")
    public String root(){
        return "redirect:/posts";
    }
}
