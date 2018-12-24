/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

import android.graphics.drawable.BitmapDrawable;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author Swift
 */
public class ImageRequest extends ServerRequest<BitmapDrawable> {
	private final Logger log = Logger.getLogger(getClass().getName());

	public ImageRequest(String url) {
		super(url, GET);
	}

	@Override
	public BitmapDrawable processRequest(InputStream content) throws ServerApiException, IOException {
		BitmapDrawable bitmapDrawable;
		try {
			bitmapDrawable = new BitmapDrawable(content);
		} catch (Throwable e) {
			log.severe(LogUtils.getErrorReport(e.getMessage(), e));
			System.gc();
			bitmapDrawable = new BitmapDrawable();
		}
		log.config( "BitmapDrawable create " + getUrl());
		if (bitmapDrawable.getBitmap() != null) {
			log.config( "BitmapDrawable " + bitmapDrawable.getBitmap().getWidth() + "x" + bitmapDrawable.getBitmap().getHeight());
			log.config( "BitmapDrawable " + bitmapDrawable.getBitmap().getConfig());
		}
		return bitmapDrawable;
	}
}
