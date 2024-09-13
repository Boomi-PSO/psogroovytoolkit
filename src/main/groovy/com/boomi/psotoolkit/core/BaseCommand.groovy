package com.boomi.psotoolkit.core

import com.boomi.execution.ExecutionUtil

import java.text.MessageFormat

abstract public class BaseCommand {

	private static final String SCRIPT_START = '>>> Script start '
	// Global Resource Keys
	protected static final String INFO_ONE_VARIABLE_EQUALS = "onevariableequals.info";
	// Setup global objects
	protected def logger = ExecutionUtil.getBaseLogger()
	protected def dataContext;

	public BaseCommand() {
		super();
	}

	public BaseCommand(def dataContext) {
		this.dataContext = dataContext;
	}

	abstract public void execute();

	protected void logScriptName(String scriptName) {
		logger.finest(SCRIPT_START + scriptName);
	}

	protected static String getStringResource(String key) {
		return MessageUtils.getString(key);
	}

	protected static String getStringResource(String key, Object... params) {
		return MessageUtils.getString(key, params);
	}
}
