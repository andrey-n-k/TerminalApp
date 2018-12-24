/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.openglgallery;

import android.graphics.Point;
import android.widget.Adapter;

/**
 * @author Swift
 */
public interface TransformMap extends Adapter{
	/**
	 * Return transformation for point  [-infinity, infinity]^2
	 * @param index index of view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return matrix of transformation
	 */
	public float[] getTransformForView(int index, int cx, int cy);

	/**
	 * Return transformation for point  [-infinity, infinity]^2
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @param tx x coordinate of point
	 * @param ty y coordinate of point
	 * @return matrix of transformation
	 */
	public int getViewIndexByPoint(int[] cx, int[] cy, int tx, int ty);

	/**
	 * Alpha value for point  [-infinity, infinity]^2
	 * @param index index of view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return matrix of transformation
	 */
	public float getAlphaForView(int index, int cx, int cy);

	/**
	 * @return X Size of virtual center
	 */
	public int getXSize();

	/**
	 * @return Y Size of virtual center
	 */
	public int getYSize();

	/**
	 * Array of indexes of visible views
	 * @param views old value of visible views
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return matrix of transformation
	 */
	public int[] getVisibleViews(int[] views, int[] cx, int[] cy);

	/**
	 * returns absolute index of nearest to current center view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return row and column of nearest to current center view
	 */
	public int getIndexNear(int cx, int cy);

	/**
	 * returns absolute index of nearest to current center view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return row and column of nearest to current center view
	 */
	public Point getPointNear(int cx, int cy);

	/**
	 * Returns virtual position (x,y) of view pointed to passed index
	 * @param index absolute index of view
	 * @return virtual position (x,y) of view pointed to passed index
	 */
	public Point getPosition(int index);

	/**
	 * Returns virtual position (x,y) of view pointed to passed row and column
	 * @param index row and column indexes for view
	 * @return virtual position (x,y) of view pointed to passed index (row and column)
	 */
	public Point getPositionForPoint(Point index);

	/**
	 * Returns count of views rows
	 * (impotent that {@link #getRowCount()} x {@link #getColumnCount()} == {@link #getCount()})
	 * @return count of views rows
	 */
	public int getRowCount();

	/**
	 * Returns count of views columns
	 * (impotent that {@link #getRowCount()} x {@link #getColumnCount()} == {@link #getCount()})
	 * @return count of views columns
	 */
	public int getColumnCount();
}