/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.transformgalery;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.Transformation;
import android.widget.*;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Base view for creation controls with hard 3d transformation (such as coverflow)<br/>
 * Use views and {@link Transformation} objects
 *
 * @author Swift
 */
public class Transform2DFrame extends AdapterView<TransformMap> {
	private boolean abort;
	private final Logger log = Logger.getLogger(getClass().getName());
//	private FixedSizeCache<View> cache = new FixedSizeCache<View>(20);
	CellIndependent mode = CellIndependent.NO_INDEPENDENT;
	boolean changed;
	boolean invalidated;
	boolean verticalCircle;
	boolean horizontalCircle;
	Transform2DGalleryDatasetObserver dataSetObserver;
	int widthMeasureSpec;
	int heightMeasureSpec;
	private boolean flingHorizontal = false;
	private boolean flingVertical   = false;
	Scroller scroller;
	VerifyScrollThread scrollUpdater = new VerifyScrollThread();
	boolean touchStart = false;
	private TransformMap adapter;
	private GestureDetector detector;
	private int[] virtualCenterX = new int[]{0};
	private int[] virtualCenterY = new int[]{0};
	private int selection = 0;
	private int[] visibleViews;

	public Transform2DFrame(Context context) {
		super(context);
		init(context);
	}

	public Transform2DFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Transform2DFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * sets mode of cell scrolling
	 *
	 * @param mode scroll mode
	 * @see CellIndependent
	 */
	public void setMode(CellIndependent mode) {
		this.mode = mode;
		switch (this.mode){
			case NO_INDEPENDENT:
				virtualCenterX = new int[]{0};
				virtualCenterY = new int[]{0};
				break;
			case INDEPENDENT_COLUMNS:
				virtualCenterX = new int[]{0};
				virtualCenterY = new int[adapter.getColumnCount()];
				break;
			case INDEPENDENT_ROWS:
				virtualCenterX = new int[adapter.getRowCount()];
				virtualCenterY = new int[]{0};
				break;
		}
	}

	/**
	 * enables/disable repeatable y coordinate mode
	 *
	 * @param verticalCircle is repeatable y coordinate mode enabled
	 */
	public void setVerticalCircle(boolean verticalCircle) {
		this.verticalCircle = verticalCircle;
	}

