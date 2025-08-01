package com.example.BoardService.repository;

import com.example.BoardService.entity.Post;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    default Post findByIdOrElseThrow(Long id){
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("postId " + id + "에 해당하는 엔티티가 없습니다."));
    }
}
