/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils.json;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.ServerRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Universal request to get server data in JSON format. Use standard DOM parsing model and handle basic exceptions.
 *
 * @author Swift
 */
public abstract class JsonDomRequest<T> extends ServerRequest<T> {

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	request URL
	 * @param method request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonDomRequest(String url, int method) {
		super(url, method, "application/json; charset=utf-8");
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonDomRequest(String url, int method, String contentType) {
		super(url, method, contentType);
	}

	/**
	 * Callback to work with response. Called when input from server gets, and return result of request.<br/>
	 * Load all server data. Parse JSON and call one of convert methods.
	 * Don't override. If need, use {@link  com.arellomobile.android.libs.network.utils.ServerRequest} instead
	 *
	 * @param content input from server side
	 * @return result of processing request (data object or other information)
	 * @throws com.arellomobile.android.libs.network.utils.ServerApiException
	 *                             if error in server format of data
	 * @throws java.io.IOException if IOErrors occurred
	 */
	@Override
	public final T processRequest(InputStream content) throws ServerApiException, IOException {
		ByteArrayOutputStream dataCache = new ByteArrayOutputStream();

		// Fully read data
		byte[] buff = new byte[1024];
		int len;
		while ((len = content.read(buff)) >= 0) {
			dataCache.write(buff, 0, len);
		}

		// Close streams
		dataCache.close();

		String jsonString = getStringFromBytes(dataCache);
        if(jsonString== null) {
            jsonString = "";
        }
        jsonString = jsonString.trim();

		// Check for array index out of bounds
		if (jsonString.length() > 0) {
			try {
				// switch type of Json root object
				if (jsonString.startsWith("{")) {
					return convertJson(new JSONObject(jsonString));
				} else {
					return convertJson(new JSONArray(jsonString));
				}
			} catch (JSONException e) {
				// Box exception
				throw new ServerApiException(e);
			}
		}
		throw new ServerApiException("No data returned");
	}


	/**
     * Method that build string from byte array. Default implementation use default encoding.
     * You should overwrite this method and return new String(dataCache.toByteArray(), "encoding")
     * where "encoding" is server response encoding.
     *
     * @param dataCache     byte array to encode
     * @return              encoded string
     * @throws IOException  if encoding not found
     */
    protected String getStringFromBytes(ByteArrayOutputStream dataCache) throws IOException
    {
        return new String(dataCache.toByteArray());
    }

    protected abstract T convertJson(JSONObject obj) throws ServerApiException, JSONException, IOException;

	protected abstract T convertJson(JSONArray obj) throws ServerApiException, JSONException, IOException;
}