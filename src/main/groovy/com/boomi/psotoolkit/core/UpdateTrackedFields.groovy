package com.boomi.psotoolkit.core

import com.boomi.BaseCommand
import com.boomi.execution.ExecutionUtil;
//  Input:
//      DDP_FWK_Tracking_Key<postfix>
//      DDP_FWK_Tracking_Val<postfix>
//		DDP_FWK_IgnoreUnpairedKeyVal Do not error when there is not a matching key/val pair, default=false
//
//  Output:
//      DPP_FWK_TrackedFields value updated
//      DDP_FWK_ValidKeyValuePairs true|false
//      DDP_FWK_ErrorMessage error message if invalid key/val pair input
//

class UpdateTrackedFields extends CoreCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_PATH = "document.dynamic.userdefined.";
	private static final String DDP_FWK_TRACKING_KEY = "document.dynamic.userdefined.DDP_FWK_Tracking_Key";
	private static final String DDP_FWK_TRACKING_VAL = "document.dynamic.userdefined.DDP_FWK_Tracking_Val";
	private static final String DDP_FWK_VALIDKEYVALUEPAIRS = "document.dynamic.userdefined.DDP_FWK_ValidKeyValuePairs";
	private static final String DDP_FWK_ERRORMSG = "document.dynamic.userdefined.DDP_FWK_ErrorMessage";
	private static final String KEY = "Key";
	private static final String VAL = "Val";
	private static final String DPP_FWK_TRACKEDFIELDS = "DPP_FWK_TrackedFields";
	private static final String COMMA = ",";
	private static final String HASH = "#";
	private static final String NEW_LINE = "\n";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String DDP_FWK_IGNOREUNPAIREDKEYVAL = "document.dynamic.userdefined.DDP_FWK_IgnoreUnpairedKeyVal";
	// Local Resource Keys
	private static final String ERROR_NO_KEY_VAL_PAIR = "nokeyvalpair.error";
	private static final String ERROR_NO_VAL_FOR_KEY = "missingvalueforkey.error";
	private static final String ERROR_NO_KEY_FOR_VAL = "missingkeyforvalue.error";

	public UpdateTrackedFields(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		String trackedFields = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS);
		// Main document loop
		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			Properties props = dataContext.getProperties(i);

			String ignoreUnpairedKeyValProp = props.getProperty(DDP_FWK_IGNOREUNPAIREDKEYVAL) ?: FALSE;
			boolean ignoreUnpairedKeyVal = TRUE.equals(ignoreUnpairedKeyValProp) ? true : false;

			if (ignoreUnpairedKeyVal) {
				props.setProperty(DDP_FWK_VALIDKEYVALUEPAIRS, TRUE);
				trackedFields = updateTrackedFields(props, trackedFields);
			}
			else if (validKeyValuePairs(props)) {
				trackedFields = updateValidatedTrackedFields(props, trackedFields);
			}
			dataContext.storeStream(dataContext.getStream(i), props);
		}
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, trackedFields, false);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_TRACKEDFIELDS, trackedFields] as Object[]));
	}
	private boolean validKeyValuePairs(Properties props) {
		boolean isValid = true;
		Set valProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_VAL)};
		Set keyProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_KEY)};
		List errorMsgs = [];
		// verify at least one key/val pair
		if (keyProps.isEmpty() && valProps.isEmpty()) {
			getStringResource(ERROR_NO_KEY_VAL_PAIR)
			errorMsgs.add(getStringResource(ERROR_NO_KEY_VAL_PAIR));
		}
		else {
			// verify the key properties have corresponding value properties
			keyProps.each { String keyprop ->
				if (!props.getProperty(keyprop.replace(KEY, VAL))) {
					errorMsgs.add(getStringResource(ERROR_NO_VAL_FOR_KEY, [(keyprop - DDP_PATH), props.getProperty(keyprop)] as Object[]));
				}
			}
			// verify the value properties have corresponding key properties
			valProps.each { String valprop ->
				if (!props.getProperty(valprop.replace(VAL, KEY))) {
					errorMsgs.add(getStringResource(ERROR_NO_KEY_FOR_VAL, [(valprop - DDP_PATH), props.getProperty(valprop)] as Object[]));
				}
			}
		}
		if (errorMsgs.isEmpty()) {
			props.setProperty(DDP_FWK_VALIDKEYVALUEPAIRS, TRUE);
		}
		else {
			isValid = false;
			props.setProperty(DDP_FWK_VALIDKEYVALUEPAIRS, FALSE);
			props.setProperty(DDP_FWK_ERRORMSG, errorMsgs.join(NEW_LINE));
		}
		return isValid;
	}

	private String updateTrackedFields(String key, String val, String currentTrackedFields) {
		String trackedFields = currentTrackedFields;
		String keyval = new StringBuilder(key).append(HASH).append(val).toString().replaceAll(COMMA, '');
		// Add field if not already there
		if (!trackedFields?.contains(keyval)) {
			trackedFields = trackedFields ?
					new StringJoiner(COMMA).add(trackedFields).add(keyval).toString() :
					keyval;
		}
		return trackedFields;
	}

	private String updateValidatedTrackedFields(Properties props, String currentTrackedFields) {
		String trackedFields = currentTrackedFields;
		Set keyProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_KEY)};
		keyProps.each { String keyprop ->
			String valprop = keyprop.replace(KEY, VAL);
			trackedFields = updateTrackedFields(props.getProperty(keyprop), props.getProperty(valprop), trackedFields);
		}
		return trackedFields;
	}

	private String updateTrackedFields(Properties props, String currentTrackedFields) {
		String trackedFields = currentTrackedFields;
		Set keyProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_KEY)};
		keyProps.each { String keyprop ->
			String valprop = keyprop.replace(KEY, VAL);
			String key = props.getProperty(keyprop);
			String val = props.getProperty(valprop);
			if (key && val) {
				trackedFields = updateTrackedFields(key, val, trackedFields);
			}
		}
		return trackedFields;
	}
}
