package com.boomi.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.boomi.model.platform.Component;

public class ExecutionUtil {
	private static final String PROCESS_NAME = "PROCESS_NAME";
	public static Map<String, String> dynamicProcessProperties = new HashMap<String, String>();
	public static Map<String, String> processProperties = new HashMap<String, String>();
	public static Map<String, String> executionProperties = new HashMap<String, String>();;

	public static String getDynamicProcessProperty(String key) {
		return dynamicProcessProperties.get(key);
	}

	public static String getProcessProperty(String componentId, String propertyKey) {
		return processProperties.get(propertyKey);
	}

	public static void setProcessProperty(String componentId, String propertyKey, String value) {

	}

	public static void setDynamicProcessProperty(String key, String value, Boolean persist) {
		dynamicProcessProperties.put(key, value);
	}

	public static String getRuntimeExecutionProperty(String propertyName) {
		String executionProperty = executionProperties.get(propertyName);
		if (executionProperty == null) {
			if (PROCESS_NAME.equals(propertyName)) {
				executionProperty = "[TEST] Current Process";
			} else {
				executionProperty = UUID.randomUUID().toString();
			}
			executionProperties.put(propertyName, executionProperty);
		}
		return executionProperty;
	}

	public static Component getComponent(String componentId) {
		return new Component();
	}

	public static Object getBaseLogger() {
		return new ExecutionUtil.Logger();
	}

	static class Logger {
		public void info(Object msg) {
			System.out.println(msg);
		}

		public void severe(Object msg) {
			System.out.println(msg);
		}

		public void fine(Object msg) {
			System.out.println(msg);
		}

		public void finest(Object msg) {
			System.out.println(msg);
		}
	}
}
