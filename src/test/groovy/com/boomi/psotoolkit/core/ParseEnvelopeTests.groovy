package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

import groovy.json.JsonSlurper

class ParseEnvelopeTests extends BaseTests {
	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/extractenvparts");

		new ParseEnvelope(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		def actualJson0 = jsluper.parse(dataContext.getOutStreams()[0]);
		def actualJson1 = jsluper.parse(dataContext.getOutStreams()[1]);

		assert actualJson0 == actualJson1
		assert !dataContext.getOutStreams()[2].getText();
	}
}