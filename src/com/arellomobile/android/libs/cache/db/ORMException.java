/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

/**
 * @author Swift
 */
public class ORMException extends RuntimeException {
	public ORMException() {
	}

	public ORMException(String message) {
		super(message);
	}

	public ORMException(String message, Throwable cause) {
		super(message, cause);
	}

	public ORMException(Throwable cause) {
		super(cause);
	}
}
