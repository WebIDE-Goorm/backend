package com.example.demo.execution.controller.compile;


import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket 기반 실행을 사용하며 REST /compile은 제공하지 않습니다.
 */
@CrossOrigin
@RestController
public class CompileController {
	/**
	 * 서버 종료 API (테스트용)
	 */
	@PostMapping(value="stop")
	@Hidden
	public void stopTomcatTest() throws Exception {
		System.exit(1);
	}
}
