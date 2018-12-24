/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.cache;

/**
 * @author Swift
 */
public class CacheException extends Exception{
	public CacheException() {
	}

	public CacheException(String detailMessage) {
		super(detailMessage);
	}

	public CacheException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public CacheException(Throwable throwable) {
		super(throwable);
	}
}
