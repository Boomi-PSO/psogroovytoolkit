package com.boomi.psotoolkit

import com.boomi.execution.ExecutionUtil

abstract public class BaseCommand {

	private static final String SCRIPT_START = '>>> Script start '
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
}
