/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

import java.util.logging.Logger;


/**
 * View with zooming ability
 * @author Swift
 */
public final class ZoomView extends ViewGroup {

	/**
	 * zoom initialization algorithm ("as is", "scale inside" or "scale and crop")
	 */
	public static enum ZoomViewInitialMode {
		asIs, scaleInside, scaleCrop
	}

	/**
	 * Anchor for zooming. in rect [0,1]x[0,1]
	 */
	private PointF anchorPointParent = new PointF(0.5f, 0.5f);

	/**
	 * Anchor for child view for zooming, in rect [0,1]x[0,1]
	 */
	private PointF anchorPointChild = new PointF(0, 0);

	/**
	 * undefined zoom
 	 */
	private static final int UNSPECIFIED = -1;

	/**
	 * current zoom
	 */
	private double zoom = UNSPECIFIED;

	/**
	 * min zoom
	 */
	private double minZoom;

	/**
	 * max zoom
	 */
	private double maxZoom = 5;

	/**
	 * zoom levels
	 */
	private int zomLevels = 20;

	/**
	 * zoom step
	 */
	private double zoomSize = 2;

	private ZoomViewInitialMode mode = ZoomViewInitialMode.scaleInside;

	private ZoomControls zoomControls;
	private TwoDirectionsScrollView panel;

	/**
	 * Initial size of child view by width
	 */
	private int childWidth;

	/**
	 * Initial size of child view by height
	 */
	private int childHeight;
	private final Logger log = Logger.getLogger(getClass().getName());

	public ZoomView(Context context) {
		super(context);
		setup(context);
	}

