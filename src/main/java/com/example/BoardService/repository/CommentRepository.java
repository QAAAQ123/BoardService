package com.example.BoardService.repository;

import com.example.BoardService.entity.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findAllByPostPostId(Long id);
    void deleteAllByPostPostId(Long id);

    default Comment findByIdOrElseThrow(Long id){
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("commentId " + id + "에 해당하는 엔티티가 없습니다."));
    }

    //25/08/06-c.postId를 c.post.postId로 수정하여 UnsatisfiedDependencyException 해결
    @Query("SELECT c.post.postId FROM Comment AS c WHERE c.commentContent LIKE %:keyword%")
    List<Long> searchAllPostIdByKeywordAtComment(@Param("keyword") String keyword);
}
