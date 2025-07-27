package com.example.BoardService.controller;

import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.entity.Post;
import com.example.BoardService.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> showPosts(){
        log.info("/posts GET Request:show all Posts");
        List<PostDTO> postDTOList = postService.showPosts();

        log.info("/posts GET Request:Controller logic sucess");
        return ResponseEntity.status(HttpStatus.OK).body(postDTOList);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO){
        log.info("/posts POST Request:Create Post");
        PostDTO createdDTO = postService.createPost(postDTO);

        log.info("/posts POST Request:Controller logic sucess");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDTO);
    }
}
