package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.psotoolkit.BaseTests

import groovy.json.JsonSlurper

class CreateAuditLogTests extends BaseTests {

	String DDP_FWK_LOG_MSG = "document.dynamic.userdefined.DDP_FWK_LOG_MSG";
	String DDP_FWK_LOG_DOC = "document.dynamic.userdefined.DDP_FWK_LOG_DOC";
	String DDP_FWK_LOG_TYPE = "document.dynamic.userdefined.DDP_FWK_LOG_TYPE";
	String DDP_FWK_NS_INTERNAL_ID = "document.dynamic.userdefined.DDP_FWK_NS_INTERNAL_ID";
	String DDP_FWK_LOG_DETAILS = "document.dynamic.userdefined.DDP_FWK_LOG_DETAILS";

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createenv");

		dataContext.getProperties(0).put(DDP_FWK_LOG_MSG, 'Error');
		dataContext.getProperties(0).put(DDP_FWK_LOG_TYPE, 'json');
		dataContext.getProperties(0).put(DDP_FWK_NS_INTERNAL_ID, '1');
		dataContext.getProperties(0).put(DDP_FWK_LOG_DETAILS, 'Test it');
		dataContext.getProperties(0).put(DDP_FWK_LOG_DOC, '{"DDP_DocumentId":"6825177154989703747","DDP_PreviousEventId":"544"}');

		new CreateAuditLog(dataContext).execute();

		def auditLog = new JsonSlurper().parse(dataContext.getOutStreams()[0])

		assert auditLog.Auditlogitem[0].Level == "LOG";
		assert auditLog.Auditlogitem[0].Id == "1";
		assert auditLog.Auditlogitem[0].Step == "Error";
		assert auditLog.Auditlogitem[0].Details == "Test it";
		assert auditLog.Auditlogitem[0].DocType == "json";
		String docBase64 = auditLog.Auditlogitem[0].DocBase64;
		assert '{"DDP_DocumentId":"6825177154989703747","DDP_PreviousEventId":"544"}'.equals(new String(docBase64.decodeBase64()));
	}

	@Test
	void testSuccessDuplicate() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlog");

		dataContext.getProperties(0).put(DDP_FWK_LOG_MSG, 'Error');
		dataContext.getProperties(0).put(DDP_FWK_LOG_TYPE, 'json');
		dataContext.getProperties(0).put(DDP_FWK_NS_INTERNAL_ID, '1');
		dataContext.getProperties(0).put(DDP_FWK_LOG_DETAILS, 'Test it');
		dataContext.getProperties(0).put(DDP_FWK_LOG_DOC, '{"DDP_DocumentId":"6825177154989703747","DDP_PreviousEventId":"544"}');

		dataContext.getProperties(1).put(DDP_FWK_LOG_MSG, 'Error');
		dataContext.getProperties(1).put(DDP_FWK_LOG_TYPE, 'json');
		dataContext.getProperties(1).put(DDP_FWK_NS_INTERNAL_ID, '2');
		dataContext.getProperties(1).put(DDP_FWK_LOG_DETAILS, 'Test it');
		dataContext.getProperties(1).put(DDP_FWK_LOG_DOC, '{"DDP_DocumentId":"6825177154989703747","DDP_PreviousEventId":"544"}');

		new CreateAuditLog(dataContext).execute();

		def auditLog = new JsonSlurper().parse(dataContext.getOutStreams()[0])

		assert auditLog.Auditlogitem[0].Level == "LOG";
		assert auditLog.Auditlogitem[0].Id == "1";
		assert auditLog.Auditlogitem[0].Step == "Error";
		assert auditLog.Auditlogitem[0].Details == "Test it";
		assert auditLog.Auditlogitem[0].DocType == "json";
		String docBase64 = auditLog.Auditlogitem[0].DocBase64;
		assert '{"DDP_DocumentId":"6825177154989703747","DDP_PreviousEventId":"544"}'.equals(new String(docBase64.decodeBase64()));
	}

	@Test
	void testSuccessNoDDPs() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/any");

		new CreateAuditLog(dataContext).execute();

		def auditLog = new JsonSlurper().parse(dataContext.getOutStreams()[0])

		assert auditLog.Auditlogitem[0].Level == "LOG";
		assert !auditLog.Auditlogitem[0].Id;
		assert !auditLog.Auditlogitem[0].Step;
		assert !auditLog.Auditlogitem[0].Details;
		assert !auditLog.Auditlogitem[0].DocType;
		String docBase64 = auditLog.Auditlogitem[0].DocBase64;
		assert !docBase64;
	}
}