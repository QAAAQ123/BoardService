package com.example.BoardService.controller;

import com.example.BoardService.dto.PostDTO;
import com.example.BoardService.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    LocalDateTime now = LocalDateTime.now();

    @DisplayName("GET /posts 요청시 모든 게시글 목록을 성공적으로 반환해야 한다.")
    @Test
    void showAllPostEndpointSuccess() throws Exception{
        //준비
        List<PostDTO> list = Arrays.asList(
                new PostDTO(1L,"제목1","내용1",now),
                new PostDTO(2L,"제목2","내용2",now)
        );
        List<PostDTO> mockDTO = new ArrayList<>(list);
        when(postService.showPosts()).thenReturn(mockDTO);

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
}
