package com.boomi.psotoolkit.core

import com.boomi.execution.ExecutionManager;
import com.boomi.execution.ExecutionTask;
import com.boomi.execution.ExecutionUtil;

import groovy.json.JsonException;
import groovy.json.JsonSlurper;

/* **************************************************************************
 This Groovy script sets dynamic process properties.
 IN : document "on the flow"
 OUT: DPP_FWK_ProcessName - top level process name
 DPP_FWK_ProcessId   - top level process id
 DPP_FWK_ExecutionId - top level execution id
 DPP_FWK_PROCESS_ERROR - empty string
 DPP_FWK_DISABLE_NOTIFICATION - if not already set then default property
 DPP_FWK_DISABLE_AUDIT - if not already set then default property
 DPP_FWK_ENABLE_ERROR_TERM - if not already set then default property
 DPP_FWK_APIURL - derived from default DPPs
 DPP_FWK_inheader_<postscript> - derived from default DDPs
 DPP_FWK_TF_<key> = <value> - derived from DPP_FWK_TrackedFields
 ------------------------------------------------
 ************************************************************************** */

class SetContext {

	// Constants
	private final static String SCRIPT_NAME = "SetContext";
	// Global
	private final static String DPP_FWK_DISABLE_NOTIFICATION_DEFAULT = "DPP_FWK_DISABLE_NOTIFICATION_DEFAULT";
	private final static String DPP_FWK_DISABLE_NOTIFICATION = "DPP_FWK_DISABLE_NOTIFICATION";
	private final static String DPP_FWK_DISABLE_AUDIT_DEFAULT = "DPP_FWK_DISABLE_AUDIT_DEFAULT";
	private final static String DPP_FWK_DISABLE_AUDIT = "DPP_FWK_DISABLE_AUDIT";
	private final static String DPP_FWK_ENABLE_ERROR_TERM_DEFAULT = "DPP_FWK_ENABLE_ERROR_TERM_DEFAULT";
	private final static String DPP_FWK_ENABLE_ERROR_TERM = "DPP_FWK_ENABLE_ERROR_TERM";
	private final static String DPP_FWK_PROCESSNAME = "DPP_FWK_ProcessName";
	private final static String DPP_FWK_PROCESSID = "DPP_FWK_ProcessId";
	private final static String DPP_FWK_EXECUTIONID = "DPP_FWK_ExecutionId";
	private final static String DPP_FWK_CONTAINERID = "DPP_FWK_ContainerId";
	private final static String DPP_FWK_PROCESS_ERROR = "DPP_FWK_PROCESS_ERROR";
	private final static String DPP_FWK_STARTTIME = "DPP_FWK_StartTime";
	private final static String INMETHOD = "inmethod";
	private final static String INPATH = "inpath";
	private final static String DPP_FWK_APIURL = "DPP_FWK_APIURL";
	private final static String DPP_FWK_TRACKEDFIELDS = "DPP_FWK_TrackedFields";
	private final static String DPP_FWK_TF_ = "DPP_FWK_TF_"
	private final static String DDP_INHEADER = "document.dynamic.userdefined.inheader_";
	private final static String DPP_FWK_INHEADER_ = "DPP_FWK_inheader_";
	private final static String HASH = "#";
	private final static String COMMA = ",";
	private final static String DPP_FWK_TRACKINGID = "DPP_FWK_TrackingId";
	// Local
	private final static String DPP_FWK_TRACKINGID_DEFAULT = "DPP_FWK_TrackingId_Default";
	// Setup global objects
	private JsonSlurper jsonSlurper = new JsonSlurper();
	private def logger = ExecutionUtil.getBaseLogger();
	private int envCount = 0
	private def dataContext;

	public SetContext(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {

		// Start
		logger.finest('>>> Script start ' + SCRIPT_NAME);
		// Start main DPPs
		setMainDynamicProcessProperties();
		// Number of docs
		int docCount = dataContext.getDataCount();
		logger.fine("In-Document Count=" + docCount);
		// Main doc loop
		for (int docNo = 0; docNo < docCount; docNo++) {
			// Get DDPs
			Properties props = dataContext.getProperties(docNo);
			// Set up tracking DPPs if Json Envelope
			setPropertiesIfJsonEnvelope(docNo);
			// Set up possible HTTP Header DPPs
			setHTTPHeaderDynamicProcessProperties(props);
			// Send input doc to output
			dataContext.storeStream(dataContext.getStream(docNo), props);
		}
		// Set Traking ID with default if not already set
		checkOrSetProcessProperty(DPP_FWK_TRACKINGID, DPP_FWK_TRACKINGID_DEFAULT);
	}

	// =================================================
	// -------------------- LOCALS ---------------------
	// =================================================
	// Set Track DPPs from Json Envelope
	private void setPropertiesIfJsonEnvelope(int docNo) {
		// Try to parse Json, jsonDoc will be null if not Json
		def jsonDoc = getJsonDoc(dataContext.getStream(docNo));
		// Extract Tracked info if this is an envelope
		if (jsonDoc?.EnvelopeHeader) {
			if (++envCount > 1) {
				throw new IllegalStateException("Number of detected Message Envelopes = " + envCount + ". Only a single Message Envelope allowed at SET Context.");
			}
			String trackedFields = jsonDoc.EnvelopeHeader.TrackedFields;
			if (trackedFields) {
				setTrackedFields(trackedFields);
			}
			String trackingId = jsonDoc.EnvelopeHeader.TrackingId;
			if (trackingId) {
				ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKINGID, trackingId, false);
			}
		}
	}
	// Get parsed Json, if not Json doc return null
	private def getJsonDoc(InputStream doc) {
		def jsonDoc;
		try {
			jsonDoc = jsonSlurper.parse(doc);
		}
		catch (JsonException je) {
			// eat exception and return null;
		}
		return jsonDoc;
	}
	// Set DPP to default value if it is not already set
	private void checkOrSetProcessProperty(String propName, String defaultPropName) {
		String propVal = ExecutionUtil.getDynamicProcessProperty(propName);
		if (!propVal) {
			propVal = ExecutionUtil.getDynamicProcessProperty(defaultPropName);
			ExecutionUtil.setDynamicProcessProperty(propName, propVal, false);
		}
		logger.fine(propName + "=" + propVal);
	}

