package com.example.BoardService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class PostAndMediasDTO {
    private PostDTO postDTO;
    private List<MediaDTO> mediaDTOList;
}
