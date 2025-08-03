package com.example.BoardService.controller;

import com.example.BoardService.dto.*;
import com.example.BoardService.service.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@org.springframework.web.bind.annotation.RestController
@Slf4j
@RequestMapping("/api")
public class RestController {

    @Autowired
    private Service service;

    //전체 글 목록 조회
    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> showPosts(){
        log.info("/posts GET Request:show all Posts");
        List<PostDTO> postDTOList = service.showPostsService();

        log.info("/posts GET Request:Controller logic sucess");
        return ResponseEntity.status(HttpStatus.OK).body(postDTOList);
    }

    //글 생성
    //media 결합 완료
    @PostMapping("/posts")
    public ResponseEntity<PostAndMediaDTO> createPost(@RequestBody PostAndMediaDTO inputPostAndMediaListDTO){
        log.info("/posts POST Request:Create Post");
        PostDTO inputPostDTO = inputPostAndMediaListDTO.getPostDTO();
        List<MediaDTO> inputMediaListDTO = inputPostAndMediaListDTO.getMediaDTOList();
        PostAndMediaDTO createdPostAndMediaDTO = service.createPost(inputPostDTO,inputMediaListDTO);

        log.info("/posts POST Request:Controller logic sucess");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPostAndMediaDTO);
    }

    //글 수정
    //media 결합 완료
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostAndMediaDTO> updatePost(@PathVariable Long postId,@RequestBody PostAndMediaDTO inputPostAndMediaDTO){
        //받은 DTO와 postId service로 넘겨줌
        log.info("/post/{} PUT Request:Update Post",postId);
        PostAndMediaDTO updatedDTO = service.updatePost(postId,inputPostAndMediaDTO);

        log.info("/posts/{} PUT Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDTO);
    }

    //글 삭제
    //media 결합 완료
    //comment 결합 완료
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId){
        log.info("/post/{} DELETE Request:Delete Post",postId);
        service.deletePost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //글 조회
    //media 결합 완료
    //comment 결합 완료
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostAndMediaAndCommentDTO> showPost(@PathVariable Long postId){
        //받은 Id 서비스로 넘겨줌
        log.info("/post/{} GET Reqeust:Show Post",postId);
        PostAndMediaAndCommentDTO postAndMediaAndCommentDTO = service.showPost(postId);

        log.info("/post/{} GET Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(postAndMediaAndCommentDTO);
    }

    //댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(@PathVariable Long postId,@RequestBody CommentDTO createCommentRequestDTO){
        log.info("/post/{}/comments POST Reqeust:Create comment",postId);
        CommentDTO savedCommentRequestDTO = service.createComment(postId,createCommentRequestDTO);

        log.info("/post/{}/comments POST Reqeust:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCommentRequestDTO);
    }

    //댓글 수정
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId,@RequestBody CommentDTO updateCommentRequestDTO){
        log.info("/post/?/comments/{} PUT Reqeust:Update comment",commentId);
        CommentDTO savedCommentRequestDTO = service.updateComment(commentId,updateCommentRequestDTO);

        log.info("/post/?/comments/{} PUT Reqeust:Controller logic sucess",commentId);
        return ResponseEntity.status(HttpStatus.OK).body(savedCommentRequestDTO);
    }
}
