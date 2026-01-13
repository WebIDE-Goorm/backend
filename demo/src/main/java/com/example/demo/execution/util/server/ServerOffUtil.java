package com.example.demo.execution.util.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 서버 종료 유틸리티 (현재 미사용)
 */
@Slf4j
@Component
public class ServerOffUtil {
	public void stopTomcatTest() throws Exception {
		log.info("[ServerOffUtil] 서버 종료");
		System.exit(1);
	}
}
