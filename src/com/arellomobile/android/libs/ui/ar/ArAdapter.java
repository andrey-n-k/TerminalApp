/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.ar;

import android.location.Location;
import android.widget.Adapter;

/**
 * Extended adapter to use with augmented reality view
 *
 * @author Swift
 */
public interface ArAdapter extends Adapter {

	/**
	 * get position for view with index
	 * @param index object index
	 * @return geo position of object
	 */
	@Override
	GeoPoint getItem(int index);

	/**
	 * notify adapter that GPS position was changed
	 * @param location new position
	 */
	void updateGpsPosition(Location location);

}
