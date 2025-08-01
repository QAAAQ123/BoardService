package com.example.BoardService.dto;

import com.example.BoardService.entity.Comment;
import com.example.BoardService.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor

public class CommentDTO {
    private Long commentId;
    private String commentContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime commentTime;

    public Comment toEntity() {
        return new Comment(this.commentId,this.commentContent,this.commentTime,new Post());
    }
}
