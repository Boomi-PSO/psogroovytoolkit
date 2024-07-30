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

class CreateNotification extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_FWK_DOC_KEY = "document.dynamic.userdefined.DDP_FWK_DOC_KEY";
	private static final String DDP_FWK_DOC_VAL = "document.dynamic.userdefined.DDP_FWK_DOC_VAL";
	private static final String DDP_FWK_DOC_CRUD_TYPE = "document.dynamic.userdefined.DDP_FWK_DOC_CRUD_TYPE";
	private static final String DDP_FWK_NS_LEVEL = "document.dynamic.userdefined.DDP_FWK_NS_LEVEL";
	private static final String DDP_FWK_NS_CLASS = "document.dynamic.userdefined.DDP_FWK_NS_CLASS";
	private static final String DDP_FWK_NS_DOC = "document.dynamic.userdefined.DDP_FWK_NS_DOC";
	private static final String DDP_FWK_NS_MSG = "document.dynamic.userdefined.DDP_FWK_NS_MSG";
	private static final String DDP_FWK_NS_INTERNAL_ID = "document.dynamic.userdefined.DDP_FWK_NS_INTERNAL_ID";
	private static final String DDP_FWK_NS_PROCESSCALLSTACK = "document.dynamic.userdefined.DDP_FWK_NS_ProcessCallStack";
	private static final String DPP_FWK_EXEC_SUMMARY_FLAG = "DPP_FWK_EXEC_SUMMARY_FLAG";
	private static final String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	private static final String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";
	private static final String MSG_ILLEGAL_CHARS = "[></?]";
	private static final String WHITE_SPACES = "^\\s+|\\s+\$|\\s+(?=\\s)";
	private static final String SPACE = " ";
	private static final String DATE_FORMAT = "yyyyMMdd HHmmss.SSS";
	private static final String CREATE = "CREATE";
	private static final String UPSERT = "UPSERT";
	private static final String UPDATE = "UPDATE";
	private static final String DELETE = "DELETE";
	private static final String READ = "READ";
	private static final String TRUE = "true";
	private static final String EMPTY_STRING = "";
	private static final String ERROR = "ERROR";
	private static final String WARNING = "WARNING";
	private static final String INFO = "INFO";
	private static final String ADVISORY_NOTIF = "AdvisoryNotification";
	private static final String NOTIFICATION_DOC = "NOTIFICATION_DOC";

	public CreateNotification(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		int docCount = dataContext.getDataCount()
		logger.fine("In-Document Count=" + docCount)
		Set<String> uniqueValues = [] as Set

		for (int docNo = 0; docNo < docCount; docNo++) {
			Properties props = dataContext.getProperties(docNo)
			boolean skipNotification = false;
			String msgHash;
			String nsMsg;
			String docType;
			String docbase64;
			String key = props.getProperty(DDP_FWK_DOC_KEY) ?: EMPTY_STRING;
			String val = props.getProperty(DDP_FWK_DOC_VAL) ?: EMPTY_STRING;
			String level = props.getProperty(DDP_FWK_NS_LEVEL) ?: EMPTY_STRING;
			String crud = props.getProperty(DDP_FWK_DOC_CRUD_TYPE) ?: EMPTY_STRING;
			if (key.length() > 0 && (CREATE.equals(crud) || UPSERT.equals(crud) || UPDATE.equals(crud) || DELETE.equals(crud) || READ.equals(crud))) {
				msgHash = String.valueOf((key+val+crud).hashCode());
				logger.fine("notification object hash = " + msgHash);
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG, TRUE, false);
				logger.fine("DPP_FWK_EXEC_SUMMARY_FLAG = true");
				if(!(WARNING.equals(level) || ERROR.equals(level))) {
					skipNotification = true;
				}
			}
			String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
			String code = props.getProperty(DDP_FWK_NS_CLASS) ?: EMPTY_STRING;
			if (ERROR.equals(level)) {
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_ERROR_LEVEL, TRUE, false);
				logger.fine("DPP_FWK_ERROR_LEVEL = true");
			}
			else if (WARNING.equals(level)) {
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

			if (msgHash && uniqueValues.add(msgHash) && !skipNotification) {
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