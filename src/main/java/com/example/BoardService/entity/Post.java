package com.example.BoardService.entity;

import com.example.BoardService.dto.PostDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "post_title")
    private String postTitle;

    @Lob
    @Column(name = "post_content")
    private String postContent;

    @Column(name = "post_time")
    private LocalDateTime postTime;

    public PostDTO toDTO() {
        return new PostDTO(this.postId,this.postTitle,this.postContent,this.postTime);
    }
}
