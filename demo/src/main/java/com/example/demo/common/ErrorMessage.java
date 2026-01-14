package com.example.demo.common;

import org.springframework.http.HttpStatus;

public enum ErrorMessage {
    // 404 Not Found
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 400 Bad Request
    FILE_READ_ERROR("파일을 읽을 수 없습니다.", HttpStatus.BAD_REQUEST),
    FILE_WRITE_ERROR("파일을 저장할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_PATH("올바르지 않은 파일 경로입니다.", HttpStatus.BAD_REQUEST),
    CODE_SAVE_ERROR("코드 저장에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CODE_FORMAT_ERROR("코드 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    TERMINAL_COMMAND_ERROR("명령어 실행에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CHAT_SEND_ERROR("메시지 전송에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CHAT_LOAD_ERROR("채팅 내역을 불러올 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD("필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    TERMINAL_ACCESS_DENIED("터미널 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 409 Conflict
    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorMessage(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatusCode() {
        return httpStatus.value();
    }
}