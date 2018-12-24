/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.log.serveroutput;

import android.util.Log;
import com.arellomobile.android.libs.system.log.serveroutput.SeverSendOutputStream;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Handler for send logs on server. It use 3 additional parameters:
 * <ul>
 * <li><i>.package</i> name of application package (used to generate folder with temporary files)
 * <li><i>.url</i> url for send logs
 * <li><i>.size</i> max size of log for add file in logger queue
 * </ul>
 * @author Swift
 */
public class SeverSendHandler extends StreamHandler {
	private SeverSendOutputStream stream;

	public SeverSendHandler() {
		Log.e(getClass().getSimpleName(), "SeverSendHandler create");
		try {
			String cName = getClass().getName();
			String packageName = LogManager.getLogManager().getProperty(cName + ".package");
			String sendUrl = LogManager.getLogManager().getProperty(cName + ".url");
			String logSize = LogManager.getLogManager().getProperty(cName + ".size");
			if (packageName != null && packageName.length() != 0 && sendUrl != null && sendUrl.length() != 0) {

				float logSizeFloat = 500.0f;
				try {
					logSizeFloat = logSize != null && logSize.length() != 0 ? Float.parseFloat(logSize) : 500.0f;
				} catch (NumberFormatException e) {
					// pass
				}
				stream = ServerSendOutputPool.getInstance().getStream(packageName, sendUrl, logSizeFloat);
				setOutputStream(stream);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
