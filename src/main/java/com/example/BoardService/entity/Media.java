package com.example.BoardService.entity;

import com.example.BoardService.dto.MediaDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @Column(name = "media_type")
    private String mediaType;

    @Lob
    @Column(name = "media_content")
    private byte[] mediaContent;

    @JoinColumn(name = "post_id")
    @ManyToOne
    private Post post;

    public MediaDTO toDTO(){
        return new MediaDTO(this.mediaId,this.mediaType,this.mediaContent);
    }
}
