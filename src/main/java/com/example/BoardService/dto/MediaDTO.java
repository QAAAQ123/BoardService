package com.example.BoardService.dto;

import com.example.BoardService.entity.Media;
import com.example.BoardService.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MediaDTO {
    private Long mediaId;
    private String mediaType;
    private byte[] mediaContent;

    public Media toEntity(){
        return new Media(this.mediaId,this.mediaType,this.mediaContent,new Post());
    }
}
