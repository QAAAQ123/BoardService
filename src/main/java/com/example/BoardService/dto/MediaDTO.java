package com.example.BoardService.dto;

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
}
