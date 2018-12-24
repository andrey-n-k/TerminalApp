/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.ar;

/**
 * Position representation for {@link ArView}
 * @author Swift
 */
public class GeoPoint {
	private double latitude;
	private double longitude;

	public GeoPoint() {
	}

	public GeoPoint(double latitude, double longitude) {
		if (latitude > 90 || latitude < -90) throw new IllegalArgumentException("Invalid Latitude");
		if (longitude <= -180 || longitude > 180) throw new IllegalArgumentException("Invalid Longitude");
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		if (latitude > 90 || latitude < -90) throw new IllegalArgumentException("Invalid Latitude");
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		if (longitude <= -180 || longitude > 180) throw new IllegalArgumentException("Invalid Longitude");
		this.longitude = longitude;
	}
}
