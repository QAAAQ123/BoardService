package com.example.BoardService.service;

import com.example.BoardService.dto.PostAndMediasDTO;
import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.entity.Media;
import com.example.BoardService.entity.Post;
import com.example.BoardService.repository.MediaRepository;
import com.example.BoardService.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

    public List<PostDTO> showPostsService(){
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

    public PostDTO updatePost(Long postId,PostDTO inputDTO) {
        //받아온 inputDto에는 postID가 없다.
        //받아온 DTO를 entity로 바꾸고 기존의 데이터와 merge
        //수정한 데이터 DTO로 바꿔서 Return
        
        //1. 기존 데이터 꺼내기,DTO to Entity
        Post targetPostEntity = postRepository.findByIdOrElseThrow(postId);
        Post inputPostEntity = inputDTO.toEntity();
        //2.merge data and save
        Post updatedPostEntity = postRepository.save(mergeEntity(targetPostEntity, inputPostEntity));
        //3. return DTO
        return updatedPostEntity.toDTO();
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    public PostAndMediasDTO showPost(Long postId) {
        //id로 리포지토리 조회해서 엔티티 가져오기->엔티팉 dto로 변환동시에 리턴
        Post post = postRepository.findByIdOrElseThrow(postId);

        //미디어 리포지토리 postId로 조회해서 리스트 만든후에 DTO로 변환
        List<Media> mediaList = mediaRepository.findAllByPostPostId(postId);
        System.out.println(mediaList.get(0));
        System.out.println(mediaList.get(1));

        //DTO 합침
        PostAndMediasDTO postAndMediasDTO = new PostAndMediasDTO(post.toDTO(),
                mediaList.stream()
                        .map(Media::toDTO)
                        .collect(Collectors.toList())
        );

        return postAndMediasDTO;
    }



    private Post mergeEntity(Post targetPostEntity, Post inputPostEntity) {
        boolean hasChanged = false;
        //title,content 새로운 것만 target에 넣기
        if(!inputPostEntity.getPostTitle().isEmpty() || inputPostEntity.getPostContent() != null) {
            targetPostEntity.setPostTitle(inputPostEntity.getPostTitle());
            hasChanged = true;
        }
        if(!inputPostEntity.getPostContent().isEmpty() || inputPostEntity.getPostContent() != null) {
            targetPostEntity.setPostContent(inputPostEntity.getPostContent());
            hasChanged = true;
        }

        if(hasChanged)
            targetPostEntity.setPostTime(LocalDateTime.now());

        return targetPostEntity;
    }
}
