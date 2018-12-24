/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db.annotations;

/**
 * Sub annotation for {@link FastSearchable} annotation
 *
 * @author Swift
 */
public @interface AlphabetElement {
	/**
	 * @return name of element to shows on screen
	 */
	String name();
	/**
	 * @return count of elements in db
	 */
	int count();
}