	public ZoomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}

	public ZoomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}

	/**
	 * setup initial zoom mode
	 * @param mode mode used for calculating zoom after call {@link #dropZoom()}
	 */
	public void setMode(ZoomViewInitialMode mode) {
		this.mode = mode;
	}

	/**
	 * shows zooming buttons
	 */
	public void showZoomButtons(){
		zoomControls.show();
	}

	/**
	 * hides zooming buttons
	 */
	public void hideZoomButtons(){
		zoomControls.hide();
	}

	/**
	 * indicate if zooming buttons is visible
	 * @return current state of zooming buttons
	 */
	public boolean isZoomButtonsVisible() {
		return zoomControls.getVisibility() == View.VISIBLE;
	}


	/**
	 * Drops current zoom state
	 */
	public void dropZoom() {
		this.setZoom(-1);
		requestLayout();
		// panel will not change layout if bounds not changed, force it to layout
		panel.requestLayout();
	}

	@Override
	public void addView(View child) {
		if (child == panel || child == zoomControls) {
			super.addView(child);
			return;
		}
		panel.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (child == panel || child == zoomControls) {
			super.addView(child, index);
			return;
		}
		panel.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (child == panel || child == zoomControls) {
			super.addView(child, params);
			return;
		}
		FrameLayout.LayoutParams newParams = null;
		if (params != null) {
			newParams = new FrameLayout.LayoutParams(params);
		}
		panel.addView(child, newParams);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (child == panel || child == zoomControls) {
			super.addView(child, index, params);
			return;
		}
		FrameLayout.LayoutParams newParams = null;
		if (params != null) {
			newParams = new FrameLayout.LayoutParams(params);
		}
		panel.addView(child, index, newParams);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		log.config( String.format("onLayout rect = (%s,%s) x (%s,%s)", l, t, r, b));
		int width = r-l;
		int height = b-t;

		if (panel.getChildCount() > 0) {
			View child = panel.getChildAt(0);
			panel.measure(MeasureSpec.makeMeasureSpec(Math.min(width, child.getLayoutParams().width), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(height, child.getLayoutParams().height), MeasureSpec.EXACTLY));
		} else {
			panel.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		}

		panel.layout(
				((r - l) - panel.getMeasuredWidth()) / 2 + l,
				((b - t) - panel.getMeasuredHeight()) / 2 + t,
				r - ((r - l) - panel.getMeasuredWidth()) / 2,
				b - ((b - t) - panel.getMeasuredHeight()) / 2
		);

		zoomControls.layout(
				((r - l) - zoomControls.getMeasuredWidth()) / 2 + l,
				b - zoomControls.getMeasuredHeight(),
				r - ((r - l) - zoomControls.getMeasuredWidth()) / 2,
				b
		);

		//merge anchor points
		if (panel.getChildCount() > 0) {
			int panelOffsetX = (int) (panel.getWidth() * anchorPointParent.x);
			int childOffsetX = (int) (panel.getChildAt(0).getWidth() * anchorPointChild.x);
			int panelOffsetY = (int) (panel.getHeight() * anchorPointParent.y);
			int childOffsetY = (int) (panel.getChildAt(0).getHeight() * anchorPointChild.y);

			panel.scrollTo(
					Math.min(Math.max(childOffsetX - panelOffsetX, 0), Math.max(panel.getChildAt(0).getWidth() - panel.getWidth(), 0)),
					Math.min(Math.max(childOffsetY - panelOffsetY, 0), Math.max(panel.getChildAt(0).getHeight() - panel.getHeight(), 0)));
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		log.config( "onMeasure zoom = " + zoom);

		int width = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
		switch (MeasureSpec.getMode(widthMeasureSpec)) {
			case MeasureSpec.AT_MOST:
			case MeasureSpec.EXACTLY:
				width = MeasureSpec.getSize(widthMeasureSpec);
		}

		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) log.config( "width measure mode = AT_MOST");
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) log.config( "width measure mode = EXACTLY");
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) log.config( "width measure mode = UNSPECIFIED");

		int height = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
		switch (MeasureSpec.getMode(heightMeasureSpec)) {
			case MeasureSpec.AT_MOST:
			case MeasureSpec.EXACTLY:
				height = MeasureSpec.getSize(heightMeasureSpec);
		}
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) log.config(     "height measure mode = AT_MOST");
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) log.config(     "height measure mode = EXACTLY");
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) log.config( "height measure mode = UNSPECIFIED");


		log.config( "width = " + width);
		log.config( "height = " + height);

		zoomControls.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
		setMeasuredDimension(width, height);
	}


	@Override
	public int getChildCount() {
		return panel.getChildCount();
	}

	@Override
	public View getChildAt(int index) {
		return panel.getChildAt(index);
	}

	@Override
	public void removeView(View view) {
		panel.removeView(view);
	}

	@Override
	public void removeViewInLayout(View view) {
		panel.removeViewInLayout(view);
	}

	@Override
	public void removeViewsInLayout(int start, int count) {
		panel.removeViewsInLayout(start, count);
	}

	@Override
	public void removeViewAt(int index) {
		panel.removeViewAt(index);
	}

	@Override
	public void removeViews(int start, int count) {
		panel.removeViews(start, count);
	}

	@Override
	public void removeAllViews() {
		panel.removeAllViews();
	}

	@Override
	public void removeAllViewsInLayout() {
		panel.removeAllViewsInLayout();
	}

	private void setZoomToView(View view, double newZoom) {
		if (view instanceof ViewGroup)
			for (int i = 0; i < ((ViewGroup)view).getChildCount();  ++i )
				setZoomToView(((ViewGroup)view).getChildAt(i), newZoom);
		else {
			view.getLayoutParams().height = (int) (view.getLayoutParams().height/zoom * newZoom);
			view.getLayoutParams().width = (int) (view.getLayoutParams().width/zoom * newZoom);
		}
	}
	
	private void setZoom(double newZoom) {
		newZoom = Math.min(Math.max(newZoom, minZoom), maxZoom);
		if (newZoom != zoom) {
			

			if (panel.getChildCount() > 0){
				View child = panel.getChildAt(0);

				setZoomToView(child, newZoom);
				//calculate abs position of anchor point on child view
				int absoluteAnchorPointChildX = (int) ((panel.getWidth() * anchorPointParent.x) + panel.getScrollX());
				int absoluteAnchorPointChildY = (int) ((panel.getHeight() * anchorPointParent.y) + panel.getScrollY());
				
				anchorPointChild = new PointF((float)((double)absoluteAnchorPointChildX/(double)child.getWidth()), (float)((double)absoluteAnchorPointChildY/(double)child.getHeight()));
			}
			
			zoom = newZoom;
			requestLayout();
			// panel will not change layout if bounds not changed, force it to layout
			panel.requestLayout();
		}
		
		zoomControls.setIsZoomOutEnabled(zoom > minZoom);
		zoomControls.setIsZoomInEnabled(zoom < maxZoom);
		panel.invalidate();
	}

	private void setup(Context context) {
		panel = new TwoDirectionsScrollView(context);
		addView(panel);
		zoom = 1;
		zoomControls = new ZoomControls(context);
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View view) {
				setZoom(zoom * zoomSize);
			}
		});
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View view) {
				setZoom(zoom / zoomSize);
			}
		});

		addView(zoomControls);
	}
}
