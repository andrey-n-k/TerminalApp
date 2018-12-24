/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package  com.arellomobile.android.libs.network.utils;

import java.util.Date;

/**
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 *
 * Basic interface for http transport cache
 * @author Swift
 */
public interface LocalNetworkCache {
	/**
	 * Stores data in cache 
	 * @param id request id
	 * @param storeDate last update date
	 * @param eTag content verifier
	 * @param content data to store
	 *
	 */
	public void storeData(String id, Date storeDate, String eTag, byte[] content);

	/**
	 * Get last update date for the data
	 * @param id request id
	 * @return last updated date
	 */
	public Date getCacheDate(String id);

	/**
	 * Get data identifier 
	 * @param id request id
	 * @return Data identifier 
	 */
	public String getETag(String id);

	/**
	 * Get content for stored data
	 * @param id request id
	 * @return binary data
	 */
	public byte[] getData(String id);
}
