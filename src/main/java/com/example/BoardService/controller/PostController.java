package com.example.BoardService.controller;

import com.example.BoardService.dto.MediaDTO;
import com.example.BoardService.dto.PostAndMediaDTO;
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

    //media 결합 완료
    @PostMapping("/posts")
    public ResponseEntity<PostAndMediaDTO> createPost(@RequestBody PostAndMediaDTO inputPostAndMediaListDTO){
        log.info("/posts POST Request:Create Post");
        PostDTO inputPostDTO = inputPostAndMediaListDTO.getPostDTO();
        List<MediaDTO> inputMediaListDTO = inputPostAndMediaListDTO.getMediaDTOList();
        PostAndMediaDTO createdPostAndMediaDTO = postService.createPost(inputPostDTO,inputMediaListDTO);

        log.info("/posts POST Request:Controller logic sucess");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPostAndMediaDTO);
    }

    //media 결합 완료
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostAndMediaDTO> updatePost(@PathVariable Long postId,@RequestBody PostAndMediaDTO inputPostAndMediaDTO){
        //받은 DTO와 postId service로 넘겨줌
        log.info("/post/{} PUT Request:Update Post",postId);
        PostAndMediaDTO updatedDTO = postService.updatePost(postId,inputPostAndMediaDTO);

        log.info("/posts/{} PUT Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDTO);
    }

    //media 결합 완료
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId){
        log.info("/post/{} DELETE Request:Delete Post",postId);
        postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //media 결합 완료
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostAndMediaDTO> showPost(@PathVariable Long postId){
        //받은 Id 서비스로 넘겨줌
        log.info("/post/{} GET Reqeust:Show Post",postId);
        PostAndMediaDTO postAndMediasDTO = postService.showPost(postId);

        log.info("/post/{} GET Request:Controller logic sucess",postId);
        return ResponseEntity.status(HttpStatus.OK).body(postAndMediasDTO);
    }
}
