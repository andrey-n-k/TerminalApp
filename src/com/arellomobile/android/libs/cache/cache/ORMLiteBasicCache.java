package com.arellomobile.android.libs.cache.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import com.arellomobile.android.libs.network.utils.LocalNetworkCache;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author GrafNick
 */
public abstract class ORMLiteBasicCache<T extends BasicOrmLiteOpenHelper> implements LocalNetworkCache {

	private String packageName;
	boolean storeNetworkContent = false;

	protected final T databaseHelper;

	protected String fileStoragePrefix;

	protected static final String IMAGE_EXTENTION = ".png";
	static final String NETWORK_DATA_EXTENTION = ".nd";
	static final int MAX_RELOAD_TRY = 3;

	protected Logger log = Logger.getLogger(getClass().getName());

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
	 * @param databaseHelper databaseHelper for ormLite DB
	 * @see ORMLiteBasicCache#ORMLiteBasicCache(android.content.Context, BasicOrmLiteOpenHelper, boolean)
	 */
	protected ORMLiteBasicCache(Context context, T databaseHelper) {
		this(context, databaseHelper, false);
	}

	/**
	 * Main constructor. Init general parameters, verify version and create database.
	 *
	 * @param context link to context for initializing paths and getting version
	 * @param databaseHelper databaseHelper for ormLite DB
	 * @param storeNetworkContent is need create datafiles for cashing network requests
	 */
	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	protected ORMLiteBasicCache(Context context, T databaseHelper, boolean storeNetworkContent) {
		this.storeNetworkContent = storeNetworkContent;
		this.databaseHelper = databaseHelper;
		packageName = context.getPackageName();
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
		synchronized (databaseHelper) {
			try {
				NetworkToken token = databaseHelper.getNetworkTokenDao().queryForId(id);
				if (token != null) databaseHelper.getNetworkTokenDao().update(new NetworkToken(id, storeDate, eTag));
				else databaseHelper.getNetworkTokenDao().create(new NetworkToken(id, storeDate, eTag));

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
			} catch (SQLException e) {
				log.severe(LogUtils.getErrorReport(e.getMessage(), e));
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Get last update date for data
	 *
	 * @param id request id
	 * @return last updated date
	 */
	public Date getCacheDate(String id) {
		synchronized (databaseHelper) {
			try {
				NetworkToken token = databaseHelper.getNetworkTokenDao().queryForId(id);
				return token != null ? token.getStoreDate() : null;
			} catch (SQLException e) {
				log.severe(LogUtils.getErrorReport(e.getMessage(), e));
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Get data identifier
	 *
	 * @param id request id
	 * @return Data identifier
	 */
	public String getETag(String id) {
		synchronized (databaseHelper) {
			try {
				NetworkToken token = databaseHelper.getNetworkTokenDao().queryForId(id);
				return token != null ? token.geteTag() : null;
			} catch (SQLException e) {
				log.severe(LogUtils.getErrorReport(e.getMessage(), e));
				throw new IllegalArgumentException();
			}
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
	
	protected Uri getImageUri(String subPath, int id) {
		File image = new File(generateFolderName(subPath, id) + "/" + id + IMAGE_EXTENTION);
		if (!image.exists()) return null;
		return Uri.parse(image.toURI().toString());
	}

	protected String generateFolderName(String subPath, int id) {
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
		String idS = String.valueOf(id);
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
