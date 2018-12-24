/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.transformgalery;

import android.graphics.Matrix;
import android.graphics.Point;
import android.widget.Adapter;
import android.widget.BaseAdapter;

/**
 * Interface for specify transformation of {@link Transform2DFrame} childs
 *
 * @author Swift
 */
public interface TransformMap extends Adapter {
	/**
	 * Return transformation for point  [-infinity, infinity]^2
	 * @param index index of view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return matrix of transformation
	 */
	public Matrix getTransformForView(int index, int cx, int cy);

	/**
	 * Alpha value for point  [-infinity, infinity]^2
	 * @param index index of view
	 * @param cx x coordinate of virtual center
	 * @param cy y coordinate of virtual center
	 * @return matrix of transformation
	 */
	public float getAlphaForView(int index, int cx, int cy);

	/**
	 * Returns X Size of virtual center
	 * @return X Size of virtual center
	 */
	public int getXSize();

	/**
	 * Returns Y Size of virtual center
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


	/**
	 * update information about container size
	 * @param width container width
	 * @param height container height
	 */
	public void setContainerSize(int width, int height);

	/**
	 * is need to scroll view in center or send onClick event
	 * @param index index of clicked view
	 * @return is need to scroll view in center or send onClick event
	 */
	public boolean needScroll(int index);
}
