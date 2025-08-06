package com.example.BoardService.service;

import com.example.BoardService.dto.*;
import com.example.BoardService.entity.*;
import com.example.BoardService.repository.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {
    @Mock
    private PostRepository mockPostRepository;

    @Mock
    private MediaRepository mockMediaRepository;

    @Mock
    private CommentRepository mockCommentRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private Service mockService;

    private byte[] sampleJpgBytes;
    private byte[] samplePngBytes;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        sampleJpgBytes = new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46};
        samplePngBytes = new byte[]{(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        //sercurity 부분 AI 작성한 코드 Ctrl+V
        Authentication auth = new UsernamePasswordAuthenticationToken("유저1",null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    final LocalDateTime now = LocalDateTime.now();

    @DisplayName("모든 게시글 목록을 성공적으로 조회한다.")
    @Test
    //25/08/06-User 추가
    void showAllPostSucessfully(){
        //준비: postrepository가 반환할 가짜 post 엔티티 목록 설정
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));
        List<Post> list = Arrays.asList(
                new Post(null,"제목1","내용1",now,user),
                new Post(null ,"제목2","내용2",now,user)
        );

        List<Post> mockPosts = new ArrayList<>(list);

        when(mockPostRepository.findAll()).thenReturn(mockPosts);//findAll() 호출 시 mockPosts 반환

        //실행: 테스트 대상 메서드 호출
        List<PostDTO> postDTOList = mockService.showPostsService();

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
    //25/08/05-Post에 user 연결하는 로직 테스트 완료
    @DisplayName("게시글을 성공적으로 생성한다.")
    @Test
    void createPostSucessfully(){
        //given-1. 파라미터로 들어오는 값:저장할 데이터 DTO(post,mediaList)
        PostDTO postDTO = new PostDTO(null,"제목1","내용1",null);
        MediaDTO mediaDTO1 = new MediaDTO(null,"image/jpeg",sampleJpgBytes);
        MediaDTO mediaDTO2 = new MediaDTO(null,"image/png",samplePngBytes);
        List<MediaDTO> mediaDTOList = Arrays.asList(mediaDTO1,mediaDTO2);

        //2. 이미 저장되어있던 User엔티티
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));

        //3. 리파지터리에서 반환 할 값: input dto를 저장한 엔티티
        Post savedPost  = new Post(1L,"제목1","내용1",now,user);
        Media savedMeida1 = new Media(1L,"image/jpeg",sampleJpgBytes,savedPost);
        Media savedMedia2 = new Media(2L,"image/png",samplePngBytes,savedPost);

        //4. 최종적으로 반환할 값: dto(post,mediaList)
        PostAndMediaDTO postAndMediasDTO = new PostAndMediaDTO(savedPost.toDTO(),Arrays.asList(savedMeida1.toDTO(),savedMedia2.toDTO()));

        //when-1. postrepository에 저장->savedPost리턴
        when(mockPostRepository.save(any(Post.class))).thenReturn(savedPost);
        //2. mediaReposioty에 저장->savedMediaList 리턴
        when(mockMediaRepository.saveAll(anyList())).thenReturn(Arrays.asList(savedMeida1,savedMedia2));
        //3. 원래 저장되어있던 유저 정보
        when(mockUserRepository.findByUsername(anyString())).thenReturn(user);

        //act-주입한 service 계층의 메소드 직접 사용 및 리턴 값 저장
        PostAndMediaDTO resultDTO = mockService.createPost(postDTO,mediaDTOList);

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

    //미디어 결합 완료
    @DisplayName("게시글을 성공적으로 수정한다.")
    @Test
    void updatePostSucessfully() throws AccessDeniedException {
        //given-리퍼지토리 기본 데이터 설정
        // 1. 기존 데이터 (DB에 이미 저장되어 있다고 가정)
        Long postId = 1L;
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));
        Post existingPost = new Post(1L, "원래 제목", "원래 내용", LocalDateTime.now(),user);
        Post savedPost = new Post(1L, "수정된 제목", "수정된 내용", LocalDateTime.now(),user);
        Media existingMedia1 = new Media(1L, "image/jpeg", sampleJpgBytes, existingPost);
        Media existingMedia2 = new Media(2L, "image/png", samplePngBytes, existingPost);
        List<Media> existingMediaList = Arrays.asList(existingMedia1, existingMedia2);
        List<Media> savedMediaList = Arrays.asList(
                new Media(2L, "image/jpeg", sampleJpgBytes,savedPost),
                new Media(3L, "image/gif",samplePngBytes,savedPost)
        );

        Long deleteList[] = new Long[]{1L};

        //2. findById가 호출되면 기존 post,meida 반환하도록 설정
        when(mockPostRepository.findByIdOrElseThrow(postId)).thenReturn(existingPost);
        when(mockMediaRepository.findAllByPostPostId(postId)).thenReturn(existingMediaList);
        when(mockPostRepository.save(any(Post.class))).thenReturn(existingPost);
        when(mockMediaRepository.saveAll(anyList())).thenReturn(savedMediaList);

        //when-수정을 위한 데이터
        PostDTO inputPostDTO = new PostDTO(null, "수정된 제목", "수정된 내용", null);
        MediaDTO inputMediaDTO2 = new MediaDTO(2L, "image/jpeg", sampleJpgBytes);
        MediaDTO inputMediaDTO3 = new MediaDTO(null, "image/gif",samplePngBytes);
        List<MediaDTO> inputMediaDTOList = Arrays.asList(inputMediaDTO2, inputMediaDTO3);
        PostAndMediaDTO inputPostAndDTOList = new PostAndMediaDTO(inputPostDTO,inputMediaDTOList);
        // 실제 서비스 메서드 호출
        PostAndMediaDTO updatedResult = mockService.updatePost(postId, inputPostAndDTOList);

        // then: 결과 검증
        assertThat(updatedResult.getPostDTO().getPostTitle()).isEqualTo("수정된 제목");
        assertThat(updatedResult.getMediaDTOList().size()).isEqualTo(2);
        assertThat(updatedResult.getMediaDTOList()).extracting(MediaDTO::getMediaId)
                .containsExactlyInAnyOrder(2L, 3L);

        // verify: Mock 객체의 메서드 호출 확인
        verify(mockMediaRepository, times(1)).deleteAllById(List.of(deleteList));
    }

    //미디어 결합 완료
    //comment 결합 완료
    @DisplayName("게시글을 성공적으로 삭제한다.")
    @Test
    void deletePostSucessfully(){
        //given-postId
        Long postId = 1L;

        //when-mock,실제
        doNothing().when(mockPostRepository).deleteById(anyLong());
        doNothing().when(mockMediaRepository).deleteAllByPostPostId(anyLong());
        doNothing().when(mockCommentRepository).deleteAllByPostPostId(anyLong());
        mockService.deletePost(postId);

        //then
        verify(mockPostRepository).deleteById(postId);
        verify(mockMediaRepository).deleteAllByPostPostId(postId);
        verify(mockCommentRepository).deleteAllByPostPostId(postId);
    }
    
    //미디어 결합 완료
    //comment 결합 완료
    @DisplayName("게시글을 성공적으로 조회한다")
    @Test
    void showPost(){
        //given-받아올 값: userId,리턴할 값: post dto,리파지토리 리턴값:postentity
        Long postId = 1L;
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));
        Post post = new Post(1L,"제목1","내용1",now,user);

        //given-mdeia 추가-받아 올 값: 없음(postId로 해당 mediaId조회 가능),리턴에 추가할 값: mediaDTOList,리파지토리 리턴 값: mediaEntity
        //리퍼지토리 리턴값
        Media jpegImage = new Media(1L, "image/jpeg",sampleJpgBytes,post);
        Media pngImage = new Media(2L,"image/png",samplePngBytes,post);

        //리턴에 추가할 값
        List<MediaDTO> mediaDTOList = Arrays.asList(jpegImage.toDTO(),pngImage.toDTO());

        //showPost()에서 최종 리턴 할 값
        PostAndMediaDTO postAndMediasDTO = new PostAndMediaDTO(post.toDTO(),mediaDTOList);

        //comment 추가
        //given-받아올 값:없음,리파지토리 리턴 값: commentEntityList,service.showPost리턴값: PostAndMediaAndCommentDTO
        User user1 = new User(1L,"유저1","encodedPassword",now);
        User user2 = new User(2L,"유저2","encodedPassword",now);
        List<Comment> existingCommentEntityList = Arrays.asList(
                new Comment(1L,"댓글1",now,post,user1),
                new Comment(2L,"댓글2",now.plusHours(1),post,user1),
                new Comment(3L,"댓글3",now.plusHours(2),post,user2)
        );

        List<CommentDTO> existingCommentDTOList = existingCommentEntityList.stream()
                .map(Comment::toDTO).toList();
        PostAndMediaAndCommentDTO postAndMediaAndCommentDTO = new PostAndMediaAndCommentDTO(post.toDTO(),mediaDTOList,existingCommentDTOList);

        //----------------------------------------------------------------------------------------------------------
        //when-리파지토리 find/return entity real real:service(userId)
        when(mockPostRepository.findByIdOrElseThrow(anyLong())).thenReturn(post);
        //when media관련 추가-리파지토리에서 findxxx로 찾고 리턴값은 엔티티이다. 실제: 변하지 않음
        when(mockMediaRepository.findAllByPostPostId(anyLong())).thenReturn(Arrays.asList(jpegImage,pngImage));
        //when-테스트할 메소드: repositoty.findAllByPostPostId->commentEntityList/실제 service.showPost()->PostAndMediaAndCommentDTO
        when(mockCommentRepository.findAllByPostPostId(postId)).thenReturn(existingCommentEntityList);
        PostAndMediaAndCommentDTO result = mockService.showPost(postId);

        //then-비교-post
        assertThat(result.getPostDTO().getPostContent()).isEqualTo("내용1");
        assertThat(result.getPostDTO().getPostTitle()).isEqualTo("제목1");

        //비교-media
        assertThat(result.getMediaDTOList().get(0).getMediaType()).isEqualTo("image/jpeg");
        assertThat(result.getMediaDTOList().get(0).getMediaContent()).isEqualTo(sampleJpgBytes);
        assertThat(result.getMediaDTOList().size()).isEqualTo(2);

        verify(mockPostRepository).findByIdOrElseThrow(postId);
        verify(mockMediaRepository).findAllByPostPostId(postId);

        //then-확인할 값:comment userId,contnet,commentTime/findAllByPostPostId한번만 실행되는거 확인

    }

    //댓글 생성
    //25/08/05-유저 정보 받아와서 저장하는 로직 추가
    @DisplayName("댓글을 성공적으로 생성한다.")
    @Test
    void createCommentSucessfully(){
        //given-1.받아올값: postId,CommentDTO/when(repository)의 반환값:commentEntity/최종 리턴 값: commentDTO/user 정보security에서 받아옴
        Long postId = 1L;
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));
        Post post = new Post(1L,"제목1","내용1",now.minusHours(1),user);
        CommentDTO inputCommentDTO = new CommentDTO(null,"댓글1",null);
        Comment createdCommentEntity = new Comment(1L,"댓글1",now,post,user);
        CommentDTO createdCommentDTO = new CommentDTO(1L,"댓글1",now);

        //when-repository에 넣고 entity반환
        when(mockUserRepository.findByUsername(anyString())).thenReturn(user);
        when(mockPostRepository.findByIdOrElseThrow(anyLong())).thenReturn(post);
        when(mockCommentRepository.save(any(Comment.class))).thenReturn(createdCommentEntity);

        //act-service.createCommnet에 넣은 결과
        CommentDTO result = mockService.createComment(postId,inputCommentDTO);

        //then-assertThat:댓글id,내용,시간 비교/verify:save가 한번 사용되었는지,postRe에서 post가 한번 꺼내 졌는지 확인
        assertThat(result).isNotNull();
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getCommentContent()).isEqualTo("댓글1");
        assertThat(result.getCommentTime()).isEqualTo(now);

        verify(mockCommentRepository).save(any(Comment.class));
        verify(mockPostRepository).findByIdOrElseThrow(anyLong());
        verify(mockUserRepository).findByUsername(anyString());
    }


    @DisplayName("댓글을 성공적으로 수정한다.")
    @Test
    void updateCommentSucessfully() throws AccessDeniedException{
        //before-수정전 값: commentEntity
        User user = new User(1L, "유저1", "encodedPassword", now.minusHours(1));
        Comment existingCommnetEntity = new Comment(1L,"댓글",now.minusHours(1),new Post(),user);
        when(mockCommentRepository.findByIdOrElseThrow(anyLong())).thenReturn(existingCommnetEntity);

        //given-받아올 값: commentdto,commnetId/원래 저장되어있던 값:commentEntity/when(repository)의 반환값: commententity/최종적으로 반환할 값: commentdto
        Long commentId = 1L;
        CommentDTO updateCommentRequestDTO = new CommentDTO(null,"댓글수정",now);
        Comment expectedCommentEntity = new Comment(1L,"댓글수정",now,new Post(),user);
        CommentDTO expectedCommentDTO = new CommentDTO(1L,"댓글수정",now);

        //when-repository에 저장후 반환값:comment
        when(mockCommentRepository.save(any(Comment.class))).thenReturn(expectedCommentEntity);
        
        //act-실제 서비스 
        CommentDTO result = mockService.updateComment(commentId,updateCommentRequestDTO);

        //then-assertThat:댓글id,내용,시간 비교/verify:save가 한번 사용되었는지,commentRe에서  findbyid가 1번 사용되었는지
        assertThat(result).isNotNull();
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getCommentContent()).isEqualTo("댓글수정");
        assertThat(result.getCommentTime()).isNotNull();

        verify(mockCommentRepository,times(1)).findByIdOrElseThrow(anyLong());
        verify(mockCommentRepository,times(1)).save(any(Comment.class));
    }

    //25/08/04-password encoder 테스트 추가
    @DisplayName("유저정보를 성공적으로 저장한다.")
    @Test
    void saveUserSucessfully(){
        //given-받아올 값: userDTO/repository의 반환 값: userEntity/서비스 메소드의 최종 반환 값: void
        UserDTO saveUserRequestDTO = new UserDTO(null,"userName","userPassword",null);
        User savedUserEntity = new User(1L,"userName","userPassword",now);

        //when-repository에 저장할 값과 반환값
        when(mockUserRepository.save(any(User.class))).thenReturn(savedUserEntity);
        when(encoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");

        //act-service는 void를 반환
        mockService.joinUser(saveUserRequestDTO);

        verify(mockUserRepository,times(1)).save(any(User.class));
        verify(encoder,times(1)).encode(any(CharSequence.class));
    }

    //25/08/04-password encoder 테스트 추가
    @DisplayName("유저 로그인을 위한 유저 정보 확인을 성공적으로 수행한다.")
    @Test
    void loginUserSucessfully(){
        // given
        String rawPassword = "userPassword";
        String encodedPassword = "encodedPassword";

        UserDTO loginUserRequestDTO = new UserDTO(null, "userName", rawPassword, null);
        User existingUserEntity = new User(1L, "userName", encodedPassword, now.minusHours(1));

        // when
        // userRepository.findByUsername 호출 시 existingUserEntity 반환
        when(mockUserRepository.findByUsername(anyString())).thenReturn(existingUserEntity);

        // bCryptPasswordEncoder.matches 호출 시 true 반환 (성공 가정)
        when(encoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // act
        Boolean result = mockService.loginUser(loginUserRequestDTO);

        // assert
        assertThat(result).isTrue();
        verify(mockUserRepository, times(1)).findByUsername(anyString());
        verify(encoder, times(1)).matches(rawPassword, encodedPassword);
    }
}