	/**
	 * enables/disable repeatable x coordinate mode
	 *
	 * @param horizontalCircle is repeatable x coordinate mode enabled
	 */
	public void setHorizontalCircle(boolean horizontalCircle) {
		this.horizontalCircle = horizontalCircle;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) return super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			touchStart = true;
		}
		boolean b = detector.onTouchEvent(event);
		if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
			touchStart = false;
			if (!scroller.computeScrollOffset()) {
				scrollInSlots();
			}
		}
		return b;
	}

	@Override
	public TransformMap getAdapter() {
		return adapter;
	}

	@Override
	public void setAdapter(TransformMap adapter) {
		if (this.adapter != null) {
			this.adapter.unregisterDataSetObserver(dataSetObserver);
		}
		this.adapter = adapter;
		if (adapter != null){
			dataSetObserver = new Transform2DGalleryDatasetObserver();
			adapter.registerDataSetObserver(dataSetObserver);
		} else {
			dataSetObserver = null;
		}
		visibleViews = null;
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public View getSelectedView() {
		return getView(selection);
	}

	@Override
	public void setSelection(int i) {
		if (visibleViews != null) {
			for (int visibleView : visibleViews) {
				getView(visibleView).setFocusable(false);
			}
		}
		selection = i;
		getSelectedView().setFocusable(true);
		getSelectedView().requestFocus();

		if (getOnItemSelectedListener() != null){
			getOnItemSelectedListener().onItemSelected(this, getSelectedView(), selection, getSelectedItemId());
		}
		if (mode == CellIndependent.NO_INDEPENDENT) {
			Point position = adapter.getPosition(selection);
			virtualCenterX[0] = position.x;
			virtualCenterY[0] = position.y;
		}

		if (mode == CellIndependent.INDEPENDENT_ROWS) {
			Point position = adapter.getPosition(selection);
			virtualCenterY[0] = position.y;
			virtualCenterX[adapter.getPointNear(0, position.y).y] = position.x;
		}
		if (mode == CellIndependent.INDEPENDENT_COLUMNS){
			Point position = adapter.getPosition(selection);
			virtualCenterX[0] = position.x;
			virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x] = position.y;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = 0;
		this.widthMeasureSpec = widthMeasureSpec;
		this.heightMeasureSpec = heightMeasureSpec;
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY){
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		int height = 0;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		adapter.setContainerSize(getWidth(), getHeight());
		verifyVisibleRect();
	}

	@Override
	public int getSelectedItemPosition() {
		return selection;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
//		System.out.println("getChildCount() = " + getChildCount());
		int childIndex = -1;
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) == child) {
				childIndex = i;
			}
		}
		if (childIndex < 0 || visibleViews == null) {
			log.severe("Unable to find view");
			return false;
		}
		Point position = adapter.getPosition(visibleViews[childIndex]);
		Point twoDIndex = adapter.getPointNear(position.x, position.y);
		int cx = 0;
		int cy = 0;
		switch (mode) {
			case NO_INDEPENDENT:
				cx = virtualCenterX[0];
				cy = virtualCenterY[0];
				break;
			case INDEPENDENT_COLUMNS:
				cx = virtualCenterX[0];
				cy = virtualCenterY[twoDIndex.x];
				break;
			case INDEPENDENT_ROWS:
				cy = virtualCenterY[0];
				cx = virtualCenterX[twoDIndex.y];
				break;
		}


		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		t.setAlpha(adapter.getAlphaForView(visibleViews[childIndex], cx, cy));
		t.getMatrix().set(adapter.getTransformForView(visibleViews[childIndex], cx, cy));

		return true;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return event.dispatch(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!isEnabled()) return super.onKeyDown(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			return moveUp();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			return moveDown();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			return moveLeft();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return moveRight();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (!isEnabled()) return super.onKeyUp(keyCode, event);

		if (scroller.isFinished()){
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
				if (getOnItemClickListener() != null){
					getOnItemClickListener().onItemClick(this, getSelectedView(), getSelectedItemPosition(), getSelectedItemId());
					return true;
				}
				return false;
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	private void scrollInSlots() {
		log.config( "Scroll In Slots");
		if (visibleViews == null) return;
		if (touchStart) return;
		if (mode == CellIndependent.NO_INDEPENDENT) {
			int index = adapter.getIndexNear(virtualCenterX[0], virtualCenterY[0]);
			Point position = adapter.getPosition(index);
			if (horizontalCircle && virtualCenterX[0] - position.x > adapter.getXSize() / 2){
				position.x += adapter.getXSize();
			}
			if (verticalCircle && virtualCenterY[0] - position.y > adapter.getYSize() / 2){
				position.y += adapter.getYSize();
			}
			if (position.x - virtualCenterX[0] != 0 || position.y - virtualCenterY[0] != 0) {
				scroller.startScroll(virtualCenterX[0], virtualCenterY[0], position.x - virtualCenterX[0], position.y - virtualCenterY[0], 300);
				post(scrollUpdater);
			} else {
				setSelection(index);
			}
		}

		if (mode == CellIndependent.INDEPENDENT_ROWS) {
			int positionY = virtualCenterY[0];
			int positionX = virtualCenterX[adapter.getPointNear(0, positionY).y];
			log.config( "Position = " + positionX + " " + positionY);
			int index = adapter.getIndexNear(positionX, positionY);
			Point position = adapter.getPosition(index);
			log.config( "Target Position = " + position.x + " " + position.y);
			if (horizontalCircle && positionX - position.x > adapter.getXSize() / 2){
				position.x += adapter.getXSize();
			}
			if (verticalCircle && positionY - position.y > adapter.getYSize() / 2){
				position.y += adapter.getYSize();
			}
			log.config( "Target Position = " + position.x + " " + position.y);
			if (position.x - positionX != 0 || position.y - positionY != 0) {
				scroller.startScroll(positionX, positionY, position.x - positionX, position.y - positionY, 300);
				flingVertical = true;
				flingHorizontal = true;
				post(scrollUpdater);
			} else {
				setSelection(index);
			}
		}
		if (mode == CellIndependent.INDEPENDENT_COLUMNS){
			int index = adapter.getIndexNear(virtualCenterX[0], virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x]);
			Point position = adapter.getPosition(index);
			if (horizontalCircle && virtualCenterX[0] - position.x > adapter.getXSize() / 2){
				position.x += adapter.getXSize();
			}
			if (verticalCircle && virtualCenterY[0] - position.y > adapter.getYSize() / 2){
				position.y += adapter.getYSize();
			}
			if (position.x - virtualCenterX[0] != 0 || position.y - virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x] != 0) {
				scroller.startScroll(virtualCenterX[0], virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x], position.x - virtualCenterX[0], position.y - virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x], 300);
				flingVertical = true;
				flingHorizontal = true;
				post(scrollUpdater);
			} else {
				setSelection(index);
			}
		}
	}

	private void init(Context context) {
		scroller = new Scroller(getContext());
		detector = new GestureDetector(context, new GestureListenerImpl());
		setStaticTransformationsEnabled(true);
		setAlwaysDrawnWithCacheEnabled(true);
	}

	private View getView(int index) {
//		View result = cache.getObject(index);
//		if (result != null) {
//			cache.cacheObject(index, result);
//			return result;
//		}
		View result = adapter.getView(index, null, this);
//		cache.cacheObject(index, result);
		return result;

	}

	private void verifyVisibleRect() {
		if (visibleViews == null) visibleViews = new int[0];
//		int cx = 0;
//		int cy = 0;
//		switch (mode) {
//			case NO_INDEPENDENT:
//				cx = virtualCenterX[0];
//				cy = virtualCenterY[0];
//				break;
//			case INDEPENDENT_COLUMNS:
//				cx = virtualCenterX[0];
//				cy = virtualCenterY[transform.getPointNear(cx, 0).x];
//				break;
//			case INDEPENDENT_ROWS:
//				cy = virtualCenterY[0];
//				cx = virtualCenterX[transform.getPointNear(0, cy).y];
//				break;
//		}
		// TODO: finding Battle neks
		int[] newVisibleViews = adapter.getVisibleViews(visibleViews, virtualCenterX, virtualCenterY);
//		int[] newVisibleViews;
//		if (visibleViews.length > 0){
//			newVisibleViews = visibleViews;
//		} else {
//			newVisibleViews = transform.getVisibleViews(visibleViews, virtualCenterX, virtualCenterY);
//		}
//		Arrays.sort(newVisibleViews);
//		if (visibleViews.length > 0) return;
		if (visibleViews == null || Arrays.equals(newVisibleViews, visibleViews)) return;

		detachAllViewsFromParent();

		int oldViewsIterator = 0;
		int newViewsIterator = 0;
		while (oldViewsIterator < visibleViews.length || newViewsIterator < newVisibleViews.length) {
			if (oldViewsIterator < visibleViews.length && newViewsIterator < newVisibleViews.length) {
				if (visibleViews[oldViewsIterator] == newVisibleViews[newViewsIterator]) {
					layoutView(visibleViews[oldViewsIterator], oldViewsIterator);
					oldViewsIterator++;
					newViewsIterator++;
					continue;
				}

				removeViewInLayout(getChildAt(newViewsIterator));
				layoutView(newVisibleViews[newViewsIterator], newViewsIterator);
				oldViewsIterator++;
				newViewsIterator++;
				continue;

//				if (visibleViews[oldViewsIterator] < newVisibleViews[newViewsIterator]) {
//					removeViewInLayout(getChildAt(newViewsIterator));
//					oldViewsIterator++;
//					continue;
//				}
//				if (visibleViews[oldViewsIterator] > newVisibleViews[newViewsIterator]){
//					layoutView(newVisibleViews[newViewsIterator], newViewsIterator);
//					newViewsIterator++;
//					continue;
//				}
			}
			if (oldViewsIterator < visibleViews.length) {
				removeViewInLayout(getChildAt(newViewsIterator));
				oldViewsIterator++;
				continue;
			}
			if (newViewsIterator < newVisibleViews.length) {
				layoutView(newVisibleViews[newViewsIterator], newViewsIterator);
				newViewsIterator++;
				continue;
			}
		}
		visibleViews = newVisibleViews;
	}

	private void layoutView(int index, int viewPosition) {
		View child = getView(index);
		LayoutParams lp = child.getLayoutParams();
		if (lp == null) {
			lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}

		child.setDrawingCacheEnabled(true);
		child.setWillNotCacheDrawing(false);

		addViewInLayout(child, viewPosition, lp);
		child.measure(ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, lp.width), ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, lp.height));
		child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
	}

	private boolean moveLeft() {
		Point twoDIndex = null;
		int cx = 0;
		switch (mode) {
			case NO_INDEPENDENT:
				cx = virtualCenterX[0];
				twoDIndex = adapter.getPointNear(cx, virtualCenterX[0]);
				break;
			case INDEPENDENT_ROWS:
				cx = virtualCenterX[adapter.getPointNear(0, virtualCenterY[0]).y];
				twoDIndex = adapter.getPointNear(cx, virtualCenterY[0]);
				break;
			case INDEPENDENT_COLUMNS:
				cx = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(cx, 0);
				break;
		}

		if (twoDIndex.x <= 0 && !horizontalCircle) return false;
		scroller.startScroll(cx, 0, adapter.getPositionForPoint(new Point(twoDIndex.x - 1, 0)).x - cx, 0, 500);
		flingHorizontal = true;
		post(scrollUpdater);
		return true;
	}

	private boolean moveRight() {
		Point twoDIndex = null;
		int cx = 0;
		switch (mode) {
			case NO_INDEPENDENT:
				cx = virtualCenterX[0];
				twoDIndex = adapter.getPointNear(cx, virtualCenterX[0]);
				break;
			case INDEPENDENT_ROWS:
				cx = virtualCenterX[adapter.getPointNear(0, virtualCenterY[0]).y];
				twoDIndex = adapter.getPointNear(cx, virtualCenterY[0]);
				break;
			case INDEPENDENT_COLUMNS:
				cx = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(cx, 0);
				break;
		}

		if (twoDIndex.x >= adapter.getColumnCount() - 1 && !horizontalCircle) return false;
		scroller.startScroll(cx, 0, adapter.getPositionForPoint(new Point(twoDIndex.x + 1, 0)).x - cx, 0, 500);
		flingHorizontal = true;
		post(scrollUpdater);
		return true;
	}

	private boolean moveDown() {
		Point twoDIndex = null;
		int cy = 0;
		switch (mode) {
			case NO_INDEPENDENT:
				cy = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(virtualCenterX[0], cy);
				break;
			case INDEPENDENT_COLUMNS:
				cy = virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x];
				twoDIndex = adapter.getPointNear(virtualCenterX[0], cy);
				break;
			case INDEPENDENT_ROWS:
				cy = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(0, cy);
				break;
		}

		if (twoDIndex.y >= adapter.getColumnCount() - 1 && !verticalCircle) return false;
		scroller.startScroll(0, cy, 0, adapter.getPositionForPoint(new Point(0, twoDIndex.y + 1)).y - cy, 500);
		flingVertical = true;
		post(scrollUpdater);
		return true;
	}

	private boolean moveUp() {
		Point twoDIndex = null;
		int cy = 0;
		switch (mode) {
			case NO_INDEPENDENT:
				cy = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(virtualCenterX[0], cy);
				break;
			case INDEPENDENT_COLUMNS:
				cy = virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x];
				twoDIndex = adapter.getPointNear(virtualCenterX[0], cy);
				break;
			case INDEPENDENT_ROWS:
				cy = virtualCenterY[0];
				twoDIndex = adapter.getPointNear(0, cy);
				break;
		}

		if (twoDIndex.y <= 0 && !verticalCircle) return false;
		scroller.startScroll(0, cy, 0, adapter.getPositionForPoint(new Point(0, twoDIndex.y - 1)).y - cy, 500);
		flingVertical = true;
		post(scrollUpdater);
		return true;
	}

	private class VerifyScrollThread implements Runnable {

		public void run() {
			if (!scroller.computeScrollOffset()) {
				flingVertical = false;
				flingHorizontal = false;
				scrollInSlots();
			} else {
				if (mode == CellIndependent.NO_INDEPENDENT) {
					if (verticalCircle) {
						virtualCenterY[0] = adapter.getYSize() != 0? scroller.getCurrY() % adapter.getYSize():0;
						if (virtualCenterY[0] < 0) virtualCenterY[0] += adapter.getYSize();
					} else {
						virtualCenterY[0] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
					}
					if (horizontalCircle) {
						virtualCenterX[0] = adapter.getXSize() != 0? scroller.getCurrX() % adapter.getXSize():0;
						if (virtualCenterX[0] < 0) virtualCenterX[0] += adapter.getXSize();
					} else {
						virtualCenterX[0] =  Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
					}
				}
				if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
					if (flingVertical) {
						int index = adapter.getPointNear(virtualCenterX[0], 0).x;
						if (verticalCircle) {
							virtualCenterY[index] = adapter.getYSize() != 0? scroller.getCurrY() % adapter.getYSize():0;
							if (virtualCenterY[index] < 0) virtualCenterY[index] += adapter.getYSize();
						} else {
							virtualCenterY[index] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
						}
					}
					if (flingHorizontal) {
						if (horizontalCircle) {
							virtualCenterX[0] = adapter.getXSize() != 0? scroller.getCurrX() % adapter.getXSize():0;
							if (virtualCenterX[0] < 0) virtualCenterX[0] += adapter.getXSize();
						} else {
							virtualCenterY[0] =  Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
						}
					}
				}
				if (mode == CellIndependent.INDEPENDENT_ROWS) {
					if (flingHorizontal) {
						int index = adapter.getPointNear(0,virtualCenterY[0]).y;
						if (horizontalCircle) {
							virtualCenterX[index] = adapter.getXSize() != 0? scroller.getCurrX() % adapter.getXSize():0;
							if (virtualCenterX[index] < 0) virtualCenterX[index] += adapter.getXSize();
						} else {
							virtualCenterX[index] = Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
						}
					}
					if (flingVertical) {
						if (verticalCircle) {
							virtualCenterY[0] = adapter.getYSize() != 0? scroller.getCurrY() % adapter.getYSize():0;
							if (virtualCenterY[0] < 0) virtualCenterY[0] += adapter.getYSize();
						} else {
							virtualCenterY[0] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
						}
					}
				}
				verifyVisibleRect();
				invalidate();
				post(this);
			}
		}
	}

	private class GestureListenerImpl implements GestureDetector.OnGestureListener{
		public boolean onDown(MotionEvent motionEvent) {
			abort = scroller.isFinished();
			scroller.forceFinished(true);
			return true;
		}

		public void onShowPress(MotionEvent motionEvent) {
		}

		public boolean onSingleTapUp(MotionEvent motionEvent) {
			log.info("onSingleTapUp");
			if (!abort) return true;
			if (scroller.isFinished()) {
				for (int i = visibleViews.length - 1; i >= 0; i--) {
					int viewNum = visibleViews[i];
					Point position = adapter.getPosition(viewNum);
					Point twoDIndex = adapter.getPointNear(position.x, position.y);
					int cx = 0;
					int cy = 0;
					switch (mode) {
						case NO_INDEPENDENT:
							cx = virtualCenterX[0];
							cy = virtualCenterY[0];
							break;
						case INDEPENDENT_COLUMNS:
							cx = virtualCenterX[0];
							cy = virtualCenterY[twoDIndex.x];
							break;
						case INDEPENDENT_ROWS:
							cy = virtualCenterY[0];
							cx = virtualCenterX[twoDIndex.y];
							break;
					}
					Matrix forView = adapter.getTransformForView(viewNum, cx, cy);
					Matrix newMatrix = new Matrix();
					forView.invert(newMatrix);
					float[] floats = {motionEvent.getX(), motionEvent.getY()};
					newMatrix.mapPoints(floats);
					if (floats[0] > 0 && floats[0] < getChildAt(i).getWidth() && floats[1] > 0 && floats[1] < getChildAt(i).getHeight()) {
						log.info("onSingleTapUp index = " + viewNum);

						if (adapter.needScroll(viewNum) && viewNum != selection) {
							flingVertical = true;
							flingHorizontal = true;
							int lengthX = position.x - cx;
							int lengthY = position.y - cy;

							if (horizontalCircle && Math.abs(lengthX) > adapter.getXSize() - Math.abs(lengthX)) {
								lengthX = -(int) Math.signum(lengthX) * adapter.getXSize() + lengthX;
							}

							if (verticalCircle && Math.abs(lengthY) > adapter.getYSize() - Math.abs(lengthY)) {
								lengthY = -(int) Math.signum(lengthY) * adapter.getYSize() + lengthY;
							}

							scroller.startScroll(cx, cy, lengthX, lengthY, 500);
							post(scrollUpdater);
							return true;
						} else {
							if (getOnItemClickListener() != null) {
								getOnItemClickListener().onItemClick(Transform2DFrame.this, getView(viewNum), viewNum, adapter.getItemId(viewNum));
								return true;
							}
						}
					}
				}
			}


			return false;
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (mode == CellIndependent.NO_INDEPENDENT) {
				virtualCenterX[0] += distanceX;
				if (virtualCenterX[0] > adapter.getXSize()) {
					if (horizontalCircle){
						virtualCenterX[0] -= adapter.getXSize();
					} else {
						virtualCenterX[0] = adapter.getXSize();
					}
				}
				if (virtualCenterX[0] < 0) {
					if (horizontalCircle){
						virtualCenterX[0] += adapter.getXSize();
					} else {
						virtualCenterX[0] = 0;
					}
				}
				virtualCenterY[0] += distanceY;
				if (virtualCenterY[0] > adapter.getYSize()) {
					if (verticalCircle) {
						virtualCenterY[0] -= adapter.getYSize();
					} else {
						virtualCenterY[0] = adapter.getYSize();
					}
				}
				if (virtualCenterY[0] < 0) {
					if (verticalCircle) {
						virtualCenterY[0] += adapter.getYSize();
					} else {
						virtualCenterY[0] = 0;
					}
				}
			}
			if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
				virtualCenterX[0] += distanceX;
				if (virtualCenterX[0] > adapter.getXSize()) {
					if (horizontalCircle){
						virtualCenterX[0] -= adapter.getXSize();
					} else {
						virtualCenterX[0] = adapter.getXSize();
					}
				}
				if (virtualCenterX[0] < 0) {
					if (horizontalCircle){
						virtualCenterX[0] += adapter.getXSize();
					} else {
						virtualCenterX[0] = 0;
					}
				}
				int columnNum = adapter.getPointNear(virtualCenterX[0], 0).x;
				virtualCenterY[columnNum] += distanceY;
				if (virtualCenterY[columnNum] > adapter.getYSize()) {
					if (verticalCircle){
						virtualCenterY[columnNum] -= adapter.getYSize();
					} else {
						virtualCenterY[columnNum] = adapter.getYSize();
					}
				}
				if (virtualCenterY[columnNum] < 0) {
					if (verticalCircle) {
						virtualCenterY[columnNum] += adapter.getYSize();
					} else {
						virtualCenterY[columnNum] = 0;
					}
				}
			}
			if (mode == CellIndependent.INDEPENDENT_ROWS) {
				virtualCenterY[0] += distanceY;
				if (virtualCenterY[0] > adapter.getYSize()) {
					if (verticalCircle) {
						virtualCenterY[0] -= adapter.getYSize();
					} else {
						virtualCenterY[0] = adapter.getYSize();
					}
				}
				if (virtualCenterY[0] < 0) {
					if (verticalCircle) {
						virtualCenterY[0] += adapter.getYSize();
					} else {
						virtualCenterY[0] = 0;
					}
				}

				int rowNum = adapter.getPointNear(0, virtualCenterY[0]).y;

				virtualCenterX[rowNum] += distanceX;
				if (virtualCenterX[rowNum] > adapter.getXSize()) {
					if (horizontalCircle){
						virtualCenterX[rowNum] -= adapter.getXSize();
					} else {
						virtualCenterX[rowNum] = adapter.getXSize();
					}
				}
				if (virtualCenterX[rowNum] < 0) {
					if (horizontalCircle){
						virtualCenterX[rowNum] += adapter.getXSize();
					} else {
						virtualCenterX[rowNum] = 0;
					}
				}
			}
			verifyVisibleRect();
			invalidate();
			return true;
		}

		public void onLongPress(MotionEvent motionEvent) {
			if (getOnItemLongClickListener() != null){
				getOnItemLongClickListener().onItemLongClick(Transform2DFrame.this, getSelectedView(), getSelectedItemPosition(), getSelectedItemId());
			}
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			int maxX = horizontalCircle ? Integer.MAX_VALUE : adapter.getXSize();
			int maxY = verticalCircle ? Integer.MAX_VALUE : adapter.getYSize();
			int minX = horizontalCircle ? Integer.MIN_VALUE : 0;
			int minY = verticalCircle ? Integer.MIN_VALUE : 0;
			if (mode == CellIndependent.NO_INDEPENDENT){
				scroller.fling(virtualCenterX[0], virtualCenterY[0], (int) -velocityX, (int) -velocityY, minX, maxX, minY, maxY);
			}
			if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
				if (Math.abs(velocityX) > Math.abs(velocityY)){
					scroller.fling(virtualCenterX[0], 0, (int) -velocityX, 0, minX, maxX, minY, maxY);
					flingHorizontal = true;
					flingVertical = false;
				} else {
					scroller.fling(virtualCenterX[0], virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x], 0, (int) -velocityY, minX, maxX, minY, maxY);
					flingHorizontal = false;
					flingVertical = true;
				}
			}
			if (mode == CellIndependent.INDEPENDENT_ROWS) {
				if (Math.abs(velocityY) > Math.abs(velocityX)){
					scroller.fling(0, virtualCenterY[0], 0, (int) -velocityY, minX, maxX, minY, maxY);
					flingHorizontal = false;
					flingVertical = true;
				} else {
					scroller.fling(virtualCenterX[adapter.getPointNear(0, virtualCenterY[0]).y], virtualCenterY[0], (int) -velocityX, 0, minX, maxX, minY, maxY);
					flingHorizontal = true;
					flingVertical = false;
				}
			}
			post(scrollUpdater);
			return true;
		}
	}

	private class Transform2DGalleryDatasetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			super.onChanged();
			verifyVisibleRect();
			invalidate();
			changed = true;
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			verifyVisibleRect();
			invalidate();
			invalidated = true;
		}
	}

	/**
	 * Modes of cell scrolling (as independent rows, as independent columns, as 2d panel)
	 */
	public static enum CellIndependent {
		NO_INDEPENDENT, INDEPENDENT_ROWS, INDEPENDENT_COLUMNS
	}
}