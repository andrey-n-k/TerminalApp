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

import android.content.Context;
import com.arellomobile.android.libs.cache.db.ORMException;
import com.arellomobile.android.libs.network.utils.LocalNetworkCache2;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.io.*;
import java.util.Date;

/**
 * Extension for {@link BasicCache} that implements {@link LocalNetworkCache2}
 *
 * @author Swift
 */
public abstract class ExtendedBasicCache extends BasicCache implements LocalNetworkCache2{

	protected ExtendedBasicCache(Context context, Class[] classes) {
		super(context, classes);
	}

	protected ExtendedBasicCache(Context context, Class[] classes, boolean storeNetworkContent) {
		super(context, classes, storeNetworkContent);
	}

	/**
	 * Get content for stored data
	 *
	 * @param id request id
	 * @return binary data
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Override
	public InputStream getDataInput(String id) {
		if (!storeNetworkContent) {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		}

		InputStream in;
		try {
			verifyCacheLocation();
			String folderName = fileStoragePrefix + "/network";
			File inFile = new File(folderName);
			inFile.mkdirs();
			inFile = new File(folderName + "/" + id.hashCode() + NETWORK_DATA_EXTENTION);
			if (inFile.exists()) inFile.delete();
			inFile.createNewFile();
			in = new FileInputStream(inFile);

			return in;
		} catch (IOException ex) {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		}
	}

	/**
	 * Open input stream to store data
	 *
	 * @param id		request id
	 * @param storeDate last update date
	 * @param eTag	  content verifier
	 * @return Output proxy to store server data
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Override
	public OutputStream startStoreData(String id, Date storeDate, String eTag) {
		try {
			networkDb.commit(new NetworkToken(id, storeDate, eTag));
			if (!storeNetworkContent) return new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			};
			OutputStream out = null;
			try {
				verifyCacheLocation();
				String folderName = fileStoragePrefix + "/network";
				File outFile = new File(folderName);
				outFile.mkdirs();
				outFile = new File(folderName + "/" + id.hashCode() + NETWORK_DATA_EXTENTION);
				if (outFile.exists()) outFile.delete();
				outFile.createNewFile();
				return new FileOutputStream(outFile);
			} catch (IOException e) {
				return new OutputStream() {
					@Override
					public void write(int b) throws IOException {
					}
				};
			} finally {
				if (out != null) {
					try{out.close();} catch (IOException e) {/*pass*/}
				}
			}
		} catch (ORMException e) {
			log.severe(LogUtils.getErrorReport(e.getMessage(), e));
			throw new IllegalArgumentException();
		}
	}
}
