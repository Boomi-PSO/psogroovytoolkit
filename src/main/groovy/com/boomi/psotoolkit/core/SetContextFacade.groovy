package com.boomi.psotoolkit.core

/**
 * Description : This Groovy script sets dynamic process properties within the process execution context.
 *
 * Input:
 *       document "on the flow"
 *               
 * Output:
 *       DPP_FWK_Directory - top level component directory from the build repository
 **/

import com.boomi.execution.ExecutionManager;
import com.boomi.execution.ExecutionUtil;
class SetContextFacade {
	// Constants
	private final static String DPP_FWK_DIRECTORY = "DPP_FWK_Directory";
	// Setup global objects
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public SetContextFacade(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		String folderFullPath = ExecutionUtil.getComponent(ExecutionManager.getCurrent().getTopLevelComponentId())?.getFolderId()?.getName();
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_DIRECTORY, folderFullPath, false);

		logger.fine("DPP_FWK_Directory=" + folderFullPath);

		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			dataContext.storeStream(dataContext.getStream(i), dataContext.getProperties(i));
		}
	}
}