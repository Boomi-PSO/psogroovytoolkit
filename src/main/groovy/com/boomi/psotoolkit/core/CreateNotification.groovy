package com.boomi.psotoolkit.core

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.boomi.execution.ExecutionUtil;

import groovy.json.JsonBuilder;
/* **************************************************************************
 This script sets notification document properties and creates notification object
 IN : DDP_FWK_NS_INTERNAL_ID - unique value
 OUT: notification JSON document "on the flow""
 DPP_FWK_EXEC_SUMMARY_FLAG - true, if CRUD statistics provided
 DPP_FWK_ERROR_LEVEL - true if level ERROR found
 DPP_FWK_WARN_LEVEL- true if level WARNING found
 ************************************************************************** */

class CreateNotification {
	// Constants
	private final static String SCRIPT_NAME = "CreateNotification";
	private final static String DDP_FWK_DOC_KEY = "document.dynamic.userdefined.DDP_FWK_DOC_KEY";
	private final static String DDP_FWK_DOC_VAL = "document.dynamic.userdefined.DDP_FWK_DOC_VAL";
	private final static String DDP_FWK_DOC_CRUD_TYPE = "document.dynamic.userdefined.DDP_FWK_DOC_CRUD_TYPE";
	private final static String DDP_FWK_NS_LEVEL = "document.dynamic.userdefined.DDP_FWK_NS_LEVEL";
	private final static String DDP_FWK_NS_CLASS = "document.dynamic.userdefined.DDP_FWK_NS_CLASS";
	private final static String DDP_FWK_NS_DOC = "document.dynamic.userdefined.DDP_FWK_NS_DOC";
	private final static String DDP_FWK_NS_MSG = "document.dynamic.userdefined.DDP_FWK_NS_MSG";
	private final static String DDP_FWK_NS_INTERNAL_ID = "document.dynamic.userdefined.DDP_FWK_NS_INTERNAL_ID";
	private final static String DDP_FWK_NS_PROCESSCALLSTACK = "document.dynamic.userdefined.DDP_FWK_NS_ProcessCallStack";
	private final static String DPP_FWK_EXEC_SUMMARY_FLAG = "DPP_FWK_EXEC_SUMMARY_FLAG";
	private final static String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	private final static String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";
	private final static String MSG_ILLEGAL_CHARS = "[></?]";
	private final static String WHITE_SPACES = "^\\s+|\\s+\$|\\s+(?=\\s)";
	private final static String SPACE = " ";
	private final static String DATE_FORMAT = "yyyyMMdd HHmmss.SSS";
	private final static String CREATE = "CREATE";
	private final static String UPSERT = "UPSERT";
	private final static String UPDATE = "UPDATE";
	private final static String DELETE = "DELETE";
	private final static String READ = "READ";
	private final static String TRUE = "true";
	private final static String EMPTY_STRING = "";
	private final static String ERROR = "ERROR";
	private final static String WARNING = "WARNING";
	private final static String INFO = "INFO";
	private final static String ADVISORY_NOTIF = "AdvisoryNotification";
	private final static String NOTIFICATION_DOC = "NOTIFICATION_DOC";
	// Setup global objects
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public CreateNotification(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		logger.finest('>>> Script start ' + SCRIPT_NAME);
		int docCount = dataContext.getDataCount()
		logger.fine("In-Document Count=" + docCount)
		Set<String> uniqueValues = [] as Set

		for (int docNo = 0; docNo < docCount; docNo++) {
			Properties props = dataContext.getProperties(docNo)

			// *********** Document related functionality ************
			String msgHash;
			String nsMsg;
			String docType;
			String docbase64;
			String key = props.getProperty(DDP_FWK_DOC_KEY) ?: EMPTY_STRING;
			String val = props.getProperty(DDP_FWK_DOC_VAL) ?: EMPTY_STRING;
			String crud = props.getProperty(DDP_FWK_DOC_CRUD_TYPE) ?: EMPTY_STRING;
			if (key.length() > 0 && (CREATE.equals(crud) || UPSERT.equals(crud) || UPDATE.equals(crud) || DELETE.equals(crud) || READ.equals(crud))) {
				msgHash = String.valueOf((key+val+crud).hashCode());
				logger.fine("notification object hash = " + msgHash);
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG, TRUE, false);
				logger.fine("DPP_FWK_EXEC_SUMMARY_FLAG = true");
			}
			String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
			String level = props.getProperty(DDP_FWK_NS_LEVEL) ?: EMPTY_STRING;
			String code = props.getProperty(DDP_FWK_NS_CLASS) ?: EMPTY_STRING;
			if (ERROR.equals(level)) {
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_ERROR_LEVEL, TRUE, false);
				logger.fine("DPP_FWK_ERROR_LEVEL = true");
			}
			if (WARNING.equals(level)) {
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_WARN_LEVEL, TRUE, false);
				logger.fine("DPP_FWK_WARN_LEVEL = true");
			}
			if (!INFO.equals(level) && !ADVISORY_NOTIF.equals(code)) {
				String doc = props.getProperty(DDP_FWK_NS_DOC);
				if (doc) {
					docType = NOTIFICATION_DOC;
					docbase64 = doc.bytes.encodeBase64().toString();
				}

				nsMsg = props.getProperty(DDP_FWK_NS_MSG) ?: EMPTY_STRING;
				if (nsMsg) {
					nsMsg = nsMsg.replaceAll(MSG_ILLEGAL_CHARS, SPACE);
					props.setProperty(DDP_FWK_NS_MSG, nsMsg);
					logger.fine("DDP_FWK_NS_MSG = " + nsMsg);
				}

				msgHash = level + code + nsMsg.replaceAll(WHITE_SPACES, EMPTY_STRING).hashCode().toString();
				logger.fine("notification object hash = " + msgHash);
			}

			// ******** end of Document related functionality ********

			if (msgHash && uniqueValues.add(msgHash)) {
				JsonBuilder builder = new JsonBuilder();
				builder {
					Auditlogitem([
						{
							'Level' level
							'Id' props.getProperty(DDP_FWK_NS_INTERNAL_ID)
							'Timestamp' ts
							'ProcessCallStack' props.getProperty(DDP_FWK_NS_PROCESSCALLSTACK)
							'Step' "Notification"
							'ErrorClass' code
							'Details' nsMsg
							'DocType' docType
							'DocBase64' docbase64
						}
					])
				}
				dataContext.storeStream(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")), props);
			}
		}
	}
}