/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package  com.arellomobile.android.libs.network.utils.xml;

import  com.arellomobile.android.libs.network.utils.ServerApiException;
import  com.arellomobile.android.libs.network.utils.ServerRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * Arello Mobile<br/>
 * Mobile Framework<br/>
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License<br/>
 * <a href="http://creativecommons.org/licenses/by/3.0">http://creativecommons.org/licenses/by/3.0</a></br>
 * </p>
 *
 * Universal request to get server data in xml format. Use standard DOM parsing model and handle basic exceptions.
 *
 * @author Swift
 */
public abstract class XmlDomRequest<T> extends ServerRequest<T> {
	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	request URL
	 * @param method request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlDomRequest(String url, int method) {
		super(url, method);
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlDomRequest(String url, int method, String contentType) {
		super(url, method, contentType);
	}

	/**
	 * Callback to work with response. Called when input from server gets, and return result of request.<br/>
	 * Used in other classes. Protect to override.
	 * If need to override, use {@link  com.arellomobile.android.libs.network.utils.ServerRequest} instead
	 *
	 * @param content input from server side
	 * @return result of processing request (data object or other information)
	 * @throws  com.arellomobile.android.libs.network.utils.ServerApiException
	 *                             if error in server format of data
	 * @throws java.io.IOException if IOErrors occurred
	 */
	@Override
	public final T processRequest(InputStream content) throws ServerApiException, IOException {
		try {
			// create builder
			DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// parse, convert and return result
			return convertDom(domBuilder.parse(content));
		} catch (ParserConfigurationException e) {
			// Box exception
			throw new ServerApiException(e);
		} catch (SAXException e) {
			// Box exception
			throw new ServerApiException(e);
		}
	}

	/**
	 * Method to convert <b>document object model</b>
	 * 
	 * @param document input dom
	 * @return request result
	 * @throws  com.arellomobile.android.libs.network.utils.ServerApiException if XML dom not valid
	 */
	protected abstract T convertDom(Document document) throws ServerApiException;
}
