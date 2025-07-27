package com.example.BoardService.service;

import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.entity.Post;
import com.example.BoardService.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<PostDTO> showPosts(){
        //user_id를 제외한 모든 엔티티를 DTO로 변환해야 한다.
        List<Post> postList = postRepository.findAll();
        List<PostDTO> postDTOList = new ArrayList<>();

        for(Post post:postList){
            postDTOList.add(post.toDTO());
        }
        log.info("/posts GET Request:Service logic sucess");
        return postDTOList;
    }

    public PostDTO createPost(PostDTO postDTO){
        //받아온 DTO를 entity로 변환해서 repository에 저장한다.
        Post taregetEntity = postDTO.toEntity();
        taregetEntity.setPostTime(LocalDateTime.now());

        Post savedEntity = postRepository.save(taregetEntity);
        PostDTO savedDTO = savedEntity.toDTO();

        log.info("/posts POST Request:Service logic sucess");
        return savedDTO;
    }
}
