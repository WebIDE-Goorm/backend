package com.example.demo.common;

import org.springframework.http.HttpStatus;

public enum ErrorMessage {
    // 404 Not Found
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PROJECT_NOT_FOUND("프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PROJECT_MEMBER_NOT_FOUND("프로젝트 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PARENT_FOLDER_NOT_FOUND("부모 폴더를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_CONTENT_NOT_FOUND("파일 내용을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_INVITE_CODE("유효하지 않은 초대 코드입니다.", HttpStatus.NOT_FOUND),

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
    PARENT_FOLDER_DIFFERENT_PROJECT("부모 폴더가 다른 프로젝트에 속해 있습니다.", HttpStatus.BAD_REQUEST),
    PARENT_NOT_FOLDER("부모 유형이 폴더여야 합니다.", HttpStatus.BAD_REQUEST),
    CANNOT_SAVE_TO_FOLDER("폴더에는 내용을 저장할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_READ_FOLDER_CONTENT("폴더는 내용을 조회할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    TERMINAL_ACCESS_DENIED("터미널 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    PROJECT_MEMBER_NOT_AUTHORIZED("프로젝트 멤버가 아닙니다.", HttpStatus.FORBIDDEN),

    // 409 Conflict
    DUPLICATE_USER("이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    PROJECT_MEMBER_ALREADY_EXISTS("이미 프로젝트 멤버입니다.", HttpStatus.CONFLICT),
    FILE_ALREADY_EXISTS("같은 이름의 파일이 이미 존재합니다.", HttpStatus.CONFLICT);

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