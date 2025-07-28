package com.example.BoardService.service;

import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.entity.Post;
import com.example.BoardService.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository mockPostRepository;

    @InjectMocks
    private PostService mockPostService;

    LocalDateTime now = LocalDateTime.now();

    @DisplayName("모든 게시글 목록을 성공적으로 조회한다.")
    @Test
    void showAllPostSucessfully(){
        //준비: postrepository가 반환할 가짜 post 엔티티 목록 설정
        List<Post> list = Arrays.asList(
                new Post(null,"제목1","내용1",now),
                new Post(null ,"제목2","내용2",now)
        );

        List<Post> mockPosts = new ArrayList<>(list);

        when(mockPostRepository.findAll()).thenReturn(mockPosts);//findAll() 호출 시 mockPosts 반환

        //실행: 테스트 대상 메서드 호출
        List<PostDTO> postDTOList = mockPostService.showPosts();

        //검증: 예상되는 결과 확인
        assertThat(postDTOList).isNotNull();
        assertThat(postDTOList).hasSize(2);
        assertThat(postDTOList.get(0).getPostTitle()).isEqualTo("제목1");
        assertThat(postDTOList.get(1).getPostTitle()).isEqualTo("제목2");
        assertThat(postDTOList.get(0).getPostContent()).isEqualTo("내용1");
        assertThat(postDTOList.get(1).getPostContent()).isEqualTo("내용2");
        assertThat(postDTOList.get(0).getPostTime()).isEqualTo(now);
        assertThat(postDTOList.get(1).getPostTime()).isEqualTo(now);

    }

    @DisplayName("게시글을 성공적으로 생성한다.")
    @Test
    void createPostSucessfully(){
        //given-craetePost에 매개변수로 들어갈 DTO와 save()에서 return 예상 값 작성 + when()작성
        //createPost의 매개변수DTO
        PostDTO targetDTO = new PostDTO(null,"제목1","내용1",null);
        //repository에 save된후 return값 예상
        Post savedEntity = new Post(1L,"제목1","내용1",now);
        //when(): save(entity)한다고 가정
        //이 postRepository는 실제 postRepository가 아닌 **Mock 객체**이다.
        when(mockPostRepository.save(any(Post.class))).thenReturn(savedEntity);

        //when-test가 아닌 실제 구현에서 사용할 메소드에 저장
        PostDTO result = mockPostService.createPost(targetDTO);

        //given-실제와 test 케이스가 맞는지 확인
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getPostContent()).isEqualTo("내용1");
        assertThat(result.getPostTitle()).isEqualTo("제목1");

        verify(mockPostRepository,times(1)).save(any(Post.class));
    }

    @DisplayName("게시글을 성공적으로 수정한다.")
    @Test
    void updatePostSucessfully(){
        //given-메소드로 들어올 postId,inputDTO와 existingPost,updatedPost작성
        Long postId = 1L;
        PostDTO inputDTO = new PostDTO(null,"제목수정","내용수정",now);
        Post existingPost = new Post(1L,"제목1","내용1",now.minusHours(1));
        Post updatedPost = new Post(1L,"제목수정","내용수정",now);

        when(mockPostRepository.findByIdOrElseThrow(postId)).thenReturn(existingPost);
        when(mockPostRepository.save(any(Post.class))).thenReturn(updatedPost);


        //when- 실제 service method
        PostDTO result = mockPostService.updatePost(postId,inputDTO);

        //then- 비교
        assertThat(result.getPostTitle()).isEqualTo("제목수정");
        assertThat(result.getPostContent()).isEqualTo("내용수정");
        assertThat(result.getPostTime()).isEqualTo(now);

    }


}
