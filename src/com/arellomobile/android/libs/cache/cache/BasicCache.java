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

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import com.arellomobile.android.libs.cache.db.DataSource;
import com.arellomobile.android.libs.cache.db.Database;
import com.arellomobile.android.libs.cache.db.ORMException;
import com.arellomobile.android.libs.network.utils.LocalNetworkCache;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.io.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Default cache implementation. Implement basic functionality for work with databases, images and binary data.
 * Also implement functionality of {@link LocalNetworkCache} interfaces
 *
 * @author Swift
 */
public abstract class BasicCache implements LocalNetworkCache {

	static final String APPLICATION_DB_NAME = "application.db";
	static final String NETWORK_DB_NAME = "network.db";
	private String packageName;
	boolean storeNetworkContent = false;

	protected String fileStoragePrefix;

	Database database;
	Database networkDb;


	// For version synchronizing
	static final String VERSION_KEY = "VERSION";
	static final String CACHE_VERSION = "vesion";

	static final String IMAGE_EXTENTION = ".im";
	static final String NETWORK_DATA_EXTENTION = ".nd";
	static final int MAX_RELOAD_TRY = 3;
	static final String DATABASES_ADDRESS_PATTERN = "/data/data/%s/databases/";
	Logger log = Logger.getLogger(getClass().getName());

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	protected void verifyCacheLocation() {
		boolean update;
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			update = !("/data/data/" + packageName).equals(fileStoragePrefix);
			fileStoragePrefix = "/data/data/" + packageName;
		} else {
			update = !Environment.getExternalStorageDirectory().getPath().equals(fileStoragePrefix);
			fileStoragePrefix = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName + "/cache";
		}
		if (update) {
			// TODO: need to add verification
			File f = new File(fileStoragePrefix);
			f.mkdirs();

			f = new File(fileStoragePrefix + "/.nomedia");
			try {
				if (!f.exists()) f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * The same as new BasicCache(context, classes, false);
	 *
	 * @param context link to context for initializing paths and getting version
	 * @param classes lis of database objects
	 * @see BasicCache#BasicCache(android.content.Context, Class[], boolean)
	 */
	protected BasicCache(Context context, Class[] classes) {
		this(context, classes, false);
	}

	/**
	 * Main constructor. Init general parameters, verify version and create database.
	 *
	 * @param context link to context for initializing paths and getting version
	 * @param classes lis of database objects
	 * @param storeNetworkContent is need create datafiles for cashing network requests
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	protected BasicCache(Context context, Class[] classes, boolean storeNetworkContent) {
		this.storeNetworkContent = storeNetworkContent;
		packageName = context.getPackageName();

		verifyVersion(context);

		String internalFileStorage;
		File f;

		try {
			internalFileStorage = String.format(DATABASES_ADDRESS_PATTERN, packageName);
			f = new File(internalFileStorage);
			f.mkdirs();
			this.database = DataSource.getInstance().getDatabase(internalFileStorage + APPLICATION_DB_NAME, classes);
			this.networkDb = DataSource.getInstance().getDatabase(internalFileStorage + NETWORK_DB_NAME, new Class[]{NetworkToken.class});
		} catch (Throwable e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	private boolean verifyVersion(Context context) {
		String version = null;
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(new ComponentName(context, context.getClass()).getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(CACHE_VERSION + packageName, Context.MODE_PRIVATE);
		String s = sharedPreferences.getString(VERSION_KEY, "");
		if (s.equals(version)) return true;
		String internalFileStorage = String.format(DATABASES_ADDRESS_PATTERN, packageName);
		File f = new File(internalFileStorage + APPLICATION_DB_NAME);
		f.delete();
		f = new File(internalFileStorage + NETWORK_DB_NAME);
		f.delete();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(VERSION_KEY, version);
		editor.commit();
		return false;
	}

	/**
	 * returns current instance of database object
	 * @return database for load and store data
	 */
	protected Database getDatabase() {
		return database;
	}

	/**
	 * Store data in cache
	 *
	 * @param id		request id
	 * @param storeDate last update date
	 * @param eTag	  content verifier
	 * @param content   data to store
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	public void storeData(String id, Date storeDate, String eTag, byte[] content) {
		try {
			networkDb.commit(new NetworkToken(id, storeDate, eTag));
			if (!storeNetworkContent) return;
			OutputStream out = null;
			try {
				verifyCacheLocation();
				String folderName = fileStoragePrefix + "/network";
				File outFile = new File(folderName);
				outFile.mkdirs();
				outFile = new File(folderName + "/" + id.hashCode() + NETWORK_DATA_EXTENTION);
				if (outFile.exists()) outFile.delete();
				outFile.createNewFile();
				out = new FileOutputStream(outFile);
				out.write(content);
			} catch (IOException e) {
				// pass
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

	/**
	 * Get last update date for data
	 *
	 * @param id request id
	 * @return last updated date
	 */
	public Date getCacheDate(String id) {
		try {
			NetworkToken networkToken = networkDb.findByPrimaryKey(NetworkToken.class, id);
			return networkToken != null?networkToken.getStoreDate():null;
		} catch (ORMException e) {
			Log.e("CacheImpl", e.getMessage(), e);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Get data identifier
	 *
	 * @param id request id
	 * @return Data identifier
	 */
	public String getETag(String id) {
		try {
			NetworkToken networkToken = networkDb.findByPrimaryKey(NetworkToken.class, id);
			return networkToken != null?networkToken.geteTag():null;
		} catch (ORMException e) {
			Log.e("CacheImpl", e.getMessage(), e);
			throw new IllegalArgumentException();
		}
	}


	/**
	 * Get content for stored data
	 *
	 * @param id request id
	 * @return binary data
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	public byte[] getData(String id) {
		if (!storeNetworkContent) return new byte[0];
		InputStream in = null;
		try {
			verifyCacheLocation();
			String folderName = fileStoragePrefix + "/network";
			File inFile = new File(folderName);
			inFile.mkdirs();
			inFile = new File(folderName + "/" + id.hashCode() + NETWORK_DATA_EXTENTION);
			if (inFile.exists()) inFile.delete();
			inFile.createNewFile();
			in = new FileInputStream(inFile);

			ByteArrayOutputStream dataCache = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int i;
			while ((i = in.read(buffer)) >= 0) {
				dataCache.write(buffer, 0, i);
			}
			return dataCache.toByteArray();
		} catch (IOException ex) {
			return new byte[0];
		} finally {
			if(in != null) {
				try{in.close();}catch (IOException e) {/*pass*/}
			}
		}
	}

