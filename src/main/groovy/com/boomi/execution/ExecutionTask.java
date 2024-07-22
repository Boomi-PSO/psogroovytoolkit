package com.boomi.execution;

import java.util.Date;
import java.util.UUID;

public class ExecutionTask {
	private UUID topLevelExecutionID = UUID.randomUUID();
	private UUID topLevelProcessID = UUID.randomUUID();
	private UUID topLevelComponentID = UUID.randomUUID();
	private Date startDate = new Date();

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
		return "Mock Process";
	}

	public ExecutionTask getParent() {
		return null;
	}

	public Date getStartTime() {
		return startDate;
	}
}
