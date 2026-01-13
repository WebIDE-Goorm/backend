package com.example.demo.execution.util.common;

import java.util.UUID;

/**
 * UUID 생성 유틸리티
 * 
 * 각 컴파일 요청마다 고유한 임시 디렉토리를 생성하기 위해 사용
 */
public class UUIDUtil {
	/**
	 * 하이픈(-)이 제거된 UUID 문자열 생성
	 * 
	 * @return 32자리 UUID 문자열
	 */
	public static String createUUID() {
		String uuid = UUID.randomUUID().toString().replace("-","");
		return uuid;
	}
}
