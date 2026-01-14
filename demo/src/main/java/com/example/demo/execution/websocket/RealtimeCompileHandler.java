package com.example.demo.execution.websocket;

import com.example.demo.execution.dto.response.ApiResponseResult;
import com.example.demo.execution.execute.ScriptExecutionResult;
import com.example.demo.execution.execute.ScriptExecutor;
import com.example.demo.execution.model.LanguageType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket realtime execution handler.
 *
 * Message protocol:
 * - start: {"type":"start","code":"...","params":[...],"language":"java|python"} // params are argv
 * - input: {"type":"input","data":"..."} // data may include newlines
 * - stop: {"type":"stop"}
 *
 * Responses:
 * - output: {"type":"output","stream":"stdout|stderr","data":"..."}
 * - result: {"type":"result","result":"ApiResponseResult text","stdout":"...","stderr":"...","exitCode":0,"SystemOut":"...","performance":123,"stage":"run"}
 * - error: {"type":"error","message":"..."}
 */
@Slf4j
@Component
public class RealtimeCompileHandler extends TextWebSocketHandler {
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
	
	private final ScriptExecutor scriptExecutor;
	private final ObjectMapper objectMapper;
	private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();
	
	public RealtimeCompileHandler(ScriptExecutor scriptExecutor, ObjectMapper objectMapper) {
		this.scriptExecutor = scriptExecutor;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.put(session.getId(), new SessionState(session, objectMapper));
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		SessionState state = sessions.get(session.getId());
		if(state == null) {
			return;
		}
		
		Map<String, Object> payload;
		try {
			payload = objectMapper.readValue(message.getPayload(), MAP_TYPE);
		} catch (IOException e) {
			state.sendError("invalid json payload");
			return;
		}
		
		String type = asString(payload.get("type"));
		if(type == null) {
			state.sendError("missing message type");
			return;
		}
		
		switch(type) {
			case "start":
				handleStart(state, payload);
				break;
			case "input":
				handleInput(state, payload);
				break;
			case "stop":
				state.stop();
				break;
			default:
				state.sendError("unsupported message type: " + type);
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		SessionState state = sessions.remove(session.getId());
		if(state != null) {
			state.close();
		}
	}
	
	private void handleStart(SessionState state, Map<String, Object> payload) {
		String code = asString(payload.get("code"));
		if(code == null || code.trim().isEmpty()) {
			state.sendError("code is required");
			return;
		}
		
		Object[] params = parseParams(payload.get("params"));
		LanguageType language = LanguageType.from(payload.get("language"));
		state.start(scriptExecutor, language, code, params);
	}
	
	private void handleInput(SessionState state, Map<String, Object> payload) {
		String data = asString(payload.get("data"));
		if(data == null) {
			data = asString(payload.get("input"));
		}
		if(data == null) {
			state.sendError("input data is required");
			return;
		}
		
		state.writeInput(data);
	}
	
	private static String asString(Object value) {
		return value != null ? value.toString() : null;
	}
	
	private static Object[] parseParams(Object rawParams) {
		if(rawParams == null) {
			return new Object[0];
		}
		if(rawParams instanceof List) {
			List<?> list = (List<?>) rawParams;
			return list.toArray();
		}
		if(rawParams instanceof Object[]) {
			return (Object[]) rawParams;
		}
		return new Object[] { rawParams };
	}
	
	private static class SessionState {
		private final WebSocketSession session;
		private final ObjectMapper objectMapper;
		private final Object sendLock = new Object();
		private final Object inputLock = new Object();
		private final ExecutorService executor = Executors.newSingleThreadExecutor();
		private final AtomicBoolean running = new AtomicBoolean(false);
		private Future<?> currentTask;
		private Process currentProcess;
		private PipedInputStream inputStream;
		private PipedOutputStream inputWriter;
		private SessionOutputStream stdout;
		private SessionOutputStream stderr;
		
		SessionState(WebSocketSession session, ObjectMapper objectMapper) {
			this.session = session;
			this.objectMapper = objectMapper;
		}
		
		void start(ScriptExecutor scriptExecutor, LanguageType language, String code, Object[] params) {
			if(!running.compareAndSet(false, true)) {
				sendError("execution already in progress");
				return;
			}
			
			try {
				inputStream = new PipedInputStream();
				inputWriter = new PipedOutputStream(inputStream);
				stdout = new SessionOutputStream(this, "stdout");
				stderr = new SessionOutputStream(this, "stderr");
			} catch (IOException e) {
				running.set(false);
				sendError("failed to prepare io streams");
				return;
			}
			
			currentTask = executor.submit(() -> {
				long beforeTime = System.currentTimeMillis();
				try {
					ScriptExecutionResult result = scriptExecutor.execute(language, code, params, inputStream, stdout, stderr, this::setProcess);
					long afterTime = System.currentTimeMillis();
					String message = result.isSuccess() ? null : (result.getErrorMessage() != null ? result.getErrorMessage() : "execution failed");
					sendResult(result.isSuccess() ? ApiResponseResult.SUCEESS.getText() : ApiResponseResult.FAIL.getText(),
							message,
							"run",
							afterTime - beforeTime,
							result.getExitCode(),
							result.getStdout(),
							result.getStderr());
				} catch (Exception e) {
					log.error("[RealtimeCompileHandler] execution error", e);
					sendError("execution failed");
				} finally {
					stopProcess();
					flushQuietly(stdout);
					flushQuietly(stderr);
					closeInput();
					running.set(false);
				}
			});
		}
		
		void writeInput(String data) {
			if(!running.get() || inputWriter == null) {
				sendError("no active execution");
				return;
			}
			
			synchronized (inputLock) {
				try {
					inputWriter.write(data.getBytes(StandardCharsets.UTF_8));
					inputWriter.flush();
				} catch (IOException e) {
					sendError("failed to write input");
				}
			}
		}
		
		void stop() {
			if(currentTask != null) {
				currentTask.cancel(true);
			}
			stopProcess();
			closeInput();
		}
		
		void close() {
			stop();
			executor.shutdownNow();
		}
		
		private void setProcess(Process process) {
			this.currentProcess = process;
		}
		
		private void stopProcess() {
			if(currentProcess != null && currentProcess.isAlive()) {
				currentProcess.destroyForcibly();
			}
			currentProcess = null;
		}
		
		void sendOutput(String stream, String data) {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("type", "output");
			payload.put("stream", stream);
			payload.put("data", data);
			sendMessage(payload);
		}
		
		void sendResult(String result, String systemOut, String stage, long performanceMs, int exitCode, String stdout, String stderr) {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("type", "result");
			payload.put("result", result);
			payload.put("stage", stage);
			payload.put("performance", performanceMs);
			payload.put("exitCode", exitCode);
			payload.put("stdout", stdout);
			payload.put("stderr", stderr);
			if(systemOut != null) {
				payload.put("SystemOut", systemOut);
			}
			sendMessage(payload);
		}
		
		void sendError(String message) {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("type", "error");
			payload.put("message", message);
			sendMessage(payload);
		}
		
		private void sendMessage(Map<String, Object> payload) {
			if(!session.isOpen()) {
				return;
			}
			try {
				String json = objectMapper.writeValueAsString(payload);
				synchronized (sendLock) {
					session.sendMessage(new TextMessage(json));
				}
			} catch (IOException e) {
				log.debug("[RealtimeCompileHandler] failed to send message", e);
			}
		}
		
		private void closeInput() {
			synchronized (inputLock) {
				if(inputWriter != null) {
					try {
						inputWriter.close();
					} catch (IOException e) {
						// ignore
					} finally {
						inputWriter = null;
					}
				}
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// ignore
					} finally {
						inputStream = null;
					}
				}
			}
		}
		
