package com.example.demo.execution.dto.response;

/**
 * API 응답 결과 상태 enum
 */
public enum ApiResponseResult {
	SUCEESS("성공"),
	FAIL("실패");
	
	public final String message;
	
	ApiResponseResult(String message) {
		this.message = message;
	}
	
	public String getId() {
		return this.name();
	}
	
	public String getText() {
		return this.message;
	}
}
