package com.boomi.execution;

public class ExecutionManager {

	static ExecutionTask et = new ExecutionTask();

	public static ExecutionTask getCurrent() {
		return et;
	}
}
