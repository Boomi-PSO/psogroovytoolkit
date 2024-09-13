package com.boomi.psotoolkit.core

import com.boomi.execution.ExecutionUtil;
class GetExecDuration extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DPP_FWK_STARTTIME = "DPP_FWK_StartTime";
	private static final String DPP_FWK_EXEC_DURATION_MSEC = "DPP_FWK_EXEC_DURATION_MSEC";

	public GetExecDuration(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		String startIime = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_STARTTIME);
		if (startIime != null && startIime.length() > 0) {
			long diffInMillies = Math.abs((new Date()).getTime() - Long.parseLong(startIime));
			ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_DURATION_MSEC, Long.toString(diffInMillies), false);
			logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_EXEC_DURATION_MSEC, diffInMillies] as Object[]));
		}

		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			dataContext.storeStream(dataContext.getStream(i), dataContext.getProperties(i));
		}
	}
}