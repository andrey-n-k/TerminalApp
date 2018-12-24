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
package  com.arellomobile.android.libs.network.utils.xml;

import  com.arellomobile.android.libs.network.utils.ServerApiException;
import  com.arellomobile.android.libs.network.utils.ServerRequest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Universal request to get server data in xml format. Use standard SAX parsing model and handle basic exceptions.
 * 
 * @author Swift
 */
public abstract class XmlSaxRequest<T> extends ServerRequest<T> {
	/**
	 * Handler to parse server response. Must be constant. Defined in constructor.
	 */
	private RequestHandler<T> handler;

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url	request URL
	 * @param method request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param handler Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlSaxRequest(String url, int method, RequestHandler<T> handler) {
		super(url, method);
		this.handler = handler;
	}

	/**
	 * Constructor with initialize constant fields.
	 *
	 * @param url		 request URL
	 * @param method	  request method. Use constants {@link  com.arellomobile.android.libs.network.utils.ServerRequest#GET} or {@link  com.arellomobile.android.libs.network.utils.ServerRequest#POST}
	 * @param contentType if need set content type for request
	 * @param handler Handler to parse response
	 * @throws IllegalArgumentException if method not
	 */
	protected XmlSaxRequest(String url, int method, String contentType, RequestHandler<T> handler) {
		super(url, method, contentType);
		this.handler = handler;
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
			// Get parser
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			// Parse
			parser.parse(content, handler);
			// Get result
			return handler.getResult();
		} catch (ParserConfigurationException e) {
			// Box exception
			throw new ServerApiException(e);
		} catch (SAXException e) {
			// Box exception
			throw new ServerApiException(e);
		}
	}
}
