/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils.json;

import com.arellomobile.android.libs.system.json.simple.parser.ContentHandler;
import com.arellomobile.android.libs.system.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Base handler to work with {@link  JsonSaxRequest}
 *
 * @author Swift
 */
public class RequestHandler<T> implements ContentHandler {
	/**
	 * Result field
	 */
	private T result;

	/**
	 * Method to get parse result
	 *
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

	/**
	 * Receive notification of the beginning of JSON processing.
	 * The parser will invoke this method only once.
	 *
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 */
	@Override
	public void startJSON() throws ParseException, IOException {
	}

	/**
	 * Receive notification of the end of JSON processing.
	 *
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 */
	@Override
	public void endJSON() throws ParseException, IOException {
	}

	/**
	 * Receive notification of the beginning of a JSON object.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *          - JSONParser will stop and throw the same exception to the caller when receiving this exception.
	 * @see #endJSON
	 */
	@Override
	public boolean startObject() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of a JSON object.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 * @see #startObject
	 */
	@Override
	public boolean endObject() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the beginning of a JSON object entry.
	 *
	 * @param key - Key of a JSON object entry.
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 * @see #endObjectEntry
	 */
	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of the value of previous object entry.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 * @see #startObjectEntry
	 */
	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the beginning of a JSON array.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 * @see #endArray
	 */
	@Override
	public boolean startArray() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the end of a JSON array.
	 *
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 * @see #startArray
	 */
	@Override
	public boolean endArray() throws ParseException, IOException {
		return true;
	}

	/**
	 * Receive notification of the JSON primitive values:
	 * java.lang.String,
	 * java.lang.Number,
	 * java.lang.Boolean
	 * null
	 *
	 * @param value - Instance of the following:
	 *              java.lang.String,
	 *              java.lang.Number,
	 *              java.lang.Boolean
	 *              null
	 * @return false if the handler wants to stop parsing after return.
	 * @throws com.arellomobile.android.libs.system.json.simple.parser.ParseException
	 *
	 */
	@Override
	public boolean primitive(Object value) throws ParseException, IOException {
		return true;
	}
}
