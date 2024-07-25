package com.boomi.psotoolkit.core

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

import groovy.json.JsonSlurper

class CreateEnvelopeTests extends BaseTests {

	String DPP_FWK_TRACKINGID = "DPP_FWK_TrackingId";
	String DPP_FWK_TRACKEDFIRLDS = "DPP_FWK_TrackedFields";
	String DDP_FWK_MSG_DATETIME = "document.dynamic.userdefined.DDP_FWK_MSG_Datetime";
	String DDP_FWK_MSG_ID = "document.dynamic.userdefined.DDP_FWK_MSG_Id";
	String DDP_FWK_MSG_SENDERID = "document.dynamic.userdefined.DDP_FWK_MSG_SenderId";
	String DDP_FWK_MSG_CONTENTTYPE = "document.dynamic.userdefined.DDP_FWK_MSG_ContentType";
	String DDP_FWK_MSG_REQCORRID = "document.dynamic.userdefined.DDP_FWK_MSG_RequestCorrelationId";
	String DDP_FWK_MSG_DATACLASS = "document.dynamic.userdefined.DDP_FWK_MSG_DataClassification";
	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testSuccess() {

		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createenv");

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKINGID, '987654321', false);

		dataContext.getProperties(0).put(DDP_FWK_MSG_ID, '123456789');
		String msgTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		dataContext.getProperties(0).put(DDP_FWK_MSG_DATETIME,  msgTime);
		dataContext.getProperties(1).put(DDP_FWK_MSG_ID, 'AAAAAAAAA');

		new CreateEnvelope(dataContext).execute();

		def envelope = new JsonSlurper().parse(dataContext.getOutStreams()[0])

		assert envelope.EnvelopeHeader.TrackingId == '987654321';
		assert envelope.EnvelopeHeader.MessageID == '123456789';
		assert envelope.EnvelopeHeader.CreationDateTime == msgTime;
		assert dataContext.getStream(0).getText().equals(new String(envelope.EnvelopePayload.Multipart[0].decodeBase64()));
	}
}