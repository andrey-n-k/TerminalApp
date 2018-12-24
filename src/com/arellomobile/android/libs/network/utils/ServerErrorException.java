/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

/**
 * @author Grafeev
 */
public class ServerErrorException extends ServerApiException {

	public ServerErrorException() {
	}

	public ServerErrorException(String message) {
		super(message);
	}

	public ServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerErrorException(Throwable cause) {
		super(cause);
	}

}
