/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.network.utils;

import com.arellomobile.android.libs.network.INetwork;
import com.arellomobile.android.libs.network.InvalidStatusException;
import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.NetworkPool;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Basic class to create Server API. Limits simultaneous connection to the connections available in network pool.
 *
 * @author Swift
 */
public abstract class BasicServerApiImpl {

	
	/**
	 * Network pool instance.
	 */
	protected NetworkPool networkPool = NetworkPool.getInstance();
	private final Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Constructor with cache initializer
	 *
	 * @param cache cache
	 */
	protected BasicServerApiImpl(LocalNetworkCache cache) {
		this.cache = cache;
	}

	/**
	 * Basic method to process request and get response<br/>
	 * Basic framework method.
	 *
	 * @param request request implementation
	 * @param filter  filter for input characters
	 * @param <T>     type of returned data
	 * @return result of the request
	 * @throws NetworkException   Errors on transport layer
	 * @throws ServerApiException Error in server response
	 */
	@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
	protected final <T> T processRequest(ServerRequest<T> request, InputFilter filter) throws NetworkException, ServerApiException {
		final INetwork network = networkPool.getNetwork();
		synchronized (network) {
			InputStream inputToProcess = null;
			try {
				log.info("request = " + request.toString());
				HttpURLConnection serverConnection = network.openConnection(request.getUrl(), request.getParameters(), request.getMethod(), null, null, request.getContentType());


				// Save cache parameters
				Date lastModified = new Date(serverConnection.getLastModified());
				String eTag = serverConnection.getHeaderField("Etag");

				if (cache != null && !(cache instanceof LocalNetworkCache2)) {
					InputStream serverInput = serverConnection.getInputStream();
					ByteArrayOutputStream dataCache = new ByteArrayOutputStream();

					// Fully read data
					byte[] buffer = new byte[8192];
					int i;
					while ((i = serverInput.read(buffer)) >= 0) {
						dataCache.write(buffer, 0, i);
					}

					// Close streams
					serverInput.close();
					dataCache.close();

					//store data
					byte[] data = dataCache.toByteArray();

					log.config("response = " + new String(data));

					cache.storeData(request.toString(), lastModified, eTag, data);

					inputToProcess = new ByteArrayInputStream(data);
				} else if (cache instanceof LocalNetworkCache2) {
					InputStream serverInput = serverConnection.getInputStream();
					inputToProcess = new BufferedInputStream(serverInput, 8192);

					inputToProcess = new InputCacheBridge(serverInput, ((LocalNetworkCache2) cache).startStoreData(request.toString(), lastModified, eTag));
				} else {
					InputStream serverInput = serverConnection.getInputStream();
					
					//please see below in the file for details on FlushesInputStream
					inputToProcess = new FlushedInputStream(serverInput);
				}

				// process request
				if (filter != null) {
					inputToProcess = new FilteredInputStream(inputToProcess, filter);
				}
				return request.processRequest(inputToProcess);
			} catch (IOException e) {
				// Box exception
				throw new NetworkException(e);
			} finally {
				// executed every time before exit
				if (inputToProcess != null) {
					try {
						inputToProcess.close();
					} catch (IOException ex) {/*pass*/}
				}
				network.close();
			}
		}
	}

	private LocalNetworkCache cache;

