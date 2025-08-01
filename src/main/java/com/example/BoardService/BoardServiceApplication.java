package com.example.BoardService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BoardServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardServiceApplication.class, args);
	}

	//posts get완료
	//post get,post,put,delete완료
	//media get,post,put,delete-post에 종속됨-완료
	//comment get,post,put.delete
}
