/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

import com.arellomobile.android.libs.network.INetwork;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 * <p/>
 * Basic class of sever request.
 *
 * @author Swift
 */
public abstract class ServerRequest<T>
{
	/**
	 * Constant of representation GET http request method
	 */
	public static int GET = INetwork.GET;

	/**
	 * Constant of representation POST http request method
	 */
	public static int POST = INetwork.POST;

	/**
	 * HTTP request method. Must be constant in child instances.
	 */
	private int method;

	/**
	 * Request parameters. Must be constant in child instances.
	 */
	private Map<String, String> parameters = new HashMap<String, String>();

	/**
	 * Request url
	 */
	private String url;
	private String contentType = null;

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url    request URL
	 * @param method request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected ServerRequest(String url, int method)
	{
		if (method == GET || method == POST)
		{
			this.method = method;
		}
		else
		{
			throw new IllegalArgumentException("Unknown constant of method. " + method);
		}
		this.url = url;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url         request URL
	 * @param method      request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected ServerRequest(String url, int method, String contentType)
	{
		if (method == GET || method == POST)
		{
			this.method = method;
		}
		else
		{
			throw new IllegalArgumentException("Unknown constant of method. " + method);
		}
		this.contentType = contentType;
		this.url = url;
	}

	/**
	 * Gets http method of current request.<br/>
	 * Return one of constant {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}<br/>
	 *
	 * @return current url
	 */
	public final int getMethod()
	{
		return method;
	}

	/**
	 * Gets URL of current request<br/>
	 *
	 * @return current url
	 */
	public final String getUrl()
	{
		return url;
	}

	/**
	 * Remove all parameters.<br/>
	 */
	public final void clearParameters()
	{
		parameters.clear();
	}

	/**
	 * Add new parameter in to request. Override if already exists.
	 * Append parameter with empty string is used to send data without key and url encoding
	 *
	 * @param name  new parameter name
	 * @param value new parameter value
	 */
	public final void appendParameter(String name, String value)
	{
		parameters.put(name, value);
	}

	/**
	 * Get copy of current parameters map.
	 *
	 * @return copy of current parameters map
	 */
	public final Map<String, String> getParameters()
	{
		Map<String, String> result = new HashMap<String, String>();
		result.putAll(parameters);
		return result;
	}

	/**
	 * Callback to work with response. Called when received input from server and return result of the request
	 *
	 * @param content input from the server side
	 * @return result of processing request (data object or other information)
	 * @throws ServerApiException if error in server format of data
	 * @throws IOException        if IOErrors occurred
	 */
	public abstract T processRequest(InputStream content) throws ServerApiException, IOException;

	/**
	 * Create representation of the current request. Unique for all different request
	 *
	 * @return representation string
	 */
	@Override
	public String toString()
	{
		String reqParameters = "";
		Set<String> keys = parameters.keySet();
		for (String key : keys)
		{
			reqParameters += key + "=" + parameters.get(key) + "&";
		}
		if (method == GET)
		{
			return "GET: " + url + "?" + reqParameters;
		}
		else if (method == POST)
		{
			return "POST: " + url + "?" + reqParameters;
		}
		return "";
	}

	public String getContentType()
	{
		return contentType;
	}
}