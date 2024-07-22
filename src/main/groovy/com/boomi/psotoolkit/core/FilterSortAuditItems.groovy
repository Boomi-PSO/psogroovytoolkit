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
class FilterSortAuditItems {
	// Constants
	private final static String DDP_FWK_SORT_TS = "document.dynamic.userdefined.DDP_FWK_SORT_TS";
	private final static String DDP_FWK_LEVEL = "document.dynamic.userdefined.DDP_FWK_LEVEL";
	// Setup global objects
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public FilterSortAuditItems(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		boolean fullLog = false;
		boolean auditLog = false;
		String disableAudit = ExecutionUtil.getDynamicProcessProperty("DPP_FWK_DISABLE_AUDIT");
		logger.fine("disableAudit = " + disableAudit);
		String disableNotify = ExecutionUtil.getDynamicProcessProperty("DPP_FWK_DISABLE_NOTIFICATION");
		logger.fine("disableNotify = " + disableNotify);
		String errorFlag = ExecutionUtil.getDynamicProcessProperty("DPP_FWK_ERROR_LEVEL");
		logger.fine("errorFlag = " + errorFlag);
		String warnFlag = ExecutionUtil.getDynamicProcessProperty("DPP_FWK_WARN_LEVEL");
		logger.fine("warnFlag = " + warnFlag);
		if (disableAudit.equals("0") && disableNotify.equals("0")) {
			fullLog = true;
		}
		else if (((errorFlag != null && errorFlag.equals("true")) || (warnFlag != null && warnFlag.equals("true"))) && disableNotify.equals("0")) {
			fullLog = true;
		}
		else if (disableAudit.equals("0") && disableNotify.equals("1")) {
			auditLog = true;
		}
		logger.fine("fulllog = " + fullLog);
		logger.fine("auditlog = " + auditLog);
		try {
			// Init temp collections
			SortedMap sortedMap = new TreeMap();

			// Loop through documents and store the sort-by-values and document indices in the sortedMap.
			for ( int i = 0; i < dataContext.getDataCount(); i++ ) {
				Properties props = dataContext.getProperties(i);
				String sortByValue = props.getProperty(DDP_FWK_SORT_TS) + "_" + i;
				sortedMap.put(sortByValue, i);
			}
			// Output sorted docs
			sortedMap.values().each { int index ->
				String level = dataContext.getProperties(index).getProperty(DDP_FWK_LEVEL);
				if (fullLog || (auditLog && level != null && level.equals("LOG"))) {
					dataContext.storeStream(dataContext.getStream(index), dataContext.getProperties(index));
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.info(sw.toString());
			throw new Exception(e.getMessage() + "\nCheck process log for stack trace.");
		}
	}
}