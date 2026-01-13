package com.example.demo.execution.controller.compile;


import com.example.demo.execution.builder.CompileBuilder;
import com.example.demo.execution.dto.response.ApiResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Java 코드 컴파일 및 실행 API 컨트롤러
 * 
 * 프로그램 흐름:
 * 1. 클라이언트가 POST /compile 요청으로 Java 소스코드 전송
 * 2. CompileBuilder를 통해 코드 컴파일 및 클래스 로드
 * 3. 동적으로 로드된 클래스의 runMethod 또는 main 실행
 * 4. 실행 결과(반환값, System.out 출력, 실행시간) 반환
 */
@CrossOrigin
@RestController
public class CompileController {
	@Autowired
  CompileBuilder builder;
	
	/**
	 * Java 코드 컴파일 및 실행 API
	 * 
	 * 요청 형식:
	 * {
	 *   "code": "Java 소스코드 문자열",
	 *   "params": [파라미터1, 파라미터2, ...]  // 선택사항: runMethod가 파라미터를 받는 경우에만 필요
	 * }
	 * 
	 * 예시 1 (파라미터 있음):
	 * {
	 *   "code": "public class DynamicClass { public String runMethod(String[] a, String[] b) { return \"result\"; } }",
	 *   "params": [["marina", "josipa"], ["marina"]]
	 * }
	 * 
	 * 예시 2 (파라미터 없음):
	 * {
	 *   "code": "public class DynamicClass { public String runMethod() { return \"hello\"; } }"
	 * }
	 * 
	 * 예시 3 (표준 입력 사용):
	 * {
	 *   "code": "public class DynamicClass { public String runMethod() throws Exception { java.util.Scanner sc = new java.util.Scanner(System.in); String line = sc.nextLine(); return line; } }",
	 *   "input": "hello world"
	 * }
	 * 
	 * 예시 4 (main 실행):
	 * {
	 *   "code": "public class Main { public static void main(String[] args) { System.out.println(\"hello\"); } }"
	 * }
	 * 
	 * 응답 형식:
	 * {
	 *   "result": "성공" 또는 "실패",
	 *   "return": "메서드 반환값",
	 *   "SystemOut": "System.out.println() 출력 내용",
	 *   "performance": 실행시간(밀리초)
	 * }
	 */
	@PostMapping(value="compile")
	public Map<String, Object> compileCode(@RequestBody Map<String, Object> input) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		// 1단계: 전달받은 Java 소스코드를 컴파일하고 클래스 인스턴스 생성
		// 컴파일 실패 시 에러 메시지(String) 반환, 성공 시 "클래스 인스턴스" 반환
		Object obj = builder.compileCode(input.get("code").toString());
		
		// 컴파일 실패 처리: compileCode가 String을 반환하면 컴파일 에러 메시지
		if(obj instanceof String) {
			returnMap.put("result", ApiResponseResult.FAIL.getText());
			returnMap.put("SystemOut", obj.toString());
			return returnMap;
		}
		
		// 2단계: 실행 시간 측정 시작
		long beforeTime = System.currentTimeMillis();
		
		// 3단계: 메서드 실행에 필요한 파라미터 준비
		// runMethod에 전달할 파라미터 (사용자가 작성한 코드가 파라미터를 받는 경우에만 필요)
		// params가 없으면 빈 배열로 실행 (파라미터 없는 메서드도 실행 가능)
		Object[] params = new Object[0];
		
		if(input.containsKey("params") && input.get("params") != null) {
			// JSON에서 List로 전달된 경우 배열로 변환
			if(input.get("params") instanceof java.util.List) {
				java.util.List<?> paramList = (java.util.List<?>) input.get("params");
				params = paramList.toArray();
			} else if(input.get("params") instanceof Object[]) {
				params = (Object[]) input.get("params");
			} else {
				params = new Object[] {input.get("params")};
			}
		}
		
		// 4단계: 표준 입력 데이터 준비 (System.in 사용을 위해)
		// input 필드가 있으면 표준 입력으로 사용, 없으면 null
		String stdinData = null;
		if(input.containsKey("input") && input.get("input") != null) {
			stdinData = input.get("input").toString();
		}
		
		// 5단계: 컴파일된 클래스의 runMethod 실행
		// System.out 출력을 캡처하고 타임아웃 체크하며 실행
		Map<String, Object> output = builder.runObject(obj, params, stdinData);
		long afterTime = System.currentTimeMillis();
		
		// 5단계: 실행 결과를 응답 맵에 저장
		returnMap.putAll(output);
		returnMap.put("performance", (afterTime - beforeTime));
		
		return returnMap;
	}
	
	/**
	 * 서버 종료 API (테스트용)
	 */
	@PostMapping(value="stop")
	public void stopTomcatTest() throws Exception {
		System.exit(1);
	}
}