	/**
	 * Method to get server binary data. Use cache if needed.
	 * Basic framework method.
	 *
	 * @param request			request implementation
	 * @param filter			 to filter returned data
	 * @param returnResultAlways is need to return result in no content changed
	 * @return result of request
	 * @throws NetworkException	  if error on transport layer
	 * @throws ServerApiException	if error in server format
	 * @throws IllegalStateException if cache not initialized
	 */
	@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
	protected final <T> T processRequestWithCache(ServerRequest<T> request, InputFilter filter, boolean returnResultAlways) throws NetworkException, ServerApiException {
		if (cache == null) throw new IllegalStateException("Cache is not specified");
		final INetwork network = networkPool.getNetwork();
		InputStream inputToProcess = null;
		synchronized (network) {
			try {
				// Load data and check for the changes
				Date cacheDate = cache.getCacheDate(request.toString());
				String dataIdentifier = cache.getETag(request.toString());
				HttpURLConnection serverConnection;
				serverConnection = network.openConnection(request.getUrl(), request.getParameters(), request.getMethod(), cacheDate, dataIdentifier, request.getContentType());

				// If data is not modified return from cache
				if (serverConnection == null) {
					if (returnResultAlways) {
						if (cache instanceof LocalNetworkCache2) {
							return request.processRequest(((LocalNetworkCache2) cache).getDataInput(request.toString()));
						} else {
							return request.processRequest(new ByteArrayInputStream(cache.getData(request.toString())));
						}
					} else {
						return null;
					}
				}

				// Save cache parameters
				Date lastModified = new Date(serverConnection.getLastModified());
				String eTag = serverConnection.getHeaderField("Etag");

				if (cache instanceof LocalNetworkCache2) {
					InputStream serverInput = serverConnection.getInputStream();
					inputToProcess = new BufferedInputStream(serverInput, 8192);

					inputToProcess = new InputCacheBridge(serverInput, ((LocalNetworkCache2) cache).startStoreData(request.toString(), lastModified, eTag));
				} else {
					InputStream serverInput = serverConnection.getInputStream();
					InputStream bufferedServerInput = new BufferedInputStream(serverInput, 8192);
					ByteArrayOutputStream dataCache = new ByteArrayOutputStream();

					// Fully read data
					byte[] buffer = new byte[8192];
					int i;
					while ((i = bufferedServerInput.read(buffer)) >= 0) {
						dataCache.write(buffer, 0, i);
					}

					// Close streams
					serverInput.close();
					dataCache.close();

					//store data
					byte[] data = dataCache.toByteArray();

					log.config("response = " + new String(data));

					cache.storeData(request.toString(), lastModified, eTag, data);

					inputToProcess = new ByteArrayInputStream(data);
				}

				// process request
				if (filter != null) {
					inputToProcess = new FilteredInputStream(inputToProcess, filter);
				}
				return request.processRequest(inputToProcess);
			} catch (IOException e) {
				// Box exception
				throw new NetworkException(e);
			} finally {
				// executed every time before exit
				if (inputToProcess != null) {
					try {
						inputToProcess.close();
					} catch (IOException ex) {/*pass*/}
				}
				network.close();
			}
		}
	}
	
	//please see http://code.google.com/p/android/issues/detail?id=6066 for details
	protected static class FlushedInputStream extends FilterInputStream {
	    public FlushedInputStream(InputStream inputStream) {
	        super(inputStream);
	    }

	    @Override
	    public long skip(long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        while (totalBytesSkipped < n) {
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            if (bytesSkipped == 0L) {
	                  int oneByte = read();
	                  if (oneByte < 0) {
	                      break;  // we reached EOF
	                  } else {
	                      bytesSkipped = 1; // we read one byte
	                  }
	           }
	            totalBytesSkipped += bytesSkipped;
	        }
	        return totalBytesSkipped;
	    }
	}

	protected static class InputCacheBridge extends InputStream {
		protected InputStream src;
		protected OutputStream dst;
		protected int counter = 0;

		public InputCacheBridge(InputStream src, OutputStream dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public int read(byte[] b) throws IOException {
			int i = src.read(b);
			if (i > 0) {
				counter += i;
				dst.write(b, 0, i);
			}
			return i;
		}

		@Override
		public int read(byte[] b, int offset, int length) throws IOException {
			int i = src.read(b, offset, length);
			if (i > 0) {
				counter += i;
				dst.write(b, offset, i);
			}
			return i;
		}

		@Override
		public void close() throws IOException {
			super.close();
			src.close();
			dst.close();
			Logger.getLogger(getClass().getName()).info(counter + "");
		}

		@Override
		public int read() throws IOException {
			int i = src.read();
			if (i >= 0) {
				counter += 1;
				dst.write(i);
			}
			return i;
		}
	}
}