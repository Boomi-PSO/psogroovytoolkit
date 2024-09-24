package com.boomi.psotoolkit.core


import com.boomi.execution.ExecutionUtil

import groovy.json.JsonBuilder

class CreateEnvelope extends CoreCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DPP_FWK_TRACKINGID = "DPP_FWK_TrackingId";
	private static final String DPP_FWK_TRACKEDFIRLDS = "DPP_FWK_TrackedFields";
	private static final String DDP_FWK_MSG_DATETIME = "document.dynamic.userdefined.DDP_FWK_MSG_Datetime";
	private static final String DDP_FWK_MSG_ID = "document.dynamic.userdefined.DDP_FWK_MSG_Id";
	private static final String DDP_FWK_MSG_SENDERID = "document.dynamic.userdefined.DDP_FWK_MSG_SenderId";
	private static final String DDP_FWK_MSG_CONTENTTYPE = "document.dynamic.userdefined.DDP_FWK_MSG_ContentType";
	private static final String DDP_FWK_MSG_REQCORRID = "document.dynamic.userdefined.DDP_FWK_MSG_RequestCorrelationId";
	private static final String DDP_FWK_MSG_DATACLASS = "document.dynamic.userdefined.DDP_FWK_MSG_DataClassification";

	public CreateEnvelope(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		Properties props;
		String msgDatetime;
		String msgId;
		String msgSenderId;
		String msgContentType;
		String msgRequestCorrelationId;
		String msgDataClassification;
		String version = "1.0"
		List<String> parts = [];

		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			props = props ?: dataContext.getProperties(i);
			InputStream is = dataContext.getStream(i);
			msgDatetime = msgDatetime ?: props.getProperty(DDP_FWK_MSG_DATETIME);
			msgId = msgId ?: props.getProperty(DDP_FWK_MSG_ID);
			msgSenderId = msgSenderId ?: props.getProperty(DDP_FWK_MSG_SENDERID);
			msgContentType = msgContentType ?: props.getProperty(DDP_FWK_MSG_CONTENTTYPE);
			msgRequestCorrelationId = msgRequestCorrelationId ?: props.getProperty(DDP_FWK_MSG_REQCORRID);
			msgDataClassification = msgDataClassification ?: props.getProperty(DDP_FWK_MSG_DATACLASS);
			parts.add(is.getBytes().encodeBase64().toString());
		}

		def builder = new JsonBuilder();
		def root = builder {
			'EnvelopeHeader' {
				'CreationDateTime' msgDatetime
				'SenderID' msgSenderId
				'MessageID' msgId
				'TrackingId' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKINGID)
				'TrackedFields' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIRLDS)
				'PayloadType' msgContentType
				'Version' version
				'DataClassification' msgDataClassification
			}
			'EnvelopePayload' {
				'Multipart' parts
			}
		}

		dataContext.storeStream(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")), props);
	}
}