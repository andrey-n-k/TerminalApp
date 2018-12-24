/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system.log.serveroutput;

import android.util.Log;
import com.arellomobile.android.libs.utils.TaskRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Swift
 */
public class SeverSendOutputStream extends OutputStream{
	private static final String LOGGER_ADDRESS = "/data/data/%s/log/";
	private static int logCounter = 0;
	private static final String LOG_FILE_NAME = "log%d.log";

	private FileOutputStream fileOutputStream;
	private int counter = 0;
	private boolean sendAfterBR;
	private String internalFileStorage;
	private String lastFileName;
	private String sendUrl;
	private int logSize;
	private boolean closed = false;

	public SeverSendOutputStream(String packageName, String sendUrl, float logSize) {
		internalFileStorage = String.format(LOGGER_ADDRESS, packageName);
		this.sendUrl = sendUrl;
		this.logSize = (int) (logSize * 1024);
		createNewFile();
	}

	private void createNewFile() {
		try {
			File f = new File(internalFileStorage);
			f.mkdirs();

			lastFileName = internalFileStorage + String.format(LOG_FILE_NAME, logCounter);
			while ((f = new File(lastFileName)).exists()) {
				counter = 1;
				sendFile();
				logCounter++;
				lastFileName = internalFileStorage + String.format(LOG_FILE_NAME, logCounter);
			}
			f.createNewFile();
			fileOutputStream = new FileOutputStream(f);
			sendAfterBR = false;
			logCounter++;
			counter = 0;
		} catch (IOException e) {
			// pass
		}
	}

	@Override
	public void write(int i) throws IOException {
		if (fileOutputStream != null){
			counter++;
			fileOutputStream.write(i);
		}

		if (sendAfterBR && ((char)i) == '\n') {
			sendFile();
			createNewFile();
		}

		verifyFileSize();
	}

	private void sendFile() {
		try {
			if (fileOutputStream != null){
				fileOutputStream.flush();
				fileOutputStream.close();
			}
			if (counter == 0) {
				File f = new File(lastFileName);
				f.delete();
				return;
			}
			TaskRunner.getInstance().queueTask(new FileSender(lastFileName, sendUrl));
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "IOException in sendFile:\n" + e);
		}
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		if (!sendAfterBR) {
			if (fileOutputStream != null) {
				counter += buffer.length;
				fileOutputStream.write(buffer);
			}
		} else {
			if (fileOutputStream != null) {
				String s = new String(buffer);
				if (s.indexOf("\n") >= 0) {
					fileOutputStream.write(s.substring(0, s.indexOf("\n") + 1).getBytes());
					sendFile();
					createNewFile();
					s = s.substring(s.indexOf("\n") + 1);
					counter += s.getBytes().length;
					fileOutputStream.write(s.getBytes());
				} else {
					counter += buffer.length;
					fileOutputStream.write(buffer);
				}
			}
		}
		verifyFileSize();
	}

	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		if (!sendAfterBR) {
			if (fileOutputStream != null) {
				counter += count;
				fileOutputStream.write(buffer, offset, count);
			}
		} else {
			if (fileOutputStream != null) {
				String s = new String(buffer, offset, count);
				if (s.indexOf("\n") >= 0) {
					fileOutputStream.write(s.substring(0, s.indexOf("\n") + 1).getBytes());
					sendFile();
					createNewFile();
					s = s.substring(s.indexOf("\n") + 1);
					counter += s.getBytes().length;
					fileOutputStream.write(s.getBytes());
				} else {
					counter += count;
					fileOutputStream.write(buffer, offset, count);
				}
			}
		}
		verifyFileSize();
	}

	private void verifyFileSize() {
		if (counter < logSize) return;
		sendAfterBR = true;
	}

	protected static class FileSender implements Runnable {
		protected String fileName;
		private HttpURLConnection currentConnection;
		private String sendUrl;

		public FileSender(String fileName, String sendUrl) {
			this.fileName = fileName;
			this.sendUrl = sendUrl;
		}

		/**
		 * Checks response code and modify connection if need
		 *
		 * @throws IOException if IO error occurred
		 * @return need to retry
		 */
		private boolean workWithResponseCode() throws IOException {
			int code = currentConnection.getResponseCode();
			if (code == HttpURLConnection.HTTP_OK) return false;

			// if system problem (no valid response code)
			if (code == -1) {
				return true;
			}

			Log.w(getClass().getSimpleName(), "code != HttpURLConnection.HTTP_OK && code != -1, code == " + code);
			
			throw new IOException();
		}

		public void run() {
			try{
				do {

					// Create connection for selected method
					currentConnection = null;
					URL urlObject = new URL(sendUrl);
					currentConnection = (HttpURLConnection) urlObject.openConnection();
					currentConnection.setRequestMethod("POST");
					currentConnection.setDoOutput(true);
					currentConnection.setDoInput(true);
					OutputStream outputStream = currentConnection.getOutputStream();
					InputStream in = new FileInputStream(fileName);
					int count = 0;
					byte[] buff = new byte[4096];
					int len;
					while ((len = in.read(buff)) >= 0){
						outputStream.write(buff,0,len);
						count += len;
					}
					in.close();
					outputStream.close();
					Log.w(getClass().getSimpleName(), "count == " + count);
					Log.w(getClass().getSimpleName(), "file name == " + fileName);
					// check response code and retry if need
				} while (workWithResponseCode());
				System.out.println("Send logs success");
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
				// pass
			} finally {
				close();
				File f = new File(fileName);
				f.delete();
			}

		}

		/**
		 * Close previous connection and streams
		 */
		public void close() {
			if (currentConnection !=  null) {
				currentConnection.disconnect();
				currentConnection = null;
			}
		}

	}

	public void flush() throws IOException {
		fileOutputStream.flush();
	}

	public void close() throws IOException {
		Log.w(getClass().getSimpleName(), "close stream and send file");
		Log.w(getClass().getSimpleName(), "owner == " + SeverSendOutputStream.this);
		closed = true;
		fileOutputStream.close();
		sendFile();
		createNewFile();
	}

	public boolean isClosed() {
		return closed;
	}
}
