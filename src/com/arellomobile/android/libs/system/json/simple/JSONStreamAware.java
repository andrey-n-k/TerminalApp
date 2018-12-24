/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.json.simple;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement this interface.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface JSONStreamAware {
	/**
	 * write JSON string to out.
	 */
	void writeJSONString(Writer out) throws IOException;
}
