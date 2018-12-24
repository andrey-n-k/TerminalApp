/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.log.serveroutput;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Swift
 */
public class ServerSendOutputPool {
	private static ServerSendOutputPool instance = null;

	public static ServerSendOutputPool getInstance() {
		if (instance == null) instance = new ServerSendOutputPool();
		return instance;
	}

	private ServerSendOutputPool() {
	}

	private Map<String, SeverSendOutputStream> pool = new HashMap<String, SeverSendOutputStream>();


	public synchronized SeverSendOutputStream getStream(String packageName, String sendUrl, float logSize) {
		Log.e(getClass().getSimpleName(), "get new server output");
		SeverSendOutputStream result = pool.get(packageName);
		if (result == null || result.isClosed()) {
			Log.e(getClass().getSimpleName(), "generate new object");
			result = new SeverSendOutputStream(packageName, sendUrl, logSize);
			pool.put(packageName, result);
		}
		return result;
	}
}
