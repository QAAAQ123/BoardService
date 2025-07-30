package com.example.BoardService.repository;

import com.example.BoardService.entity.Media;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media,Long> {
    default Media findByIdOrElseThrow(Long id){
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException("mediaId " + id + "에 해당하는 엔티티가 없습니다."));
    }

    List<Media> findAllByPostPostId(Long id);
}
