package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class UpdateTrackedFieldsTests extends BaseTests {
	String DDP_PATH = "document.dynamic.userdefined.";
	String DDP_FWK_TRACKING_KEY = "document.dynamic.userdefined.DDP_FWK_Tracking_Key";
	String DDP_FWK_TRACKING_VAL = "document.dynamic.userdefined.DDP_FWK_Tracking_Val";
	String DPP_FWK_TRACKEDFIELDS = "DPP_FWK_TrackedFields";
	String DDP_FWK_ERRORMSG = "document.dynamic.userdefined.DDP_FWK_ErrorMessage";
	String DDP_FWK_VALIDKEYVALUEPAIRS = "document.dynamic.userdefined.DDP_FWK_ValidKeyValuePairs";
	String DDP_FWK_IGNOREUNPAIREDKEYVAL = "document.dynamic.userdefined.DDP_FWK_IgnoreUnpairedKeyVal";

	def dataContext;

	@BeforeEach
	void setUp() {
		dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/updatetrackedfields");
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testEmptyAtStartAndDup() {

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY, '123456789');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL, 'AAAAAAAAA');

		dataContext.getProperties(1).put(DDP_FWK_TRACKING_KEY, 'abcgofish');
		dataContext.getProperties(1).put(DDP_FWK_TRACKING_VAL, 'Not today');

		dataContext.getProperties(2).put(DDP_FWK_TRACKING_KEY, 'abcgofish');
		dataContext.getProperties(2).put(DDP_FWK_TRACKING_VAL, 'Not today');

		new UpdateTrackedFields(dataContext).execute();

		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == "123456789#AAAAAAAAA,abcgofish#Not today";
		assert "true".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testNotEmptyAtStartDocWithoutTFs() {

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, 'already#here,add#somemore', false);

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY, '123456789');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL, 'AAAAAAAAA');

		dataContext.getProperties(1).put(DDP_FWK_TRACKING_KEY, 'abcgofish');
		dataContext.getProperties(1).put(DDP_FWK_TRACKING_VAL, 'Not today');

		dataContext.getProperties(2).put(DDP_FWK_TRACKING_KEY, 'abcgofish');
		dataContext.getProperties(2).put(DDP_FWK_TRACKING_VAL, 'Not today');

		new UpdateTrackedFields(dataContext).execute();

		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == "already#here,add#somemore,123456789#AAAAAAAAA,abcgofish#Not today";
		assert "true".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testMultipleTFsPerDoc() {

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, 'already#here,add#somemore', false);

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", '12345,6789');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "1", 'AAAA,AAAAA');

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "2", 'abcgofish');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "2", 'Not today');

		dataContext.getProperties(1).put(DDP_FWK_TRACKING_KEY + "1", '12345,6789');
		dataContext.getProperties(1).put(DDP_FWK_TRACKING_VAL + "1", 'AAAA,AAAAA');

		dataContext.getProperties(2).put(DDP_FWK_TRACKING_KEY + "1", '12345,6789');
		dataContext.getProperties(2).put(DDP_FWK_TRACKING_VAL + "1", 'AAAA,AAAAA');

		dataContext.getProperties(2).put(DDP_FWK_TRACKING_KEY + "2", 'abcgofish');
		dataContext.getProperties(2).put(DDP_FWK_TRACKING_VAL + "2", 'Not today');

		new UpdateTrackedFields(dataContext).execute();

		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == "already#here,add#somemore,abcgofish#Not today,123456789#AAAAAAAAA";
		assert "true".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testKeyNoVal() {

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", 'KeyNoVal');

		new UpdateTrackedFields(dataContext).execute();

		assert ("Missing corresponding Value property for Key Property=" + ((DDP_FWK_TRACKING_KEY + "1") + "::KeyNoVal" - DDP_PATH )).equals(dataContext.getOutProperties()[0].get(getDDP_FWK_ERRORMSG()));
	}

	@Test
	void testValNoKey() {

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "1", 'AAAA,AAAAA');

		new UpdateTrackedFields(dataContext).execute();

		assert ("Missing corresponding Key property for Value Property=" + ((DDP_FWK_TRACKING_VAL + "1") + "::AAAA,AAAAA" - DDP_PATH )).equals(dataContext.getOutProperties()[0].get(getDDP_FWK_ERRORMSG()));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[1].get(DDP_FWK_ERRORMSG));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[2].get(DDP_FWK_ERRORMSG));
		assert "false".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testNoKeyVal() {

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", '12345,6789');

		new UpdateTrackedFields(dataContext).execute();

		assert ("Missing corresponding Value property for Key Property=" + ((DDP_FWK_TRACKING_KEY + "1") + "::12345,6789" - DDP_PATH )).equals(dataContext.getOutProperties()[0].get(getDDP_FWK_ERRORMSG()));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[1].get(DDP_FWK_ERRORMSG));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[2].get(DDP_FWK_ERRORMSG));
		assert "false".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testAllKeyPairErrors() {

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, 'already#here,add#somemore', false);

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", '12345,6789');

		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "2", 'Not today');

		new UpdateTrackedFields(dataContext).execute();

		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == "already#here,add#somemore";
		assert ("Missing corresponding Value property for Key Property=" + ((DDP_FWK_TRACKING_KEY + "1") + "::12345,6789" - DDP_PATH ) +
		"\nMissing corresponding Key property for Value Property="  + ((DDP_FWK_TRACKING_VAL + "2") + "::Not today" - DDP_PATH))
		.equals(dataContext.getOutProperties()[0].get(DDP_FWK_ERRORMSG));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[1].get(DDP_FWK_ERRORMSG));
		assert ("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val").equals(dataContext.getOutProperties()[2].get(DDP_FWK_ERRORMSG));
		assert "false".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testNoValidationMakesEmpty() {

		dataContext.getProperties(0).put(DDP_FWK_IGNOREUNPAIREDKEYVAL, 'true');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", 'KeyNoVal');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "2", 'ValNoKey');

		new UpdateTrackedFields(dataContext).execute();

		assert (dataContext.getOutProperties()[0].get(DDP_FWK_ERRORMSG) == null);
		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == null;
		assert "true".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}

	@Test
	void testNoValidation() {

		dataContext.getProperties(0).put(DDP_FWK_IGNOREUNPAIREDKEYVAL, 'true');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_KEY + "1", 'Key1');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "1", 'Val1');
		dataContext.getProperties(0).put(DDP_FWK_TRACKING_VAL + "2", 'ValNoKey');

		new UpdateTrackedFields(dataContext).execute();

		assert (dataContext.getOutProperties()[0].get(DDP_FWK_ERRORMSG) == null);
		assert ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS) == "Key1#Val1";
		assert "true".equals(dataContext.getOutProperties()[0].get(DDP_FWK_VALIDKEYVALUEPAIRS))
	}
}