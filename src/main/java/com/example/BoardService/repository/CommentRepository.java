package com.example.BoardService.repository;

import com.example.BoardService.entity.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findAllByPostPostId(Long id);
    void deleteAllByPostPostId(Long id);
    default Comment findByIdOrElseThrow(Long id){
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("commentId " + id + "에 해당하는 엔티티가 없습니다."));
    }
}
