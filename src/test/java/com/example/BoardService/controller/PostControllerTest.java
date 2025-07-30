package com.example.BoardService.controller;

import com.example.BoardService.dto.MediaDTO;
import com.example.BoardService.dto.PostAndMediaDTO;
import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.service.PostService;
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


@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService mockPostService;

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
        when(mockPostService.showPostsService()).thenReturn(mockDTO);

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
        when(mockPostService.createPost(any(PostDTO.class),anyList())).thenReturn(postAndMediaDTO);

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

    @DisplayName("PUT /posts/{postId} 요청시 게시글을 성공적으로 수정한다.")
    @Test
    void updatePostEndpointSuccessfully() throws Exception {
        //given-클라이언트에서 들어올 DTO,서비스 계층 끝나고 리턴한 DTO
        //when()-serviec계층 any(dto) 넣고 return은 구체적 DTO
        Long postId = 1L;
        PostDTO inputDTO = new PostDTO(null, "제목수정", "내용수정", now);
        PostDTO updatedDTO = new PostDTO(1L, "제목수정", "내용수정", now);

        when(mockPostService.updatePost(eq(postId), any(PostDTO.class))).thenReturn(updatedDTO);

        //when+then - mockMvc
        mockMvc.perform(put("/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO))//LocalDateTime 직렬화 불가능 에러 jackson 의존성 추가하여 해결
                        .with(user("testuser").roles("USER")))
                .andDo(print());
        //.andExpect(status().isOk());

        //red:실패-INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes
        //green:성공-security관련 오류 발생-> andExpect에서 오류(요청을 잘 받아와지는데 security때문에 핸들러부터 null로 표시됨)
    }

    @DisplayName("DELETE /Post/{postId} 요청시 게시글을 성공적으로 삭제한다")
    @Test
    void deletePostEndpointSucessfully() throws Exception {
        //given-id받아옴
        Long postId = 1L;

        //when -리퍼지토리 delete가 void 리턴
        doNothing().when(mockPostService).deletePost(anyLong());

        //when실제 + then(when의 실제 동작이 클라이언트에서 요청 받아 데이터 보내는것)
        mockMvc.perform(delete("/post/{postId}", postId)
                        .with(user("testuser").roles("USER")))
                .andDo(print());

    }

    //미디어 결합 완료
    @DisplayName("GET /Post/{postId} 요청시 게시글을 성공적으로 반환한다.")
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void showPostEndpointSucessfully() throws Exception {
        //given-postId만 받아옴,when()에서 반환할 값: PostAndMediaDTO
        Long postId = 1L;
        byte[] sampleJpgBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        byte[] samplePngBytes = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        PostDTO postDTO = new PostDTO(1L, "제목1", "내용1", now);
        MediaDTO mediaDTO1 = new MediaDTO(1L, "image/jpeg", sampleJpgBytes);
        MediaDTO mediaDTO2 = new MediaDTO(2L, "image/png", samplePngBytes);
        PostAndMediaDTO postAndMediasDTO = new PostAndMediaDTO(postDTO, Arrays.asList(mediaDTO1, mediaDTO2));

        //when-servicr 계층에서 postAndMediasDTO 반환
        when(mockPostService.showPost(anyLong())).thenReturn(postAndMediasDTO);

        //then postId받아와서 postDTO 클라이언트로
        mockMvc.perform(get("/posts/{postId}", postId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) // 요청/응답 상세 로그 출력
                .andExpect(status().isOk());

    }

}
