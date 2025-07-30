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
	//media get,post,put,delete-post에 종속됨
	//그럼 get,put,post가능/하지만 delete는 어떻게???-> post의 put요청에서 media를 삭제하면 된다. 따라서 media에 DeleteMapping을 할 필요가 없다.
	//comment get,post,put.delete
}
