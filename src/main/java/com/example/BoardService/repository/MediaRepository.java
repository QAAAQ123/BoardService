package com.example.BoardService.repository;

import com.example.BoardService.entity.Media;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media,Long> {
    List<Media> findAllByPostPostId(Long id);
    void deleteAllByPostPostId(Long id);
}
