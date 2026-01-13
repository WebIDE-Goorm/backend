package com.example.demo.execution.execute;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 메서드 실행 유틸리티
 * 
 * 실행 유틸리티:
 * - 별도 스레드에서 메서드 실행
 */
public class MethodExecutation {
	/**
	 * 메서드 실행
	 * 
	 * 처리 과정:
	 * 1. 리플렉션으로 메서드 정보 획득
	 * 2. 별도 스레드에서 메서드 실행
	 * 
	 * @param obj 실행할 클래스 인스턴스
	 * @param methodName 실행할 메서드명
	 * @param params 메서드에 전달할 파라미터 배열
	 * @param arguments 파라미터 타입 배열
	 * @return 실행 결과 맵 (result: 성공여부, return: 반환값)
	 */
	public static Map<String, Object> timeOutCall(Object obj, String methodName, Object[] params, Class<? extends Object> arguments[]) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Class<?> targetClass = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
		
		// 1단계: 리플렉션으로 실행할 메서드 정보 획득
		final Method objMethod = targetClass.getMethod(methodName, arguments);
		Object target = obj;
		if(obj instanceof Class) {
			if(Modifier.isStatic(objMethod.getModifiers())) {
				target = null;
			} else {
				target = targetClass.getDeclaredConstructor().newInstance();
			}
		}
		final Object invokeTarget = target;
		final Object[] invokeParams = params;
		
		// 2단계: 별도 스레드에서 메서드 실행 (타임아웃 체크를 위해)
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Callable<Map<String, Object>> task = new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				Map<String, Object> callMap = new HashMap<String, Object>();
				
				// 리플렉션으로 메서드 실행
				callMap.put("return", objMethod.invoke(invokeTarget, invokeParams));
				
				callMap.put("result", true);
				return callMap;
			}
		};
		
		// 3단계: 메서드 실행
		Future<Map<String, Object>> future = executorService.submit(task);
		try {
			returnMap = future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			future.cancel(true);
			returnMap.put("result", false);
			returnMap.put("error", e);
		} catch (ExecutionException e) {
			returnMap.put("result", false);
			returnMap.put("error", e.getCause() != null ? e.getCause() : e);
		} finally {
			// 스레드 풀 종료
			executorService.shutdownNow();
		}
		
		return returnMap;
	}
}
