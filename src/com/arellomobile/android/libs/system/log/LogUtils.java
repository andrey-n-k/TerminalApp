/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.log;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * Util class for work with logging
 *
 * @author Swift
 */
public class LogUtils {

	private static final String ERROR_REPORT = "%s \n %s";

	/**
	 * Setups java logging system.<br/>
	 * Use standard format for configuration file.<br/>
	 * <a href="http://download.oracle.com/javase/1.4.2/docs/guide/util/logging/overview.html">http://download.oracle.com/javase/1.4.2/docs/guide/util/logging/overview.html</a>
	 *
	 * @param context context for accessing to resources
	 * @param applicationName name of application to initial start log
	 * @param logFileId id of raw recourse to setup logging
	 */
	public static void setupLogging(Context context, String applicationName, int logFileId){
		try {
			// there are problems with class loaders
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(LogUtils.class.getClassLoader());
			InputStream inputStream = context.getResources().openRawResource(logFileId);
			LogManager.getLogManager().readConfiguration(inputStream);
			inputStream.close();

			// initialize all headers
			inputStream = context.getResources().openRawResource(logFileId);
			Properties properties = new Properties();
			properties.load(inputStream);
			inputStream.close();
			Set<Object> keys = properties.keySet();
			for (Object keyO : keys) {
				String key = (String) keyO;
				if (key!= null && key.endsWith(".handlers")) {
					Logger.getLogger(key.substring(0, key.length() - ".handlers".length())).getHandlers();
				}
			}

			//Log application info
			String version = null;
			String packageName = null;
			try {
				PackageInfo packageInfo = context.getPackageManager().getPackageInfo(new ComponentName(context, context.getClass()).getPackageName(), 0);
				version = packageInfo.versionName;
				packageName = packageInfo.packageName;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			Logger.getLogger("AndroidAppDeveloper").info("Starting Application " + applicationName + " v. " + version + " package: " + packageName);

			Thread.currentThread().setContextClassLoader(loader);
		} catch (Exception e) {
			Log.e("LogUtils", e.getMessage(), e);
		}
	}

	/**
	 * Method for generating string of error report include message and stacktrace of exception
	 * @param message error description
	 * @param e exception
	 * @return generated report
	 */
	public static String getErrorReport(String message, Throwable e) {
		if (message != null && message.length() > 0) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(byteArrayOutputStream));
			return String.format(ERROR_REPORT, message, byteArrayOutputStream.toString());
		} else {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(byteArrayOutputStream));
			return byteArrayOutputStream.toString();
		}


	}

}
