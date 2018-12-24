/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Swift
 */
public class FilteredInputStream extends InputStream {
	protected BufferedReader reader;
	protected InputFilter filter;
	protected InputStream is;
	protected byte[] buffer = new byte[0];
	protected int bufferPosition = 0;

	public FilteredInputStream(InputStream input, InputFilter filter) {
		is = input;
		this.reader = new BufferedReader(new InputStreamReader(is));
		this.filter = filter;
	}

	@Override
	public int read() throws IOException {
		String line;
		while (buffer.length <= bufferPosition && (line = reader.readLine()) != null) {
			buffer = (filter.filter(line) + System.getProperty("line.separator")).getBytes();
			bufferPosition = 0;
		}
		if (buffer.length > bufferPosition) {
			int result = buffer[bufferPosition] > 0 ? buffer[bufferPosition] : 256 + buffer[bufferPosition];
			bufferPosition ++;
			return result;
		}
		return -1;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
