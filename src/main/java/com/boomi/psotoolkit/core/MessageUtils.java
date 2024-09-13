package com.boomi.psotoolkit.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageUtils {
    private static final String LOGGER_BUNDLE_NAME = "com.boomi.psotoolkit.core.LoggerMessages";
    private static final ResourceBundle LOGGER_MESSAGES = ResourceBundle.getBundle(LOGGER_BUNDLE_NAME);

	public static String getString(String key) {
        try {
            return LOGGER_MESSAGES.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
	
    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(LOGGER_MESSAGES.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