		private static void flushQuietly(OutputStream stream) {
			if(stream == null) {
				return;
			}
			try {
				stream.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	private static class SessionOutputStream extends OutputStream {
		private static final int FLUSH_THRESHOLD = 1024;
		
		private final SessionState session;
		private final String stream;
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		SessionOutputStream(SessionState session, String stream) {
			this.session = session;
			this.stream = stream;
		}
		
		@Override
		public synchronized void write(int b) {
			buffer.write(b);
			if(b == '\n' || buffer.size() >= FLUSH_THRESHOLD) {
				flushBuffer();
			}
		}
		
		@Override
		public synchronized void write(byte[] b, int off, int len) {
			buffer.write(b, off, len);
			if(buffer.size() >= FLUSH_THRESHOLD || containsNewline(b, off, len)) {
				flushBuffer();
			}
		}
		
		@Override
		public synchronized void flush() {
			flushBuffer();
		}
		
		private void flushBuffer() {
			if(buffer.size() == 0) {
				return;
			}
			String data = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
			buffer.reset();
			session.sendOutput(stream, data);
		}
		
		private static boolean containsNewline(byte[] bytes, int off, int len) {
			for(int i = off; i < off + len; i++) {
				if(bytes[i] == '\n') {
					return true;
				}
			}
			return false;
		}
	}
}
