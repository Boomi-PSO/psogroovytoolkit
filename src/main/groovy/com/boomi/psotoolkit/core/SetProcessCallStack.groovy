package com.boomi.psotoolkit.core

/* **************************************************************************
 Get DDP_FWK_ProcessCallStack
 IN : [Describe inbound arguments]
 OUT: [Describe outbound arguments]
 ************************************************************************** */
import com.boomi.execution.ExecutionManager;
import com.boomi.execution.ExecutionTask;
import com.boomi.execution.ExecutionUtil;

class SetProcessCallStack extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_FWK_NS_LEVEL = "document.dynamic.userdefined.DDP_FWK_NS_LEVEL"
	private static final String ERROR = 'ERROR'
	private static final String WARNING = 'WARNING'
	private static final String PROCESS_NAME = 'PROCESS_NAME'
	private static final String DDP_FWK_NS_PROCESS_CALL_STACK = "document.dynamic.userdefined.DDP_FWK_NS_ProcessCallStack"
	private static final String ARROW_RIGHT = " > "

	public SetProcessCallStack(Object dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		int docCount = dataContext.getDataCount();
		logger.fine("In-Document Count=" + docCount);

		String processName = getProcessCallStack()

		for (int docNo = 0; docNo < docCount; docNo++) {
			Properties props = dataContext.getProperties(docNo);
			String level = props.getProperty(DDP_FWK_NS_LEVEL)
			if (level == ERROR || level == WARNING) {
				props.setProperty(DDP_FWK_NS_PROCESS_CALL_STACK, processName)
				logger.fine("DDP_FWK_NS_ProcessCallStack: " + processName)
			}
			dataContext.storeStream(dataContext.getStream(docNo), props)
		}
	}

	private void updateCallStack(StringBuilder callStack, String processName, boolean insert) {
		if (callStack.length() == 0) {
			callStack.append(processName)
		}
		else if (callStack.indexOf(processName) < 0) {
			if (insert) {
				callStack.insert(0, ARROW_RIGHT).insert(0, processName);
			}
			else {
				callStack.append(ARROW_RIGHT).append(processName);
			}
		}
	}

	private void buildCallStack(ExecutionTask currentTask, StringBuilder callStack) {
		if (currentTask) {
			String processName = currentTask.getProcessName();
			updateCallStack(callStack, processName, true);
			buildCallStack(currentTask.getParent(), callStack);
		}
	}

	private String getProcessCallStack() {
		StringBuilder callStack = new StringBuilder();
		buildCallStack(ExecutionManager.getCurrent(), callStack);
		updateCallStack(callStack, ExecutionUtil.getRuntimeExecutionProperty(PROCESS_NAME), false);
		return callStack.toString();
	}
}
