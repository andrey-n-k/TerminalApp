/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.json.simple.parser;

import java.util.List;
import java.util.Map;

/**
 * Container factory for creating containers for JSON object and JSON array.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 * @see com.arellomobile.android.libs.system.json.simple.parser.JSONParser#parse(java.io.Reader, ContainerFactory)
 */
public interface ContainerFactory {
	/**
	 * @return A Map instance to store JSON object, or null if you want to use com.arellomobile.android.libs.system.json.simple.JSONObject.
	 */
	Map createObjectContainer();

	/**
	 * @return A List instance to store JSON array, or null if you want to use com.arellomobile.android.libs.system.json.simple.JSONArray.
	 */
	List creatArrayContainer();
}
