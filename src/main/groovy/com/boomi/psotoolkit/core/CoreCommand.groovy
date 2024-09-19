package com.boomi.psotoolkit.core

import com.boomi.BaseCommand;

abstract public class CoreCommand extends BaseCommand {

	// Global Resource Keys
	protected static final String INFO_ONE_VARIABLE_EQUALS = "onevariableequals.info";

	public CoreCommand() {
		super();
	}

	public CoreCommand(def dataContext) {
		super(dataContext);
	}

	protected static String getStringResource(String key) {
		return MessageUtils.getString(key);
	}

	protected static String getStringResource(String key, Object... params) {
		return MessageUtils.getString(key, params);
	}
}
