package com.example.BoardService.service;

import com.example.BoardService.dto.MediaDTO;
import com.example.BoardService.dto.PostAndMediaDTO;
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

    //미디어 결합 완료
    @DisplayName("게시글을 성공적으로 생성한다.")
    @Test
    void createPostSucessfully(){
        //given-1. 파라미터로 들어오는 값:저장할 데이터 DTO(post,mediaList)
        PostDTO postDTO = new PostDTO(null,"제목1","내용1",null);
        MediaDTO mediaDTO1 = new MediaDTO(null,"image/jpeg",sampleJpgBytes);
        MediaDTO mediaDTO2 = new MediaDTO(null,"image/png",samplePngBytes);
        List<MediaDTO> mediaDTOList = Arrays.asList(mediaDTO1,mediaDTO2);

        //2. 리파지터리에서 반환 할 값: input dto를 저장한 엔티티
        Post savedPost  = new Post(1L,"제목1","내용1",now);
        Media savedMeida1 = new Media(1L,"image/jpeg",sampleJpgBytes,savedPost);
        Media savedMedia2 = new Media(2L,"image/png",samplePngBytes,savedPost);

        //3. 최종적으로 반환할 값: dto(post,mediaList)
        PostAndMediaDTO postAndMediasDTO = new PostAndMediaDTO(savedPost.toDTO(),Arrays.asList(savedMeida1.toDTO(),savedMedia2.toDTO()));

        //when-1. postrepository에 저장->savedPost리턴
        when(mockPostRepository.save(any(Post.class))).thenReturn(savedPost);
        //2. mediaReposioty에 저장->savedMediaList 리턴
        when(mockMediaRepository.saveAll(anyList())).thenReturn(Arrays.asList(savedMeida1,savedMedia2));
        //3.주입한 service 계층의 메소드 직접 사용 및 리턴 값 저장
        PostAndMediaDTO resultDTO = mockPostService.createPost(postDTO,mediaDTOList);

        //given-실제와 test 케이스가 맞는지 확인
        assertThat(resultDTO.getPostDTO().getPostId()).isEqualTo(1L);
        assertThat(resultDTO.getPostDTO().getPostTitle()).isEqualTo("제목1");
        assertThat(resultDTO.getPostDTO().getPostContent()).isEqualTo("내용1");
        assertThat(resultDTO.getPostDTO().getPostTime()).isNotNull();
        assertThat(resultDTO.getMediaDTOList().size()).isEqualTo(2);
        assertThat(resultDTO.getMediaDTOList().get(0).getMediaId()).isEqualTo(1L);
        assertThat(resultDTO.getMediaDTOList().get(0).getMediaType()).isEqualTo("image/jpeg");
        assertThat(resultDTO.getMediaDTOList().get(0).getMediaContent()).isNotNull();
        assertThat(resultDTO.getMediaDTOList().get(1)).isNotNull();
        assertThat(resultDTO.getMediaDTOList().get(1).getMediaId()).isEqualTo(2L);
        assertThat(resultDTO.getMediaDTOList().get(1).getMediaType()).isEqualTo("image/png");
        assertThat(resultDTO.getMediaDTOList().get(1).getMediaContent()).isNotNull();


        verify(mockPostRepository).save(any(Post.class));
        verify(mockMediaRepository).saveAll(anyList());
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
    
    //미디어 결합 완료
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
        PostAndMediaDTO postAndMediasDTO = new PostAndMediaDTO(post.toDTO(),mediaDTOList);


        //----------------------------------------------------------------------------------------------------------
        //when-리파지토리 find/return entity real real:service(id)
        when(mockPostRepository.findByIdOrElseThrow(anyLong())).thenReturn(post);
        //when media관련 추가-리파지토리에서 findxxx로 찾고 리턴값은 엔티티이다. 실제: 변하지 않음
        when(mockMediaRepository.findAllByPostPostId(anyLong())).thenReturn(Arrays.asList(jpegImage,pngImage));


        PostAndMediaDTO result = mockPostService.showPost(postId);
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
