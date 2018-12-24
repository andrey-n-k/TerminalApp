/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package  com.arellomobile.android.libs.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 * Arello Mobile<br/>
 * Mobile Framework<br/>
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License<br/>
 * <a href="http://creativecommons.org/licenses/by/3.0">http://creativecommons.org/licenses/by/3.0</a></br>
 * </p>
 *
 * Interface to transport layer of application API
 * @author Swift 28.01.2010
 */
public interface INetwork {

	/**
	 * Constant represented POST http request
	 */
	public final int GET = 0;

	/**
	 * Constant represented POST http request
	 */
	public final int POST = 1;

	/**
	 * Open InputStream to server content specified by url, request parameters,and request method. If localCacheDate specified verify if content changed. 
	 *
	 * @param url request url
	 * @param parameters request parameters map
	 * @param method specified request method. Accept one of constant {@link  com.arellomobile.android.libs.network.INetwork#GET} or {@link  com.arellomobile.android.libs.network.INetwork#POST}
	 * @param localCacheDate date of cache previous content. If null no cache supported.
	 * @param eTag data id
	 * @param contentType request content type
	 * @return null if server return NOT_MODIFIED status, and HTTPConnection other way
	 * @throws NetworkException if server return status different from OK and NOT_MODIFIED or IO error occurred
	 */
	public InputStream getInputForRequest(String url, Map<String,String> parameters, int method, Date localCacheDate, String eTag, String contentType) throws NetworkException;

	/**
	 * Open connection to server specified by url, request parameters,and request method. If localCacheDate specified verify if content changed.
	 *
	 * @param url request url
	 * @param parameters request parameters map
	 * @param method specified request method. Accept one of constant {@link  com.arellomobile.android.libs.network.INetwork#GET} or {@link  com.arellomobile.android.libs.network.INetwork#POST}
	 * @param localCacheDate date of cache previous content. If null no cache supported.
	 * @param eTag data id
	 * @param contentType request content type
	 * @return null if server return NOT_MODIFIED status, and HTTPConnection other way
	 * @throws NetworkException if server return status different from OK and NOT_MODIFIED or IO error occurred
	 */
	public HttpURLConnection openConnection(String url, Map<String,String> parameters, int method, Date localCacheDate, String eTag, String contentType) throws NetworkException;

	/**
	 * Close previous connection and streams
	 */
	public void close();

}
