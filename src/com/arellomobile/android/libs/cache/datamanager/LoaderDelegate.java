/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.datamanager;

/**
 *
 * Delegate interface for asynchronous loading
 * @author Swift
 */
public interface LoaderDelegate {
	/**
	 * Main method to load data
	 * @return if data has been loaded
	 */
	public boolean load();

	/**
	 * if data can be loaded immediately
	 * @return is data preloaded 
	 */
	public boolean isPreloaded();

	/**
	 * method to abort loading process 
	 */
	public void cancel();
}