	// Setup main DPPs
	private void setMainDynamicProcessProperties() {
		ExecutionTask execTaskCurrent = ExecutionManager.getCurrent();
		String componentIDTopLevel = execTaskCurrent.getTopLevelComponentId();
		String processNameTopLevel = null;
		if (componentIDTopLevel) {
			while (execTaskCurrent && !execTaskCurrent.getComponentId().equals(componentIDTopLevel)) {
				execTaskCurrent = execTaskCurrent.getParent();
			}
			processNameTopLevel = execTaskCurrent.getProcessName();
		}
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_PROCESSNAME, processNameTopLevel, false);
		logger.fine("DPP_FWK_ProcessName=" + processNameTopLevel);

		String processIDTopLevel = execTaskCurrent.getTopLevelProcessId();
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_PROCESSID, processIDTopLevel, false);
		logger.fine("DPP_FWK_ProcessId=" + processIDTopLevel);

		String executionIDTopLevel = execTaskCurrent.getTopLevelExecutionId();
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXECUTIONID, executionIDTopLevel, false);
		logger.fine("DPP_FWK_ExecutionId=" + executionIDTopLevel);

		String containerId = ExecutionUtil.getRuntimeExecutionProperty('NODE_ID');
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_CONTAINERID, containerId, false);
		logger.fine("DPP_FWK_ContainerId=" + containerId);

		checkOrSetProcessProperty(DPP_FWK_DISABLE_NOTIFICATION, DPP_FWK_DISABLE_NOTIFICATION_DEFAULT);
		checkOrSetProcessProperty(DPP_FWK_DISABLE_AUDIT, DPP_FWK_DISABLE_AUDIT_DEFAULT);
		checkOrSetProcessProperty(DPP_FWK_ENABLE_ERROR_TERM, DPP_FWK_ENABLE_ERROR_TERM_DEFAULT);

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_PROCESS_ERROR, null, false);

		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_STARTTIME, execTaskCurrent.getStartTime().toString(), false);
		logger.fine("DPP_FWK_StartTime=" + execTaskCurrent.getStartTime());

		String inmethod = ExecutionUtil.getDynamicProcessProperty(INMETHOD);
		String inpath = ExecutionUtil.getDynamicProcessProperty(INPATH);
		if (inmethod) {
			ExecutionUtil.setDynamicProcessProperty(DPP_FWK_APIURL, inmethod + " " + inpath, false);
			logger.fine("DPP_FWK_APIURL=" + inmethod + " " + inpath);
		}
	}
	// Setup Traked Field DPPs
	private void setTrackedFields(String trackedFields) {
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_TRACKEDFIELDS, trackedFields, false);
		logger.fine("DPP_FWK_TrackedFields=" + trackedFields);
		// set DPP_FWK_TF_...
		StringTokenizer tokenizer = new StringTokenizer(trackedFields, COMMA);
		while (tokenizer.hasMoreElements()) {
			String[] keyval = tokenizer.nextToken().split(HASH);
			String key = DPP_FWK_TF_  + keyval[0].toUpperCase();
			String val = keyval[1];
			ExecutionUtil.setDynamicProcessProperty(key, val, false);
			logger.fine(key + "=" + val);
		}
	}
	// Setup HTTP Header DPPs
	private void setHTTPHeaderDynamicProcessProperties(Properties props) {
		Set keys = props.stringPropertyNames().findAll {
			it.contains(DDP_INHEADER)
		}
		keys.each { String inheaderprop ->
			String dpp_name = inheaderprop.replace(DDP_INHEADER, DPP_FWK_INHEADER_);
			String dpp_val = props.getProperty(inheaderprop);
			ExecutionUtil.setDynamicProcessProperty(dpp_name, dpp_val, false);
			logger.info(dpp_name + ': ' + dpp_val);
		}
	}
}
