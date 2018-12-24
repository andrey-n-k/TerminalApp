/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package  com.arellomobile.android.libs.network.utils.xml;

import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * Arello Mobile<br/>
 * Mobile Framework<br/>
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License<br/>
 * <a href="http://creativecommons.org/licenses/by/3.0">http://creativecommons.org/licenses/by/3.0</a></br>
 * </p>
 *
 * Base handler to work with {@link  com.arellomobile.android.libs.network.utils.xml.XmlSaxRequest}
 * @author Swift
 */
public class RequestHandler<T> extends DefaultHandler {
	/**
	 * Result field 
	 */
	private T result;

	/**
	 * Method to get parse result
	 * @return parse result  
	 */
	public final T getResult() {
		return result;
	}

	/**
	 * Don't be public. Write access only protected. Protected to override.
	 *
	 * @param result define parse result.
	 */
	protected final void setResult(T result) {
		this.result = result;
	}
}
