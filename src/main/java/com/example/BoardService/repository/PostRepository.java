package com.example.BoardService.repository;

import com.example.BoardService.entity.Post;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface PostRepository extends JpaRepository<Post, Long> {
    default List<Post> findAllByIdList(Set<Long> idList) {
        return idList.stream()
                .map(this::findByIdOrElseThrow)
                .collect(Collectors.toList());
    }

    default Post findByIdOrElseThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("postId " + id + "에 해당하는 엔티티가 없습니다."));
    }

    @Query("SELECT p.postId FROM Post AS p WHERE p.postTitle LIKE %:keyword% OR p.postContent LIKE %:keyword%")
    List<Long> searchAllPostIdByKeywordAtPost(@Param("keyword") String keyword);
}
