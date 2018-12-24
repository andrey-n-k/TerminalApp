/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.log;

import android.util.Log;

import java.util.logging.*;

/**
 * Handler for standard android logging.
 *
 * @author Swift
 */
public class AndroidHandler extends Handler {
	/**
	 * Holds the formatter for all Android log handlers.
	 */
	private static final Formatter formatter = new Formatter(){
		@Override
		public String format(LogRecord logRecord) {
			return logRecord.getMessage();
		}
	};

	/**
	 * Constructs a new instance of the Android log handler.
	 */
	public AndroidHandler() {
		setFormatter(formatter);
	}

	@Override
	public void close() {
		// No need to close, but must implement abstract method.
	}

	@Override
	public void flush() {
		// No need to flush, but must implement abstract method.
	}

	@Override
	public void publish(LogRecord record) {
		try {
			int level = getAndroidLevel(record.getLevel());
			String tag = record.getLoggerName();

			if (tag == null) {
				// Anonymous logger.
				tag = "null";
			} else {
				// Tags must be <= 23 characters.
				int length = tag.length();
				if (length > 23) {
					// Most loggers use the full class name. Try dropping the
					// package.
					int lastPeriod = tag.lastIndexOf(".");
					if (length - lastPeriod - 1 <= 23) {
						tag = tag.substring(lastPeriod + 1);
					} else {
						// Use last 23 chars.
						tag = tag.substring(tag.length() - 23);
					}
				}
			}

			String message = getFormatter().format(record);
			Log.println(level, tag, message);
		} catch (RuntimeException e) {
			Log.e("AndroidHandler", "Error logging message.", e);
		}
	}

	private static int getAndroidLevel(Level level) {
		int value = level.intValue();
		if (value >= 1000) { // SEVERE
			return Log.ERROR;
		} else if (value >= 900) { // WARNING
			return Log.WARN;
		} else if (value >= 800) { // INFO
			return Log.INFO;
		} else if (value >= 700) { // CONFIG
			return Log.DEBUG;
		} else {
			return Log.VERBOSE;
		}
	}

}

