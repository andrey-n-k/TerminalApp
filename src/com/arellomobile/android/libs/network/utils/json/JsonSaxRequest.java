/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils.json;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.ServerRequest;
import com.arellomobile.android.libs.system.json.simple.parser.JSONParser;
import com.arellomobile.android.libs.system.json.simple.parser.ParseException;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Universal request to get server data in JSON format. Use SAX style parsing model and handle basic exceptions.
 *
 * @author Swift
 */
public abstract class JsonSaxRequest<T> extends ServerRequest<T> {
	/**
	 * Handler to parse server response. Must be constant. Defined in constructor.
	 */
	private RequestHandler<T> handler;

	/**
	 * internal logger
	 */
	private Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Basic constructor initialize only URL. Method sets to default default (GET)
	 *
	 * @param url	 request URL
	 * @param handler Handler to parse response
	 */
	protected JsonSaxRequest(String url, RequestHandler<T> handler) {
		super(url, POST, "application/json; charset=utf-8");
		this.handler = handler;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	 request URL
	 * @param method  request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param handler Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonSaxRequest(String url, int method, RequestHandler<T> handler) {
		super(url, method, "application/json; charset=utf-8");
		this.handler = handler;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @param handler	 Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected JsonSaxRequest(String url, int method, String contentType, RequestHandler<T> handler) {
		super(url, method, contentType);
		this.handler = handler;
	}

	/**
	 * Callback to work with response. Called when input from server gets, and return result of request.<br/>
	 * Parse JSON with defined handler.
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
		try {
			new JSONParser().parse(new InputStreamReader(content), handler);
		} catch (ParseException e) {
			log.severe(LogUtils.getErrorReport(e.getMessage(), e));
			throw new ServerApiException(e);
		}
		return handler.getResult();
	}
}