package com.arellomobile.android.libs.cache.ormlite.android.apptools;

import android.app.ListActivity;
import android.content.Context;

import com.arellomobile.android.libs.cache.ormlite.support.ConnectionSource;

/**
 * Base class to use for Tab activities in Android.
 * 
 * For more information, see {@link OrmLiteBaseActivity}.
 * 
 * @author kevingalligan
 */
public abstract class OrmLiteBaseListActivity<H extends OrmLiteSqliteOpenHelper> extends ListActivity {

	private H helper;

	/**
	 * Get a helper for this action.
	 */
	public synchronized H getHelper() {
		if (helper == null) {
			helper = getHelperInternal(this);
		}
		return helper;
	}

	/**
	 * Get a connection source for this action.
	 */
	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (helper != null) {
			OpenHelperManager.release();
			helper = null;
		}
	}

	/**
	 * @see OrmLiteBaseActivity#getHelperInternal(Context)
	 */
	protected H getHelperInternal(Context context) {
		@SuppressWarnings("unchecked")
		H newHelper = (H) OpenHelperManager.getHelper(context);
		return newHelper;
	}

	/**
	 * @see OrmLiteBaseActivity#releaseHelper(OrmLiteSqliteOpenHelper)
	 */
	protected void releaseHelper(H helper) {
		if (helper != null) {
			OpenHelperManager.release();
			helper = null;
		}
	}
}
