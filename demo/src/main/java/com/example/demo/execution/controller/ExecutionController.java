package com.example.demo.execution.controller;

import com.example.demo.execution.dto.request.ExecuteRequest;
import com.example.demo.execution.dto.response.ExecuteStartResponse;
import com.example.demo.execution.service.ExecutionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코드 실행 요청을 받는 컨트롤러.
 */
@RestController
@RequestMapping("/api")
public class ExecutionController {
  private final ExecutionService executionService;

  public ExecutionController(ExecutionService executionService) {
    this.executionService = executionService;
  }

  @PostMapping("/execute")
  public ExecuteStartResponse execute(@RequestBody ExecuteRequest request) {
    // runId를 반환하고 실행은 비동기로 진행된다.
    String runId = executionService.start(request);
    return new ExecuteStartResponse(runId);
  }
}
