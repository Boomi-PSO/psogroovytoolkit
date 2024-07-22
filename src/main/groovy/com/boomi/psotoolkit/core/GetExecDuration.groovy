package com.boomi.psotoolkit.core

import com.boomi.execution.ExecutionUtil;
class GetExecDuration {
	// Constants
	private final static String DPP_FWK_STARTTIME = "DPP_FWK_StartTime";
	private final static String DPP_FWK_EXEC_DURATION_MSEC = "DPP_FWK_EXEC_DURATION_MSEC";
	// Setup global objects
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public GetExecDuration(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		String startIime = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_STARTTIME);
		if (startIime != null && startIime.length() > 0) {
			long diffInMillies = Math.abs((new Date()).getTime() - Long.parseLong(startIime));
			ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_DURATION_MSEC, Long.toString(diffInMillies), false);
			logger.fine("Duration (ms): " + diffInMillies);
		}

		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			dataContext.storeStream(dataContext.getStream(i), dataContext.getProperties(i));
		}
	}
}