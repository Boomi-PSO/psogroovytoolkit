package com.boomi.psotoolkit.core

import com.boomi.execution.ExecutionUtil;

/**
 * Description : This Groovy script sets audit log document properties
 * *             and creates audit log object.
 *
 * Input:
 *       audit log record "on the flow""
 *       DDP_FWK_LEVEL - audit log level
 *       DDP_FWK_SORT_TS - DDP for sorting documents in ascending order
 *       DPP_FWK_DISABLE_NOTIFICATION - if not already set then default property
 *       DPP_FWK_DISABLE_AUDIT - if not already set then default property
 * *               
 * Output:
 *       audit log record "on the flow""- filtered and sorted
 **/
class FilterSortAuditItems extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_FWK_SORT_TS = "document.dynamic.userdefined.DDP_FWK_SORT_TS";
	private static final String DDP_FWK_LEVEL = "document.dynamic.userdefined.DDP_FWK_LEVEL"
	private static final String LOG = "LOG"
	private static final String DPP_FWK_DISABLE_AUDIT = "DPP_FWK_DISABLE_AUDIT"
	private static final String DPP_FWK_DISABLE_NOTIFICATION = "DPP_FWK_DISABLE_NOTIFICATION"
	private static final String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL"
	private static final String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL"
	private static final String NO = "0"
	private static final String YES = "1"
	private static final String TRUE = "true";

	public FilterSortAuditItems(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		// Setup to output nothing
		boolean fullLog = false;
		boolean auditLog = false;
		String disableAudit = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_AUDIT);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_DISABLE_AUDIT, disableAudit] as Object[]));
		String disableNotify = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_NOTIFICATION);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_DISABLE_NOTIFICATION, disableNotify] as Object[]));
		String errorFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_ERROR_LEVEL);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_ERROR_LEVEL, errorFlag] as Object[]));
		String warnFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_WARN_LEVEL);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_WARN_LEVEL, warnFlag] as Object[]));
		// if both flags are NO (do not disable), then output all
		if (NO.equals(disableAudit) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		// if disableNotify flag is NO (do not disable) and there is at least one ERROR or WARNING then output all
		else if ((TRUE.equals(errorFlag) || TRUE.equals(warnFlag)) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		// if disableAudit flag is NO (do not disable) and disableNotify flag is YES, only output LOG messages
		else if (NO.equals(disableAudit) && YES.equals(disableNotify)) {
			auditLog = TRUE;
		}
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, ["fullLog", fullLog] as Object[]));
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, ["auditLog", auditLog] as Object[]));
		// Init temp collections
		SortedMap sortedMap = new TreeMap();
		// Loop through documents and store the sort-by-values and document indices in the sortedMap.
		for ( int i = 0; i < dataContext.getDataCount(); i++ ) {
			Properties props = dataContext.getProperties(i);
			String sortByValue = props.getProperty(DDP_FWK_SORT_TS) + i;
			sortedMap.put(sortByValue, i);
		}
		// Output sorted docs
		sortedMap.values().each { int index ->
			String level = dataContext.getProperties(index).getProperty(DDP_FWK_LEVEL);
			if (fullLog || (auditLog && LOG.equals(level))) {
				dataContext.storeStream(dataContext.getStream(index), dataContext.getProperties(index));
			}
		}
	}
}