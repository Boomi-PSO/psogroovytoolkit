package com.boomi.execution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExecutionTask {
	private UUID topLevelExecutionID = UUID.randomUUID();
	private UUID topLevelProcessID = UUID.randomUUID();
	private UUID topLevelComponentID = UUID.randomUUID();
	private Date startDate = new Date();

	private List<String> processCallStack = new ArrayList<String>();
	private int currentCallIndex = 0;

	public ExecutionTask() {
		processCallStack.add("[TEST] Level 3");
		processCallStack.add("[TEST] Level 2");
		processCallStack.add("[TEST] Level 1");
		processCallStack.add("Mock Process");
	}

	public String getTopLevelExecutionId() {
		return topLevelExecutionID.toString();
	}

	public String getTopLevelProcessId() {
		return topLevelProcessID.toString();
	}

	public String getTopLevelComponentId() {
		return topLevelComponentID.toString();
	}

	public String getComponentId() {
		return topLevelComponentID.toString();
	}

	public String getProcessName() {
		return processCallStack.get(currentCallIndex);
	}

	public ExecutionTask getParent() {
		if (currentCallIndex == processCallStack.size() - 1) {
			currentCallIndex = 0;
			return null;
		}
		currentCallIndex++;
		return this;
	}

	public Date getStartTime() {
		return startDate;
	}
}
