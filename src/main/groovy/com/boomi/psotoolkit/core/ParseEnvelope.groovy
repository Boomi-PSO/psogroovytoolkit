package com.boomi.psotoolkit.core


import groovy.json.JsonSlurper
class ParseEnvelope extends CoreCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String UTF8 = "UTF-8";

	public ParseEnvelope(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			Properties props = dataContext.getProperties(i);
			InputStream is = dataContext.getStream(i);

			def envelope = new JsonSlurper().parse(is, UTF8);
			envelope.EnvelopePayload.Multipart.each { String part ->
				dataContext.storeStream(new ByteArrayInputStream(part.decodeBase64()), props);
			}
		}
	}
}