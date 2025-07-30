package com.example.BoardService.service;

import com.example.BoardService.dto.MediaDTO;
import com.example.BoardService.dto.PostAndMediasDTO;
import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.entity.Media;
import com.example.BoardService.entity.Post;
import com.example.BoardService.repository.MediaRepository;
import com.example.BoardService.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository mockPostRepository;

    @Mock
    private MediaRepository mockMediaRepository;

    @InjectMocks
    private PostService mockPostService;

    private byte[] sampleJpgBytes;
    private byte[] samplePngBytes;

    @BeforeEach
    void mediaSetUp(){
        MockitoAnnotations.openMocks(this);
        sampleJpgBytes = new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46};
        samplePngBytes = new byte[]{(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

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
        List<PostDTO> postDTOList = mockPostService.showPostsService();

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


    @DisplayName("게시글을 성공적으로 삭제한다.")
    @Test
    void deletePostSucessfullt(){
        //given-postId
        Long postId = 1L;

        //when-mock,실제
        doNothing().when(mockPostRepository).deleteById(anyLong());
        mockPostService.deletePost(postId);

        //then
        verify(mockPostRepository).deleteById(postId);
    }

    @DisplayName("게시글을 성공적으로 조회한다")
    @Test
    void showPost(){
        //given-받아올 값: id,리턴할 값: post dto,리파지토리 리턴값:postentity
        Long postId = 1L;
        Post post = new Post(1L,"제목1","내용1",now);

        //given-mdeia 추가-받아 올 값: 없음(postId로 해당 mediaId조회 가능),리턴에 추가할 값: mediaDTOList,리파지토리 리턴 값: mediaEntity
        //리퍼지토리 리턴값
        Media jpegImage = new Media(1L, "image/jpeg",sampleJpgBytes,post);
        Media pngImage = new Media(2L,"image/png",samplePngBytes,post);

        //리턴에 추가할 값
        List<MediaDTO> mediaDTOList = Arrays.asList(jpegImage.toDTO(),pngImage.toDTO());

        //showPost()에서 최종 리턴 할 값
        PostAndMediasDTO postAndMediasDTO = new PostAndMediasDTO(post.toDTO(),mediaDTOList);


        //----------------------------------------------------------------------------------------------------------
        //when-리파지토리 find/return entity real real:service(id)
        when(mockPostRepository.findByIdOrElseThrow(anyLong())).thenReturn(post);
        //when media관련 추가-리파지토리에서 findxxx로 찾고 리턴값은 엔티티이다. 실제: 변하지 않음
        when(mockMediaRepository.findAllByPostPostId(anyLong())).thenReturn(Arrays.asList(jpegImage,pngImage));


        PostAndMediasDTO result = mockPostService.showPost(postId);
        //then-비교-post
        assertThat(result.getPostDTO().getPostContent()).isEqualTo("내용1");
        assertThat(result.getPostDTO().getPostTitle()).isEqualTo("제목1");

        //비교-media
        assertThat(result.getMediaDTOList().get(0).getMediaType()).isEqualTo("image/jpeg");
        assertThat(result.getMediaDTOList().get(0).getMediaContent()).isEqualTo(sampleJpgBytes);
        assertThat(result.getMediaDTOList().size()).isEqualTo(2);

        verify(mockPostRepository).findByIdOrElseThrow(postId);
        verify(mockMediaRepository).findAllByPostPostId(postId);

        //red:Cannot invoke "com.example.BoardService.dto.PostDTO.getPostContent()" because "result" is null

    }

}
