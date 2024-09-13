package com.boomi.psotoolkit.core

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;

import com.boomi.execution.ExecutionUtil;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

/**
 * Description : This Groovy script sets dynamic process properties.
 *
 * Input:
 *       documents - "1 to n audit logs items"
 *       DDP_FWK_DocSize - mandatory size in bytes of of each document. 
 *       					Note that supplying this value will increase performance.
 *       					Use "Document Property > Meta Data > Size" to set this value. 
 * Output:
 *       document "1 consolidated document with context header and list of audit items"
 * **************************************************************************
 **/

class CreateAuditLogNotification extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_FWK_DOCSIZE = "document.dynamic.userdefined.DDP_FWK_DocSize";
	private static final String DDP_FWK_SORT_TS = "document.dynamic.userdefined.DDP_FWK_SORT_TS";
	private static final String DDP_FWK_LEVEL = "document.dynamic.userdefined.DDP_FWK_LEVEL";
	private static final String DPP_FWK_DISABLE_AUDIT = "DPP_FWK_DISABLE_AUDIT";
	private static final String DPP_FWK_DISABLE_NOTIFICATION = "DPP_FWK_DISABLE_NOTIFICATION";
	private static final String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	private static final String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";
	private static final String DPP_FWK_AUDITLOG_SIZE_MAX = "DPP_FWK_AUDITLOG_SIZE_MAX";
	private static final String DPP_FWK_TRACKING_ID = "DPP_FWK_TrackingId";
	private static final String DPP_FWK_TRACKED_FIELDS = "DPP_FWK_TrackedFields";
	private static final String DPP_FWK_CONTAINER_ID = "DPP_FWK_ContainerId";
	private static final String DPP_FWK_EXECUTION_ID = "DPP_FWK_ExecutionId";
	private static final String DPP_FWK_APIURL = "DPP_FWK_APIURL";
	private static final String DPP_FWK_PROCESS_NAME = "DPP_FWK_ProcessName";
	private static final String DPP_FWK_PROCESS_ID = "DPP_FWK_ProcessId";
	private static final String DPP_FWK_DIRECTORY = "DPP_FWK_Directory";
	private static final String UTF_8 = "UTF-8";
	private static final String LOG = "LOG";
	private static final String ERROR = 'ERROR';
	private static final String NO = "0";
	private static final String YES = "1";
	private static final String TRUE = "true";
	private static final String DOC_BASE64 = "DocBase64";
	private static final String NOTIFICATION = 'Notification';
	private static final String INTERNAL_ERROR = 'InternalError';
	private static final String TRUNCATED = "TRUNCATED#"
	private static final int DIVISOR = 5;
	private static final String TRUNCATED_DATA_ATTR = "TruncatedData"
	private static final String COMPRESSED_DATA_ATTR = "CompressedData"
	private static final String COMMA = ',';
	// Local Resource Keys
	private static final String WARN_TRACKED_FIELDS = "trackedfieldssize.warning";
	private static final String INFO_ORIG_DOC_SIZE = "originaldocumentsize.info";
	private static final String INFO_COMPRESSED_DOC_SIZE = "compresseddocumentsize.info";
	private static final String INFO_COMPRESSION = "compressionfactor.info";
	private static final String INFO_COMPRESSION_SIZE = "sizewithcompression.info";
	// Setup global objects
	private int auditLogProcessContextSize = 0;
	private int auditLogSizeMax = 9216;
	private String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss.SSS"));

	public CreateAuditLogNotification(def dataContext) {
		super(dataContext);
	}

	public void executeWithoutFilterSortCombine() {
		logScriptName(SCRIPT_NAME);
		// Get max size of audit log
		String auditlogSizeMaxString = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_AUDITLOG_SIZE_MAX);
		auditLogSizeMax = (auditlogSizeMaxString && auditlogSizeMaxString.isInteger()) ? Integer.parseInt(auditlogSizeMaxString) : 9216;
		// Initialise slurper
		JsonSlurper jSlurper = new JsonSlurper();
		// Set up the Process Context Json Header
		def auditlogProcessContext = getAuditlogProcessContext(jSlurper);
		// for each document
		for (int i = 0; i < dataContext.getDataCount(); i++) {
			Properties props = dataContext.getProperties(i);
			// Parse audit log items
			def auditLogItems = jSlurper.parse(dataContext.getStream(i));
			storeAuditLogNotifications(auditlogProcessContext, auditLogItems, getDocSize(props, auditLogItems), props);
		}
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		// Get max size of audit log
		String auditlogSizeMaxString = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_AUDITLOG_SIZE_MAX);
		auditLogSizeMax = (auditlogSizeMaxString && auditlogSizeMaxString.isInteger()) ? Integer.parseInt(auditlogSizeMaxString) : 9216;
		// Initialise slurper
		JsonSlurper jSlurper = new JsonSlurper();
		// Set up the Process Context Json Header
		def auditLogProcessContext = getAuditlogProcessContext(jSlurper);
		// Get sorted and filtered Map of document indexes and combine
		Map filteredSortedDocuments = getfilteredSortedDocuments();
		if (!filteredSortedDocuments.isEmpty()) {
			Tuple combinedAuditLogItems = combineAuditLogs(jSlurper, filteredSortedDocuments);
			storeAuditLogNotifications(auditLogProcessContext, combinedAuditLogItems[0], combinedAuditLogItems[1], combinedAuditLogItems[2]);
		}
	}

	private int getDocSize(Properties props, auditLogItems) {
		int docSize = 0;
		String docSizeProp = props.getProperty(DDP_FWK_DOCSIZE);
		if (docSizeProp) {
			docSize = docSizeProp.toInteger();
		}
		else {
			docSize = getJsonLength(auditLogItems);
		}
		return docSize
	}

	private storeAuditLogNotifications(def auditlogProcessContext, def auditLogItems, int docSize, Properties props) {
		// don't count start/end curly brackets twice, but add the extra comma --> -1 on size
		int auditLogSize = auditLogProcessContextSize + docSize - 1;
		props.setProperty(DDP_FWK_DOCSIZE, auditLogSize.toString())
		// store audit log items with process context
		if (auditLogSize <= auditLogSizeMax) {
			// Do not modify
			storeWithoutCompression(auditlogProcessContext, auditLogItems, props);
		}
		// Else Remove attached document and recalculate size - if exists
		else if ((auditLogSize = removeAttachedDocs(auditLogItems, auditLogSize)) <= auditLogSizeMax) {
			storeWithoutCompression(auditlogProcessContext, auditLogItems, props);
		}
		// Else compress and base64
		else if (!storeWithCompression(auditlogProcessContext, auditLogItems, props)) {
			// Else too big so error
			storeWithError(auditlogProcessContext, auditLogItems, props);
		}
	}

	private Tuple combineAuditLogs(JsonSlurper jSlurper, Map auditLogItems) {
		def combinedAuditLogItems;
		Integer size = 0;
		Properties combinedProps = new Properties();
		auditLogItems.values().each { int index ->
			def auditLogItem = jSlurper.parse(dataContext.getStream(index));
			Properties props = dataContext.getProperties(index);
			combinedProps.putAll(props);
			if (combinedAuditLogItems) {
				combinedAuditLogItems.Auditlogitem.addAll(auditLogItem.Auditlogitem);
				// subtract {"Auditlogitem":[]} from the size
				size += getDocSize(props, auditLogItem) - 19;
			}
			else {
				combinedAuditLogItems = auditLogItem;
				size = getDocSize(props, auditLogItem);
			}
		}
		return new Tuple(combinedAuditLogItems, size, combinedProps);
	}

	private Map getfilteredSortedDocuments() {
		// Setup to output nothing
		boolean fullLog = false;
		boolean auditLog = false;
		String disableAudit = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_AUDIT);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_DISABLE_AUDIT, disableAudit] as Object[]));
		String disableNotify = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DISABLE_NOTIFICATION);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_DISABLE_NOTIFICATION, disableNotify] as Object[]));
		String errorFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_ERROR_LEVEL);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_ERROR_LEVEL, errorFlag] as Object[]));
		String warnFlag = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_WARN_LEVEL);
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, [DPP_FWK_WARN_LEVEL, warnFlag] as Object[]));
		// if both flags are NO (do not disable), then output all
		if (NO.equals(disableAudit) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		// if disableNotify flag is NO (do not disable) and there is at least one ERROR or WARNING then output all
		else if ((TRUE.equals(errorFlag) || TRUE.equals(warnFlag)) && NO.equals(disableNotify)) {
			fullLog = TRUE;
		}
		// if disableAudit flag is NO (do not disable) and disableNotify flag is YES, only output LOG messages
		else if (NO.equals(disableAudit) && YES.equals(disableNotify)) {
			auditLog = TRUE;
		}
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, ["fullLog", fullLog] as Object[]));
		logger.fine(getStringResource(INFO_ONE_VARIABLE_EQUALS, ["auditLog", auditLog] as Object[]));
		// Init temp collections
		SortedMap sortedMap = new TreeMap();
		// Loop through documents and store the sort-by-values and document indices in the sortedMap
		// discard elements if necessary
		for ( int i = 0; i < dataContext.getDataCount(); i++ ) {
			Properties props = dataContext.getProperties(i);
			String level = props.getProperty(DDP_FWK_LEVEL);
			if (fullLog || (auditLog && LOG.equals(level))) {
				String sortByValue = props.getProperty(DDP_FWK_SORT_TS) + i;
				sortedMap.put(sortByValue, i);
			}
		}
		return sortedMap;
	}
	//truncate tracked fields
	private String truncateTrackedFeilds(String trackedFields, int trackedFieldMaxSize) {
		StringBuilder truncatedTrackedFields;
		int lastValidCommaIndex = trackedFields.indexOf(COMMA, trackedFieldMaxSize);
		if (lastValidCommaIndex > 0) {
			truncatedTrackedFields = new StringBuilder(trackedFields.substring(0, lastValidCommaIndex + 1));
			truncatedTrackedFields.append(TRUNCATED).append(trackedFields.length());
		}
		else {
			truncatedTrackedFields = new StringBuilder(TRUNCATED);
			truncatedTrackedFields.append(trackedFields.length());
		}
		logger.warning(getStringResource(WARN_TRACKED_FIELDS, [trackedFields.length(), DIVISOR, auditLogSizeMax] as Object[]));
		return truncatedTrackedFields.toString();
	}
	// return parsed audit log header - ProcessContext
	private def getAuditlogProcessContext(JsonSlurper jSlurper) {
		// Check tracked fields size - this might be too large and cause a StringIndexOutOfBoundsException
		// when truncating the AuditLog document - see PSOTK40
		String trackedFields = ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKED_FIELDS);
		int trackedFieldSize = trackedFields ? trackedFields.length() : 0;
		// Make sure the tracked field size is less than DIVISOR of the audit log max size
		// If not replace the tracked fields with error message
		int trackedFieldMaxSize = auditLogSizeMax / DIVISOR;
		if (trackedFieldSize >= trackedFieldMaxSize) {
			trackedFields = truncateTrackedFeilds(trackedFields, trackedFieldMaxSize);
		}
		JsonBuilder builder = new JsonBuilder();
		builder {
			ProcessContext {
				'TrackingId' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_TRACKING_ID)
				'TrackedFields' trackedFields
				'Container' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_CONTAINER_ID)
				'ExecutionId' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_EXECUTION_ID)
				'API' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_APIURL)
				'MainProcessName' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_PROCESS_NAME)
				'MainProcessComponentId' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_PROCESS_ID)
				'Folder' ExecutionUtil.getDynamicProcessProperty(DPP_FWK_DIRECTORY)
			}
		};
		// convert to string to get length
		String auditlogProcessContextJson = builder.toString();
		auditLogProcessContextSize = auditlogProcessContextJson.length();
		// return parsed json
		return jSlurper.parseText(auditlogProcessContextJson);
	}
	// remove possible base64 docs form audit log
	private int removeAttachedDocs(def auditLogItems, int startSize) {
		// reduce audit log and re-calculate json size
		int endSize = startSize
		auditLogItems.Auditlogitem.each { auditlogitem ->
			String base64Doc = auditlogitem.remove(DOC_BASE64);
			if (base64Doc) {
				// 15 chars for comma, attr name, quotes and semicolon --> ,"DocBase64":"..."
				endSize -= (base64Doc.length() + 15);
			}
		}
		return endSize;
	}
	// Calculate parsed Json size
	private int getJsonLength(def parsedJson) {
		JsonBuilder builder = new JsonBuilder(parsedJson);
		return builder.toString().length();
	}
	// Output Json Builder
	private void storeStreamJsonBuilder(JsonBuilder builder, Properties props) {
		storeStreamJson(builder.toString(), props);
	}
	// Output Json String
	private void storeStreamJson(String json, Properties props) {
		dataContext.storeStream(new ByteArrayInputStream(json.getBytes(UTF_8)), props);
	}

	// Output full audit log
	private void storeWithoutCompression(def processContext, def auditLogItems, Properties props) {
		JsonBuilder builder = new JsonBuilder();
		builder {
			'ProcessContext' processContext.ProcessContext
			'Auditlogitem' auditLogItems.Auditlogitem
		}
		storeStreamJsonBuilder(builder, props);
	}
	// output error
	private void storeWithError(def processContext, def auditLogItems, Properties props) {
		JsonBuilder builder = new JsonBuilder();
		// Create over sized audit log items
		builder {
			'Auditlogitem' auditLogItems.Auditlogitem
		}
		// Truncate to max size - header json and leading curly bracket
		String truncatedData = builder.toString().substring(0, (auditLogSizeMax - auditLogProcessContextSize - 1));
		// add truncate data node
		processContext.ProcessContext.put(TRUNCATED_DATA_ATTR, truncatedData);
		// create error json to store
		builder {
			'ProcessContext' processContext.ProcessContext
			Auditlogitem([
				{
					'Level' ERROR
					'Timestamp' now
					'Step' NOTIFICATION
					'ErrorClass' INTERNAL_ERROR
				}
			])
		};
		storeStreamJsonBuilder(builder, props);
	}
	// zip and base64 encode and embed - output if small enough, otherwise return false
	private boolean storeWithCompression(def processContext, def auditLogItems, Properties props) {
		boolean result = false;
		JsonBuilder builder = new JsonBuilder();
		// build uncompressed json
		builder {
			'ProcessContext' processContext.ProcessContext
			'Auditlogitem' auditLogItems.Auditlogitem
		}
		// compress and base64
		String compressedAuditLog = compressEncode(new ByteArrayInputStream(builder.toString().getBytes(UTF_8)));
		// add compressed data node
		processContext.ProcessContext.put(COMPRESSED_DATA_ATTR, compressedAuditLog);
		// create json with compression
		builder {
			'ProcessContext' processContext.ProcessContext
			Auditlogitem([
				{
					'Level' LOG
					'Timestamp' now
				}
			])
		}
		// only store if small enough
		String json = builder.toString();
		logger.fine(getStringResource(INFO_COMPRESSION_SIZE, [json.length(), auditLogSizeMax] as Object[]));
		if (json.length() <= auditLogSizeMax) {
			storeStreamJson(json, props);
			result = true;
		}
		else {
			// still too long, remove compression
			processContext.ProcessContext.remove(COMPRESSED_DATA_ATTR);
		}
		return result;
	}
	// zip and base64encode
	private String compressEncode(ByteArrayInputStream is) {
		ByteArrayOutputStream input = new ByteArrayOutputStream();
		String base64EncodedOutput;
		try {
			byte[] b = new byte[4096];
			int n = 0;
			while ((n = is.read(b)) != -1) {
				input.write(b, 0, n);
			}
		} finally {
			input.close();
		}
		logger.fine(getStringResource(INFO_ORIG_DOC_SIZE, [input.size()] as Object[]));

		if (input.size() > 0) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
			try {
				deflater.setInput(input.toByteArray());
				deflater.finish();
				byte[] buffer = new byte[1024];
				int count = 0;
				int defSize = 0;
				while (!deflater.finished()) {
					count = deflater.deflate(buffer);
					defSize += count;
					stream.write(buffer, 0, count);
				}
				logger.fine(getStringResource(INFO_COMPRESSED_DOC_SIZE, [defSize] as Object[]));
				logger.fine(getStringResource(INFO_COMPRESSION, [(input.size() / (float) defSize)] as Object[]));
				byte[] outputbytes = new ByteArrayInputStream(stream.toByteArray()).getBytes();
				base64EncodedOutput = Base64.getEncoder().encodeToString(outputbytes);
			} finally {
				stream.close();
				deflater.end();
			}
		}
		return base64EncodedOutput;
	}
}