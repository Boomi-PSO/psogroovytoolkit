package com.boomi.psotoolkit.core
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

class UpdateTrackedFields {
	// Constants
	private final static String DDP_PATH = "document.dynamic.userdefined.";
	private final static String DDP_FWK_TRACKING_KEY = "document.dynamic.userdefined.DDP_FWK_Tracking_Key";
	private final static String DDP_FWK_TRACKING_VAL = "document.dynamic.userdefined.DDP_FWK_Tracking_Val";
	private final static String DDP_FWK_VALIDKEYVALUEPAIRS = "document.dynamic.userdefined.DDP_FWK_ValidKeyValuePairs";
	private final static String DDP_FWK_ERRORMSG = "document.dynamic.userdefined.DDP_FWK_ErrorMessage";
	private final static String KEY = "Key";
	private final static String VAL = "Val";
	private final static String DPP_FWK_TRACKEDFIELDS = "DPP_FWK_TrackedFields";
	private final static String COMMA = ",";
	private final static String HASH = "#";
	private final static String NEW_LINE = "\n";
	private final static String TRUE = "true";
	private final static String FALSE = "false";
	private final static String DDP_FWK_IGNOREUNPAIREDKEYVAL = "document.dynamic.userdefined.DDP_FWK_IgnoreUnpairedKeyVal";
	// Setup global objects
	private def logger = ExecutionUtil.getBaseLogger();
	private def dataContext;

	public UpdateTrackedFields(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
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

		logger.fine(DPP_FWK_TRACKEDFIELDS + ": " + trackedFields);
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, trackedFields, false);
	}

	boolean validKeyValuePairs(Properties props) {
		boolean isValid = true;
		Set valProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_VAL)};
		Set keyProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_KEY)};
		List errorMsgs = [];
		// verify at least one key/val pair
		if (keyProps.isEmpty() && valProps.isEmpty()) {
			errorMsgs.add("There must be at least one Key/Value pair: DDP_FWK_Tracking_Key/DDP_FWK_Tracking_Val");
		}
		else {
			// verify the key properties have corresponding value properties
			keyProps.each { String keyprop ->
				if (!props.getProperty(keyprop.replace(KEY, VAL))) {
					errorMsgs.add("Missing corresponding Value property for Key Property=" + (keyprop - DDP_PATH) + "::" + props.getProperty(keyprop));
				}
			}
			// verify the value properties have corresponding key properties
			valProps.each { String valprop ->
				if (!props.getProperty(valprop.replace(VAL, KEY))) {
					errorMsgs.add("Missing corresponding Key property for Value Property=" + (valprop - DDP_PATH) + "::" + props.getProperty(valprop));
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

	String updateTrackedFields(String key, String val, String currentTrackedFields) {
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

	String updateValidatedTrackedFields(Properties props, String currentTrackedFields) {
		String trackedFields = currentTrackedFields;
		Set keyProps = props.stringPropertyNames().findAll{it.contains(DDP_FWK_TRACKING_KEY)};
		keyProps.each { String keyprop ->
			String valprop = keyprop.replace(KEY, VAL);
			trackedFields = updateTrackedFields(props.getProperty(keyprop), props.getProperty(valprop), trackedFields);
		}
		return trackedFields;
	}

	String updateTrackedFields(Properties props, String currentTrackedFields) {
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
