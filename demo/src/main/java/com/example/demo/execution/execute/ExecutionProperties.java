package com.example.demo.execution.execute;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 실행 엔진 설정.
 *
 * workDir는 컨테이너에서도 접근 가능한 호스트 경로여야 합니다.
 */
@Component
@ConfigurationProperties(prefix = "execution")
public class ExecutionProperties {
	private String workDir;
	private final Docker docker = new Docker();

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public Docker getDocker() {
		return docker;
	}

	public static class Docker {
		private String pythonImage = "python:3.11-alpine";
		private String javascriptImage = "node:20-alpine";
		private String javaImage = "eclipse-temurin:17-jdk";

		public String getPythonImage() {
			return pythonImage;
		}

		public void setPythonImage(String pythonImage) {
			this.pythonImage = pythonImage;
		}

		public String getJavascriptImage() {
			return javascriptImage;
		}

		public void setJavascriptImage(String javascriptImage) {
			this.javascriptImage = javascriptImage;
		}

		public String getJavaImage() {
			return javaImage;
		}

		public void setJavaImage(String javaImage) {
			this.javaImage = javaImage;
		}
	}
}
