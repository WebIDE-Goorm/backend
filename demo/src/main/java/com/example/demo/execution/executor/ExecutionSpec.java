package com.example.demo.execution.executor;

/**
 * 언어별 컨테이너 실행 스펙.
 */
public class ExecutionSpec {
  // 저장할 소스 파일명
  private final String fileName;
  // 사용할 Docker 이미지
  private final String image;
  // 컨테이너 내부 실행 커맨드
  private final String command;

  public ExecutionSpec(String fileName, String image, String command) {
    this.fileName = fileName;
    this.image = image;
    this.command = command;
  }

  public String getFileName() {
    return fileName;
  }

  public String getImage() {
    return image;
  }

  public String getCommand() {
    return command;
  }
}
