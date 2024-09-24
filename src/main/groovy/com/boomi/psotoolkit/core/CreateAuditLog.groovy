package com.boomi.psotoolkit.core


import java.security.SecureRandom
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import groovy.json.JsonBuilder;

/* **************************************************************************
 This Groovy script sets audit log document properties and creates audit log object.
 DDP_FWK_LOG_TYPE - EDIRAW_IN, EDIDOC_IN, CDMDOC, ADMDOC_IN, NOTIFICATION_DOC, EDIRAW_OUT, EDIDOC_OUT, ADMDOC_OUT
 DDP_FWK_LOG_MSG - any arbitrary log header text
 DDP_FWK_LOG_DETAILS - any arbitrary log details text
 DDP_FWK_LOG_DOC - log payload (will be base 64 encoded)
 OUT: audit log JSON document "on the flow"
 ************************************************************************** */

class CreateAuditLog extends CoreCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	// Global
	private static final String DDP_FWK_LOG_DOC = "document.dynamic.userdefined.DDP_FWK_LOG_DOC";
	private static final String DDP_FWK_LOG_TYPE = "document.dynamic.userdefined.DDP_FWK_LOG_TYPE";
	private static final String DDP_FWK_LOG_MSG = "document.dynamic.userdefined.DDP_FWK_LOG_MSG";
	private static final String DDP_FWK_NS_MSG_HASH = "document.dynamic.userdefined.DDP_FWK_NS_MSG_HASH";
	private static final String DDP_FWK_LOG_DETAILS = "document.dynamic.userdefined.DDP_FWK_LOG_DETAILS";
	private static final String DATE_FORMAT = "yyyyMMdd HHmmss.SSS";
	private static final String NOTIFICATION_DOC = "NOTIFICATION_DOC";
	private static final String EMPTY_STRING = "";

	public CreateAuditLog(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		Set<String> uniqueValues = [] as Set;

		for (int docNo = 0; docNo < dataContext.getDataCount(); docNo++) {
			final Properties props = dataContext.getProperties(docNo);
			String doc = props.remove(DDP_FWK_LOG_DOC) ?: EMPTY_STRING;
			String msg = props.remove(DDP_FWK_LOG_MSG) ?: EMPTY_STRING;
			String details = props.remove(DDP_FWK_LOG_DETAILS) ?: EMPTY_STRING;
			String msgHash = String.valueOf((doc + msg + details).hashCode());
			logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, ["msgHash", msgHash] as Object[]));
			if (msgHash && uniqueValues.add(msgHash)) {
				props.setProperty(DDP_FWK_NS_MSG_HASH, msgHash);
				String docType;
				String docbase64;
				if (doc) {
					docType = props.remove(DDP_FWK_LOG_TYPE);
					docType = docType ?: NOTIFICATION_DOC;
					docbase64 = doc.bytes.encodeBase64().toString();
				}
				String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
				SecureRandom secureRandom = new SecureRandom();
				Long id = Math.abs(secureRandom.nextLong());
				def builder = new JsonBuilder();
				builder {
					Auditlogitem([
						{
							'Level' 'LOG'
							'Id' id.toString()
							'Timestamp' ts
							'Step' msg
							'Details' details
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