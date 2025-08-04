package com.example.BoardService.dto;

import com.example.BoardService.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Long userId;
    private String username;
    private String password;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime userTime;

    public User toEntity() {
        return new User(this.userId,this.username,this.password,this.userTime);
    }
}
