/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import java.util.logging.Logger;

/**
 * Simple extension of Gallery. Blocks scrolling more then 1 screen.<br/>
 * Previous and next views are required for the correct work of the this component.
 *
 * @author Swift
 */
public class IPhoneStyleGallery extends Gallery {

	private MotionEvent event;
	private boolean move = false;
	private final Logger log = Logger.getLogger(getClass().getName());

	public IPhoneStyleGallery(Context context) {
		super(context);
	}

	public IPhoneStyleGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IPhoneStyleGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * HACK: Prevents drop and gallery offset from being reset when Child View content is changed
	 * {@inheritDoc}
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		log.config("onLayout changed = " + changed);
		// Do not do anything if user is draggin the gallery
		if (move) return;
		super.onLayout(changed, l, t, r, b);	//To change body of overridden methods use File | Settings | File Templates.
	}

	/**
	 * Detects content dragging
	 * {@inheritDoc}
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		log.config("onTouchEvent: " + event);
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			move = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
			move = false;
		}
		if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			return false;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * HACK: sending keyboard message to perform animated scrolling
	 */
	public void next() {
		onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));
		onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_RIGHT));
	}

	/**
	 * HACK: sending keyboard message to perform animated scrolling
	 */
	public void previous() {
		onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
		onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
	}

	/**
	 * HACK: replacing onFling to prev/next
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (velocityX < 0){
			next();
			return true;
		} else if (velocityX > 0){
			previous();
			return true;
		}
		return false;
	}
}
