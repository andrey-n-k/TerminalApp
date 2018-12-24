/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.datamanager;

import android.app.Activity;
import android.view.View;
import com.arellomobile.android.libs.utils.TaskRunner;

import java.util.logging.Logger;

/**
 * Class for multithreaded loading. Verify if the data was loaded and load asynchronously if not.
 * @author Swift
 */
public class AsyncLoader implements Runnable {

	public interface AsyncLoaderCallback {
		public void loadingFinished(LoaderDelegate delegate);
		public void loadingCanceled(LoaderDelegate delegate);
	}

	private final Logger log = Logger.getLogger(getClass().getName());

	/**
	 * Flag to synchronize working with controls fields {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#dataView} and {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#progress}
	 */
	protected boolean workWithControls = false;

	/**
	 * Callback for GUI fields
	 */
	protected Activity context;

	/**
	 * Control that shows that loading is in process
	 */
	protected View progress;
	/**
	 * Control to represent loaded data
	 */
	protected View dataView;

	/**
	 * Delegate object for loading data
	 */
	protected AsyncLoaderCallback callback;

	/**
	 * Callback
	 */
	protected LoaderDelegate delegate;
	
	/**
	 * Flag representing cancel action
	 */
	protected boolean cancel = false;

	/**
	 * Flag representing finish loading
	 */
	protected boolean finish = false;

	/**
	 * Creates loader, checks for preloaded data, runs new thread if data is not preloaded otherwise loads data immediately
	 * Before run asynchronous load hides {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#dataView} and shows {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#progress}
	 *
	 * @param context {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#context} value
	 * @param progress {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#progress} value
	 * @param dataView {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#dataView} value
	 * @param delegate {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#delegate} value
	 */
	public AsyncLoader(Activity context, View progress, View dataView, LoaderDelegate delegate) {
		this(context, progress, dataView, delegate, null);
	}

	/**
	 * Creates loader, checks for preloaded data, runs new thread if data is not preloaded otherwise loads data immediately
	 * Before run asynchronous load hides {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#dataView} and shows {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#progress}
	 *
	 * @param context {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#context} value
	 * @param progress {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#progress} value
	 * @param dataView {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#dataView} value
	 * @param delegate {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#delegate} value
	 * @param callback {@link com.arellomobile.android.libs.cache.datamanager.AsyncLoader#callback} value
	 */
	public AsyncLoader(Activity context, View progress, View dataView, LoaderDelegate delegate, AsyncLoaderCallback callback) {

		// init values
		this.context = context;
		this.progress = progress;
		this.dataView = dataView;
		this.delegate = delegate;
		this.callback = callback;

		// if data is preloaded don't load asynchronously 
		if (delegate.isPreloaded()) {
			run();
		} else {
			// hide data field and show loading progress
			workWithControls = true;
			context.runOnUiThread(new Runnable() {
				public void run() {
					if (AsyncLoader.this.progress != null) {
						AsyncLoader.this.progress.setVisibility(View.VISIBLE);
					}
					if (AsyncLoader.this.dataView != null) {
						AsyncLoader.this.dataView.setVisibility(View.GONE);
					}
					workWithControls = false;
				}
			});

			// start update
			TaskRunner.getInstance().queueTask(this);
		}
	}

	/**
	 * implementation of method {@link Runnable#run()}
	 * Calls delegate, hides progress and shows data if loading succeeed, release objects
	 */
	public void run() {
		try {
			//call delegate to load resource
			if (cancel) return;
			if (delegate.load()) {
				if (cancel) return;
				workWithControls = true;

				//check the result and update UI
				context.runOnUiThread(new Runnable() {
					public void run() {
						if (AsyncLoader.this.progress != null) {
							AsyncLoader.this.progress.setVisibility(View.GONE);
						}
						if (AsyncLoader.this.dataView != null) {
							AsyncLoader.this.dataView.setVisibility(View.VISIBLE);
						}
						workWithControls = false;
					}
				});
			} else {
				log.warning("delegate.load() = " + false);
			}
		} finally {
			// wait for the controls to be freed
			while (workWithControls) {
				try { Thread.sleep(100); } catch (InterruptedException e) {/*pass*/}
			}
			finish = true;
			if (callback != null) callback.loadingFinished(delegate);
			// free resources
			delegate = null;
			context = null;
			dataView = null;
			progress = null;
			callback = null;
		}
	}

	/**
	 * Cancel loading process
	 */
	public void cancel() {
		cancel = true;
		// call delegate 
		if (delegate != null){
			delegate.cancel();
		}
		if (callback != null) callback.loadingCanceled(delegate);
		// free resources
		delegate = null;
		context = null;
		dataView = null;
		progress = null;
		callback = null;
	}

	/**
	 *
	 * @return true if task is canceled
	 */
	public boolean isCancel() {
		return cancel;
	}

	/**
	 *
	 * @return true if task is finished
	 */
	public boolean isFinish() {
		return finish;
	}
}
