package com.example.BoardService.dto;

import com.example.BoardService.entity.Media;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostAndMediaAndCommentDTO {
    private PostDTO postDTO;
    private List<MediaDTO> mediaDTOList;
    private List<CommentDTO> commentDTOList;
}
