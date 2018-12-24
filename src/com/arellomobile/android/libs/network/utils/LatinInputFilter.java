/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

/**
 * @author Swift
 */
public class LatinInputFilter implements InputFilter {
	public String filter(String input) {
		StringBuffer out = new StringBuffer();
		char current;

		if (input == null || ("".equals(input))) return "";
		for (int i = 0; i < input.length(); i++) {
			current = input.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || // white spaces
				((current >= 0x20) && (current <= 0xD7FF)) ||
				((current >= 0xE000) && (current <= 0xFFFD)) ||
				((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}
}
