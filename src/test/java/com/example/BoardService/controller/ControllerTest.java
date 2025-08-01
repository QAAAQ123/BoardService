package com.example.BoardService.controller;

import com.example.BoardService.dto.*;
import com.example.BoardService.service.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(Controller.class)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Service mockService;

    @Autowired
    private ObjectMapper objectMapper;

    private byte[] sampleJpgBytes;
    private byte[] samplePngBytes;
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void mediaSetUp() {
        MockitoAnnotations.openMocks(this);
        sampleJpgBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        samplePngBytes = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }


    @DisplayName("GET /posts 요청시 모든 게시글 목록을 성공적으로 반환한다.")
    @Test
    void showAllPostEndpointSucessfully() throws Exception {
        //준비
        List<PostDTO> list = Arrays.asList(
                new PostDTO(1L, "제목1", "내용1", now),
                new PostDTO(2L, "제목2", "내용2", now)
        );
        List<PostDTO> mockDTO = new ArrayList<>(list);
        when(mockService.showPostsService()).thenReturn(mockDTO);

        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("testuser").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // 배열의 길이가 2인지 검증합니다.
                .andExpect(jsonPath("$[0].postId").value(1L)) // 첫 번째 객체의 id가 1L인지 검증합니다.
                .andExpect(jsonPath("$[0].postTitle").value("제목1")) // 첫 번째 객체의 title이 '첫 번째 게시글'인지 검증합니다.
                .andExpect(jsonPath("$[1].postContent").value("내용2")); // 두 번째 객체의 content가 '내용2'인지 검증합니다.
    }

    //media 결합 완료
    @DisplayName("POST /posts 요청시 게시글을 성공적으로 생성한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createPostEndpointSuccessfully() throws Exception {
        //given-1. 받는 값: postDTO,mediaDTOList
        PostDTO inputPostDTO = new PostDTO(null, "제목1", "내용1", null);
        List<MediaDTO> inputMediaDTOList = Arrays.asList(
                new MediaDTO(null, "image/jpeg", sampleJpgBytes),
                new MediaDTO(null, "image/png", samplePngBytes)
        );
        //2 서비스에서 반환할 값: postandmediasDTO
        PostAndMediaDTO postAndMediaDTO = new PostAndMediaDTO(
                new PostDTO(1L, "제목1", "내용1", now),
                Arrays.asList(new MediaDTO(1L, "image/jpeg", sampleJpgBytes),
                        new MediaDTO(2L, "image/png", samplePngBytes))
        );

        //when-service계층 createPost()로 DTO보내고 postAndmeidasDTO 받음
        when(mockService.createPost(any(PostDTO.class), anyList())).thenReturn(postAndMediaDTO);

        Map<String, Object> requestBodyMap = Map.of(
                "postDTO", inputPostDTO,
                "mediaDTOList", inputMediaDTOList
        );
        String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);


        //then-서버와 클라이언트간 request,response확인
        mockMvc.perform(post("/posts")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON) // JSON 데이터 전송임을 명시
                        .content(requestBodyJson) // 생성한 JSON 본문 추가
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    //media 결합 완료
    @DisplayName("PUT /posts/{postId} 요청시 게시글을 성공적으로 수정한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updatePostEndpointSuccessfully() throws Exception {
        /* given-dto로 들어올 값(postandmedia),postId,서비스에서 반환할 값
        when-서비스 updatePost에 들어가고 리턴함
        then-mockmvc(put,posts/{postId},requestBody,csrf,media_type,accecpt->print/isOk()
         */
        PostDTO postDTO = new PostDTO(null, "제목1", "내용1", null);
        MediaDTO mediaDTO2 = new MediaDTO(null, "image/jpeg", sampleJpgBytes);
        MediaDTO mediaDTO3 = new MediaDTO(null, "image/png", samplePngBytes);
        List<MediaDTO> mediaDTOList = Arrays.asList(mediaDTO2, mediaDTO3);

        PostDTO updatedPostDTO = new PostDTO(1L, "수정된 제목", "수정된 내용", now);
        MediaDTO updatedMediaDTO2 = new MediaDTO(2L, "image/jpeg", sampleJpgBytes);
        MediaDTO updatedMediaDTO3 = new MediaDTO(3L, "image/png", samplePngBytes);
        List<MediaDTO> updatedMediaDTOList = Arrays.asList(updatedMediaDTO2, updatedMediaDTO3);

        Long postId = 1L;
        PostAndMediaDTO inputPostAndMediaDTO = new PostAndMediaDTO(postDTO, mediaDTOList);
        PostAndMediaDTO updatedPostAndDTOList = new PostAndMediaDTO(updatedPostDTO, updatedMediaDTOList);

        when(mockService.updatePost(anyLong(), any(PostAndMediaDTO.class))).thenReturn(updatedPostAndDTOList);

        //when+then - mockMvc
        mockMvc.perform(put("/posts/{postId}", postId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputPostAndMediaDTO)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    //media 결합 완료
    //comment 결합 완료
    @DisplayName("DELETE /Post/{postId} 요청시 게시글을 성공적으로 삭제한다")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deletePostEndpointSucessfully() throws Exception {
        //given-id받아옴
        Long postId = 1L;

        //when -리퍼지토리 delete가 void 리턴
        doNothing().when(mockService).deletePost(anyLong());

        //when실제 + then(when의 실제 동작이 클라이언트에서 요청 받아 데이터 보내는것)
        mockMvc.perform(delete("/posts/{postId}", postId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    //미디어 결합 완료
    //comment 결합 완료
    @DisplayName("GET /posts/{postId} 요청시 게시글을 성공적으로 반환한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void showPostEndpointSucessfully() throws Exception {
        //given-postId만 받아옴,when()에서 반환할 값: PostAndMediaDTO
        //post
        Long postId = 1L;
        PostDTO postDTO = new PostDTO(1L, "제목1", "내용1", now);
        //media
        byte[] sampleJpgBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        byte[] samplePngBytes = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MediaDTO mediaDTO1 = new MediaDTO(1L, "image/jpeg", sampleJpgBytes);
        MediaDTO mediaDTO2 = new MediaDTO(2L, "image/png", samplePngBytes);
        List<MediaDTO> mediaDTOList = Arrays.asList(mediaDTO1, mediaDTO2);
        //comment
        List<CommentDTO> existingCommentDTOList = Arrays.asList(
                new CommentDTO(1L, "댓글1", now),
                new CommentDTO(2L, "댓글2", now.plusHours(1)),
                new CommentDTO(3L, "댓글3", now.plusHours(2))
        );

        PostAndMediaAndCommentDTO postAndMediaAndCommentDTO =
                new PostAndMediaAndCommentDTO(postDTO, mediaDTOList, existingCommentDTOList);

        //when-servicr 계층에서 postAndMediasDTO 반환
        when(mockService.showPost(anyLong())).thenReturn(postAndMediaAndCommentDTO);

        //then postId받아와서 postDTO 클라이언트로
        mockMvc.perform(get("/posts/{postId}", postId)
                        .with(csrf()))
                .andDo(print()) // 요청/응답 상세 로그 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.postDTO.postTitle").value("제목1"))
                .andExpect(jsonPath("$.postDTO.postContent").value("내용1"))
                .andExpect(jsonPath("$.mediaDTOList[0].mediaType").value("image/jpeg"))
                .andExpect(jsonPath("$.mediaDTOList[1].mediaType").value("image/png"))
                .andExpect(jsonPath("$.commentDTOList[0].commentContent").value("댓글1"))
                .andExpect(jsonPath("$.commentDTOList[1].commentContent").value("댓글2"))
                .andExpect(jsonPath("$.commentDTOList[2].commentContent").value("댓글3"));

    }

    @DisplayName("Post /posts/{postId}/comments 요청시 댓글을 성공적으로 생성한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createCommentEndpointSuccessfully() throws Exception {
        //given-1.들어올 값: postId,commentDTO/service메소드 파리미터로 들어갈 값: postId,commentDTO/최종적으로 반환할 값: commentDTO
        Long postId = 1L;
        CommentDTO createCommentRequestDTO = new CommentDTO(null,"댓글1",null);
        CommentDTO expectedCommentRequestDTO = new CommentDTO(1L,"댓글1",now);

        //when-service가 무엇을 리턴하는지
        when(mockService.createComment(anyLong(),any(CommentDTO.class))).thenReturn(expectedCommentRequestDTO);

        //then
        mockMvc.perform(post("/posts/{postId}/comments",postId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentRequestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.commentId").value(1L))
                .andExpect(jsonPath("$.commentContent").value("댓글1"))
                .andExpect(jsonPath("$.commentTime").isNotEmpty());
    }

    @DisplayName("PUT /posts/{postId}/comments/{commentId} 요청시 댓글을 성공적으로 수정한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateCommentEndpointSucessfully() throws Exception {
        //given-들어올 값:postId,commentId,commentDTO/service의 메소드로 들어갈 값: commentId,commentDTO/최종적으로 반환할 값: commentDTO
        Long postId = 1L;
        Long commentId = 1L;
        CommentDTO updateCommentRequestDTO = new CommentDTO(null,"댓글수정",now.minusHours(1));

        CommentDTO savedCommentRequestDTO = new CommentDTO(1L,"댓글수정",now);

        //when-service.updatecomment가 리턴할 값
        when(mockService.updateComment(anyLong(),any(CommentDTO.class))).thenReturn(savedCommentRequestDTO);

        //then
        mockMvc.perform(put("/posts/{postId}/comments/{commentId}",postId,commentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCommentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.commentId").value(1L))
                .andExpect(jsonPath("$.commentContent").value("댓글수정"))
                .andExpect(jsonPath("$.commentTime").isNotEmpty());

    }

}
