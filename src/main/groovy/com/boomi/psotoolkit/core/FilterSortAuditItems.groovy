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
		boolean fullLog = false;
		boolean auditLog = false;
		String disableAudit = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_AUDIT);
		logger.fine("disableAudit = " + disableAudit);
		String disableNotify = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_NOTIFICATION);
		logger.fine("disableNotify = " + disableNotify);
		String errorFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_ERROR_LEVEL);
		logger.fine("errorFlag = " + errorFlag);
		String warnFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_WARN_LEVEL);
		logger.fine("warnFlag = " + warnFlag);
		if (NO.equals(disableAudit) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		else if (((TRUE.equals(errorFlag)) || (TRUE.equals(warnFlag))) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		else if (NO.equals(disableAudit) && YES.equals(disableNotify)) {
			auditLog = TRUE;
		}
		logger.fine("fulllog = " + fullLog);
		logger.fine("auditlog = " + auditLog);
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
			if (fullLog || LOG.equals(level)) {
				dataContext.storeStream(dataContext.getStream(index), dataContext.getProperties(index));
			}
		}
	}
}