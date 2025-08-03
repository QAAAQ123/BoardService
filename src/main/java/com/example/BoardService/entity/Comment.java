package com.example.BoardService.entity;

import com.example.BoardService.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "comment_content")
    private String commentContent;

    @Column(name = "comment_time")
    private LocalDateTime commentTime;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public CommentDTO toDTO() {
        return new CommentDTO(this.commentId,this.commentContent,this.commentTime);
    }
}
