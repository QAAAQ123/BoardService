package com.example.BoardService.controller;

import com.example.BoardService.dto.PostAndMediasDTO;
import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> showPosts(){
        log.info("/posts GET Request:show all Posts");
        List<PostDTO> postDTOList = postService.showPostsService();

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

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long postId,@RequestBody PostDTO inputDTO){
        //받은 DTO와 postId service로 넘겨줌
        log.info("/post/{} PUT Request:Update Post",postId);
        PostDTO updatedDTO = postService.updatePost(postId,inputDTO);

        log.info("/posts/{} PUT Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDTO);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId){
        log.info("/post/{} DELETE Request:Delete Post",postId);
        postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostAndMediasDTO> showPost(@PathVariable Long postId){
        //받은 Id 서비스로 넘겨줌
        log.info("/post/{} GET Reqeust:Show Post",postId);
        PostAndMediasDTO postAndMediasDTO = postService.showPost(postId);

        log.info("/post/{} GET Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(postAndMediasDTO);
    }
}
