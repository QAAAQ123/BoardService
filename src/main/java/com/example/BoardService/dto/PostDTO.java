package com.example.BoardService.dto;

import com.example.BoardService.entity.Post;
import com.example.BoardService.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostDTO {
    private Long postId;
    private String postTitle;
    private String postContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime postTime;

    public Post toEntity() {
        return new Post(this.postId,this.postTitle,this.postContent,this.postTime,new User());
    }
}
