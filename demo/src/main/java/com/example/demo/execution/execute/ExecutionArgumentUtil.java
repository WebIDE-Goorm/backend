package com.example.demo.execution.execute;

import java.util.List;

/**
 * 실행 파라미터를 argv 배열로 정규화합니다.
 */
public final class ExecutionArgumentUtil {
	private ExecutionArgumentUtil() {}

	public static String[] toStringArgs(Object[] params) {
		if(params == null || params.length == 0) {
			return new String[0];
		}
		if(params.length == 1) {
			Object first = params[0];
			if(first instanceof String[]) {
				return (String[]) first;
			}
			if(first instanceof List) {
				List<?> list = (List<?>) first;
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
}
