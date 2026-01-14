package com.example.demo.execution.model;

import java.util.Locale;

public enum LanguageType {
	JAVA("java"),
	PYTHON("python", "py"),
	JAVASCRIPT("javascript", "js", "node", "nodejs");
	
	private final String[] aliases;
	
	LanguageType(String... aliases) {
		this.aliases = aliases;
	}
	
	public static LanguageType from(Object raw) {
		if(raw == null) {
			return JAVA;
		}
		String value = raw.toString().toLowerCase(Locale.ROOT).trim();
		for(LanguageType type : values()) {
			for(String alias : type.aliases) {
				if(alias.equals(value)) {
					return type;
				}
			}
		}
		return JAVA;
	}
}