	/**
	 * get current cache content, stored by id
	 *
	 * @param subPath path for categorise images
	 * @param id data id
	 * @param hasAlpha is image has alpha chanel (for optimizing memory usage for images without alpha we us RGB565 standard)
	 * @return current cache content, stored by id
	 */
	protected BitmapDrawable getImageData(String subPath, int id, boolean hasAlpha) {
		File image = new File(generateFolderName(subPath, id) + "/" + id + IMAGE_EXTENTION);
		if (!image.exists()) return null;
		try {
			InputStream in = new FileInputStream(image);
			BitmapDrawable result;
			int trys = 0;
			do {
				trys ++;
				result = new BitmapDrawable(in);
				// retry if problem with decoding
			} while((result.getBitmap() == null || result.getBitmap().getWidth() <= 0 || result.getBitmap().getHeight() <= 0) && trys < MAX_RELOAD_TRY);
			if (result.getBitmap() == null || result.getBitmap().getWidth() <= 0 || result.getBitmap().getHeight() <= 0) {
				return result;
			}
			// convert in special format for optimize memory usage
			Bitmap bitmap = Bitmap.createBitmap(result.getBitmap().getWidth(), result.getBitmap().getHeight(), hasAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

			result.setBounds(0, 0, result.getBitmap().getWidth(), result.getBitmap().getHeight());
			result.draw(new Canvas(bitmap));
			result.getBitmap().recycle();

			return new BitmapDrawable(bitmap);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private String generateFolderName(String subPath, int id) {
		verifyCacheLocation();
		String result;
		if (subPath == null || subPath.length() == 0) {
			subPath = "";
		}
		while (subPath.startsWith("/")) subPath = subPath.substring(1);
		while (subPath.endsWith("/")) subPath = subPath.substring(0, subPath.length() - 1);
		if (subPath != null && subPath.length() != 0) {
			subPath = "/" + subPath;
		}
		result = fileStoragePrefix + subPath;
		String idS = id + "";
		while (idS.length() > 2) {
			result += "/" + idS.substring(0,2);
			idS = idS.substring(2);
		}
		return result;
	}

	/**
	 * update image data (remove data if bitmap is null)
	 *
	 * @param subPath path for categorise images
	 * @param id	   data id
	 * @param newValue newValue to append or replace
	 * @throws CacheException if IO or other errors occurs
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	protected void updateImageData(String subPath, int id, BitmapDrawable newValue) throws CacheException {
		OutputStream out = null;
		if (newValue == null || newValue.getBitmap() == null) {
			File outFile = new File(generateFolderName(subPath, id));
			outFile.mkdirs();
			outFile = new File(generateFolderName(subPath, id) + "/" + id + IMAGE_EXTENTION);
			if (outFile.exists()) outFile.delete();
			return;
		}
		try {
			Bitmap bitmap;
			// to avoid formats problems
			bitmap = Bitmap.createBitmap(newValue.getBitmap().getWidth(), newValue.getBitmap().getHeight(), Bitmap.Config.ARGB_8888);
			newValue.setBounds(0, 0, newValue.getBitmap().getWidth(), newValue.getBitmap().getHeight());

			newValue.draw(new Canvas(bitmap));

			File outFile = new File(generateFolderName(subPath, id));
			outFile.mkdirs();
			outFile = new File(generateFolderName(subPath, id) + "/" + id + IMAGE_EXTENTION);
			if (outFile.exists()) outFile.delete();
			outFile.createNewFile();
			out = new FileOutputStream(outFile);

			// store new image
			bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);

			bitmap.recycle();
		} catch (IOException e) {
			throw new CacheException(e);
		} finally {
			if (out != null) {
				try{out.close();} catch (IOException e) {/*pass*/}
			}
		}
	}
}
