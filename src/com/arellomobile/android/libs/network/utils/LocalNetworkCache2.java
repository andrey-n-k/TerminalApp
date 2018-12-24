/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Fixing bug with duplicate cache in memory
 *
 * @author Swift
 */
public interface LocalNetworkCache2 extends LocalNetworkCache {
	/**
	 * Open input stream to store data
	 *
	 * @param id		request id
	 * @param storeDate last update date
	 * @param eTag	  content verifier
	 * @return Output proxy to store server data
	 */
	public OutputStream startStoreData(String id, Date storeDate, String eTag);

	/**
	 * Get content for stored data
	 *
	 * @param id request id
	 * @return binary data
	 */
	public InputStream getDataInput(String id);
}
