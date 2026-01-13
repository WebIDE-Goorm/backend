package com.example.demo.execution.builder;

import com.example.demo.execution.dto.response.ApiResponseResult;
import com.example.demo.execution.execute.MethodExecutation;
import com.example.demo.execution.util.common.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 소스코드 컴파일 및 실행 빌더
 * 
 * 주요 기능:
 * 1. Java 소스코드를 파일로 저장하고 컴파일
 * 2. 컴파일된 클래스를 동적으로 로드하여 인스턴스 생성
 * 3. 로드된 클래스의 메서드를 리플렉션으로 실행
 */
@Slf4j
@Component
public class CompileBuilder {
	// 임시 파일 저장 경로 (각 요청마다 UUID로 고유 디렉토리 생성)
	private final String path = "/Users/yangjaehyeog/Desktop";
	private static final Object IO_LOCK = new Object();
	private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("(?m)^\\s*(public\\s+)?class\\s+([A-Za-z_][A-Za-z0-9_]*)");
	
	/**
	 * Java 소스코드를 컴파일하고 클래스 인스턴스 생성
	 * 
	 * 처리 과정:
	 * 1. UUID로 고유 디렉토리 생성
	 * 2. 소스코드를 DynamicClass.java 파일로 저장
	 * 3. JavaCompiler로 컴파일
	 * 4. URLClassLoader로 클래스 로드
	 * 5. 클래스 인스턴스 생성 및 반환
	 * 
	 * @param body Java 소스코드 문자열
	 * @return 컴파일 성공 시 클래스 정보, 실패 시 에러 메시지(String)
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	public Object compileCode(String body) throws Exception {
		// 1단계: 고유한 임시 디렉토리 경로 생성
		String uuid = UUIDUtil.createUUID();
		String uuidPath = path + uuid + "/";
		String className = extractClassName(body);
		
		if(className == null) {
			return "클래스명을 찾을 수 없습니다.";
		}
		
		File newFolder = new File(uuidPath);
		File sourceFile = new File(uuidPath + className + ".java");
		File classFile = new File(uuidPath + className + ".class");
		
		Class<?> cls = null;
		
		// 컴파일 에러 메시지를 캡처하기 위한 스트림 설정
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream origErr = System.err;
		
		try {
			// 2단계: 디렉토리 생성 및 소스 파일 저장
			newFolder.mkdir();
			new FileWriter(sourceFile).append(body).close();
			
			// 3단계: Java 컴파일러로 소스 파일 컴파일
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			
			// System.err를 ByteArrayOutputStream으로 리다이렉트하여 에러 메시지 캡처
			System.setErr(new PrintStream(err));
			
			// 컴파일 실행 (반환값: 0=성공, 1=실패)
			int compileResult = compiler.run(null, null, null, sourceFile.getPath());
			
			// 컴파일 실패 시 에러 메시지 반환
			if(compileResult == 1) {
				return err.toString();
			}
			
			// 4단계: 컴파일된 클래스 파일을 동적으로 로드
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {new File(uuidPath).toURI().toURL()});
			cls = Class.forName(className, true, classLoader);
			
			// 5단계: 로드된 클래스 정보 반환
			return cls;

		} catch (Exception e) {
			log.error("[CompileBuilder] 소스 컴파일 중 에러 발생 :: {}", e.getMessage());
			e.printStackTrace();
			return null;

		} finally {
			// System.err 원상복구
			System.setErr(origErr);
			
			// 임시 파일 정리 (보안 및 디스크 공간 확보)
			if(sourceFile.exists())
				sourceFile.delete();
			if(classFile.exists())
				classFile.delete();
			if(newFolder.exists())
				newFolder.delete();
		}
	}

	private static String extractClassName(String body) {
		if(body == null) {
			return null;
		}
		Matcher matcher = CLASS_NAME_PATTERN.matcher(body);
		if(matcher.find()) {
			return matcher.group(2);
		}
		return null;
	}
	
	/**
	 * 컴파일된 클래스 인스턴스의 runMethod 실행
	 * 
	 * 처리 과정:
	 * 1. 리플렉션으로 runMethod 메서드 정보 획득
	 * 2. System.in/out/err를 리다이렉트 (표준 입력/출력 캡처)
	 * 3. 메서드 실행
	 * 4. 실행 결과 및 System.out 출력 반환
	 * 
	 * @param obj 컴파일된 클래스 인스턴스
	 * @param params 메서드 실행에 전달할 파라미터 배열
	 * @param stdinData 표준 입력 데이터 (null이면 System.in 사용 안 함)
	 * @return 실행 결과 맵 (result, return, SystemOut 포함)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> runObject(Object obj, Object[] params, String stdinData) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		// 1단계: 리플렉션으로 실행할 메서드 정보 준비
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		InputStream stdin = null;
		
		if(stdinData != null && !stdinData.isEmpty()) {
			// 줄바꿈 문자를 포함하여 입력 데이터 준비
			stdin = new ByteArrayInputStream((stdinData + "\n").getBytes());
		}
		
		// 2단계: 메서드 실행
		Map<String, Object> result = runObjectWithStreams(obj, params, stdin, out, err);
		
		// 3단계: 실행 결과 처리
		if(Boolean.TRUE.equals(result.get("result"))) {
			// 실행 성공
			returnMap.put("result", ApiResponseResult.SUCEESS.getText());
			returnMap.put("return", result.get("return"));
			
			// 에러 출력이 있으면 우선 표시, 없으면 정상 출력 표시
			if(err.toString() != null && !err.toString().equals("")) {
				returnMap.put("SystemOut", err.toString());
			} else {
				returnMap.put("SystemOut", out.toString());
			}
		} else {
			// 실패 또는 타임아웃 발생
			returnMap.put("result", ApiResponseResult.FAIL.getText());
			if(result.get("error") != null) {
				returnMap.put("SystemOut", String.valueOf(result.get("error")));
			} else if(err.toString() != null && !err.toString().equals("")) {
				returnMap.put("SystemOut", err.toString());
			} else {
				returnMap.put("SystemOut", out.toString());
			}
		}
		
		return returnMap;
	}

	/**
	 * 컴파일된 클래스 인스턴스를 스트리밍 입출력으로 실행
	 *
	 * @param stdin 표준 입력 스트림 (null이면 기존 System.in 유지)
	 * @param stdout 표준 출력 스트림
	 * @param stderr 표준 에러 스트림
	 * @return 실행 결과 맵 (result, return, error 포함)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> runObjectWithStreams(Object obj, Object[] params, InputStream stdin, OutputStream stdout, OutputStream stderr) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Object[] safeParams = (params != null) ? params : new Object[0];
		Class<?> targetClass = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
		MethodSelection selection = resolveMethod(targetClass, safeParams);
		if(selection == null) {
			returnMap.put("result", false);
			returnMap.put("error", "runMethod 또는 main 메서드를 찾을 수 없습니다.");
			return returnMap;
		}
		
		PrintStream origOut = System.out;
		PrintStream origErr = System.err;
		InputStream origIn = System.in;
		PrintStream wrappedOut = (stdout != null) ? new PrintStream(stdout, true) : null;
		PrintStream wrappedErr = (stderr != null) ? new PrintStream(stderr, true) : null;
		
		synchronized (IO_LOCK) {
			try {
				if(stdin != null) {
					System.setIn(stdin);
				}
				if(wrappedOut != null) {
					System.setOut(wrappedOut);
				}
				if(wrappedErr != null) {
					System.setErr(wrappedErr);
				}
				
				Map<String, Object> result = MethodExecutation.timeOutCall(obj, selection.methodName, selection.params, selection.arguments);
				returnMap.putAll(result);
			} finally {
				if(wrappedOut != null) {
					wrappedOut.flush();
				}
				if(wrappedErr != null) {
					wrappedErr.flush();
				}
				System.setIn(origIn);
				System.setOut(origOut);
				System.setErr(origErr);
			}
		}
		
		return returnMap;
	}

	private static MethodSelection resolveMethod(Class<?> targetClass, Object[] params) {
		MethodSelection runMethodSelection = resolveRunMethod(targetClass, params);
		if(runMethodSelection != null) {
			return runMethodSelection;
		}
		MethodSelection mainSelection = resolveMainMethod(targetClass, params);
		if(mainSelection != null) {
			return mainSelection;
		}
		return null;
	}

	private static MethodSelection resolveRunMethod(Class<?> targetClass, Object[] params) {
		Class<?>[] arguments = toArgumentTypes(params);
		if(arguments.length > 0 && hasMethod(targetClass, "runMethod", arguments)) {
			return new MethodSelection("runMethod", params, arguments);
		}
		if(arguments.length == 0 && hasMethod(targetClass, "runMethod", new Class<?>[0])) {
			return new MethodSelection("runMethod", new Object[0], new Class<?>[0]);
		}
		return null;
	}

	private static MethodSelection resolveMainMethod(Class<?> targetClass, Object[] params) {
		if(!hasMethod(targetClass, "main", new Class<?>[] { String[].class })) {
			return null;
		}
		String[] args = toStringArgs(params);
		return new MethodSelection("main", new Object[] { args }, new Class<?>[] { String[].class });
	}

	private static boolean hasMethod(Class<?> targetClass, String methodName, Class<?>[] arguments) {
		try {
			targetClass.getMethod(methodName, arguments);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	private static Class<?>[] toArgumentTypes(Object[] params) {
		Class<?>[] arguments = new Class<?>[params.length];
		for(int i = 0; i < params.length; i++) {
			arguments[i] = (params[i] != null) ? params[i].getClass() : Object.class;
		}
		return arguments;
	}

	private static String[] toStringArgs(Object[] params) {
		if(params == null || params.length == 0) {
			return new String[0];
		}
		if(params.length == 1) {
			Object first = params[0];
			if(first instanceof String[]) {
				return (String[]) first;
			}
			if(first instanceof java.util.List) {
				java.util.List<?> list = (java.util.List<?>) first;
				String[] args = new String[list.size()];
				for(int i = 0; i < list.size(); i++) {
					args[i] = String.valueOf(list.get(i));
				}
				return args;
			}
		}
		String[] args = new String[params.length];
		for(int i = 0; i < params.length; i++) {
			args[i] = String.valueOf(params[i]);
		}
		return args;
	}

	private static class MethodSelection {
		private final String methodName;
		private final Object[] params;
		private final Class<?>[] arguments;
		
		MethodSelection(String methodName, Object[] params, Class<?>[] arguments) {
			this.methodName = methodName;
			this.params = params;
			this.arguments = arguments;
		}
	}
}
