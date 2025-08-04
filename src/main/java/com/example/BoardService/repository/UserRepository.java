package com.example.BoardService.repository;

import com.example.BoardService.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String userName);
}
