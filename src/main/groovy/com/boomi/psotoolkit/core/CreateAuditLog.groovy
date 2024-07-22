package com.boomi.psotoolkit.core

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.boomi.execution.ExecutionUtil;

import groovy.json.JsonBuilder;

/* **************************************************************************
 This Groovy script sets audit log document properties and creates audit log object.
 IN : DDP_FWK_NS_INTERNAL_ID - unique value
 DDP_FWK_LOG_TYPE - EDIRAW_IN, EDIDOC_IN, CDMDOC, ADMDOC_IN, NOTIFICATION_DOC, EDIRAW_OUT, EDIDOC_OUT, ADMDOC_OUT
 DDP_FWK_LOG_MSG - any arbitrary log header text
 DDP_FWK_LOG_DETAILS - any arbitrary log details text
 DDP_FWK_LOG_DOC - log payload (will be base 64 encoded)
 OUT: audit log JSON document "on the flow"
 ************************************************************************** */

class CreateAuditLog {
	// Constants
	private final static  String SCRIPT_NAME = "CreateAuditlog";
	// Global
	private final static String DDP_FWK_LOG_DOC = "document.dynamic.userdefined.DDP_FWK_LOG_DOC";
	private final static String DDP_FWK_LOG_TYPE = "document.dynamic.userdefined.DDP_FWK_LOG_TYPE";
	private final static String DDP_FWK_LOG_MSG = "document.dynamic.userdefined.DDP_FWK_LOG_MSG";
	private final static String DDP_FWK_NS_MSG_HASH = "document.dynamic.userdefined.DDP_FWK_NS_MSG_HASH";
	private final static String DDP_FWK_NS_INTERNAL_ID = "document.dynamic.userdefined.DDP_FWK_NS_INTERNAL_ID";
	private final static String DDP_FWK_LOG_DETAILS = "document.dynamic.userdefined.DDP_FWK_LOG_DETAILS";
	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final static String NOTIFICATION_DOC = "NOTIFICATION_DOC";
	private final static String EMPTY_STRING = "";
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public CreateAuditLog(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		logger.finest('>>> Script start ' + SCRIPT_NAME);
		int docCount = dataContext.getDataCount();
		logger.fine("In-Document Count=" + docCount)
		Set<String> uniqueValues = [] as Set;

		for (int docNo = 0; docNo < docCount; docNo++) {
			final Properties props = dataContext.getProperties(docNo);

			// *********** Document related functionality ************
			String doc = props.getProperty(DDP_FWK_LOG_DOC) ?: EMPTY_STRING;
			String msg = props.getProperty(DDP_FWK_LOG_MSG) ?: EMPTY_STRING;
			String details = props.getProperty(DDP_FWK_LOG_DETAILS) ?: EMPTY_STRING;
			String msgHash = String.valueOf((doc + msg + details).hashCode());
			logger.fine("audit log hash = " + msgHash);
			props.setProperty(DDP_FWK_NS_MSG_HASH, msgHash);
			if (msgHash && uniqueValues.add(msgHash)) {
				String docType;
				String docbase64;
				if (doc) {
					docType = props.getProperty(DDP_FWK_LOG_TYPE);
					docType = docType ?: NOTIFICATION_DOC;
					docbase64 = doc.bytes.encodeBase64().toString();
				}
				String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
				String id = props.getProperty(DDP_FWK_NS_INTERNAL_ID)
				def builder = new JsonBuilder();
				builder {
					Auditlogitem([
						{
							'Level' 'LOG'
							'Id' id
							'Timestamp' ts
							'Step' msg
							'Details' details
							'DocType' docType
							'DocBase64' docbase64
						}
					])
				}
				// ******** end of Document related functionality ********
				dataContext.storeStream(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")), props);
			}
		}
	}
}