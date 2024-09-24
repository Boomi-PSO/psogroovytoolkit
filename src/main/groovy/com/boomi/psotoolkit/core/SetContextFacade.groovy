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
class SetContextFacade extends CoreCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DPP_FWK_DIRECTORY = "DPP_FWK_Directory";

	public SetContextFacade(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		String folderFullPath = ExecutionUtil.getComponent(ExecutionManager.getCurrent().getTopLevelComponentId())?.getFolderId()?.getName();
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_DIRECTORY, folderFullPath, false);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_DIRECTORY, folderFullPath] as Object[]));
		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			dataContext.storeStream(dataContext.getStream(i), dataContext.getProperties(i));
		}
	}
}