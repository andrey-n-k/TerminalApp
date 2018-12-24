/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.openglgallery;

import android.content.Context;
import android.graphics.*;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.*;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.Scroller;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.*;
import java.util.logging.Logger;

/**
 * Base view for creation controls with hard 3d transformation (such as coverflow)<br/>
 * Use OpenGL driver, and need some time to load, but have more performance then {@link com.arellomobile.android.libs.ui.transformgalery.Transform2DFrame}
 *
 * @author Swift
 */
public class GLGallery extends GLSurfaceView {
	boolean texturesLoaded = false;
	int[] texturesForView;
	int[] texturesMapping;
	private boolean isCreated = false;
	GLViewPainter glViewPainter = new GLViewPainter();
	static final float PROJECTION_LEFT = -1;
	static final float PROJECTION_RIGHT = 1;
	static final float PROJECTION_TOP = 1;
	static final float PROJECTION_BOTTOM = -1;
	static final float PROJECTION_NEAR = 1;
	static final float PROJECTION_FAR = 11;
	static final int ANIMATION_DURATION = 300;
	private final Logger log = Logger.getLogger(getClass().getName());
	private GL10 callback;
	private OnItemSelectedListener onItemSelectedListener;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private OnScrollListener onScrollListener;
	private boolean scrollStartNotified = false;
	private boolean isStop = false;
	private boolean tapAfterScroll = false;
	private CellIndependent mode = CellIndependent.NO_INDEPENDENT;
	private boolean verticalCircle;
	private boolean horizontalCircle;
	private boolean flingHorizontal = false;
	private boolean flingVertical = false;
	private Scroller scroller;
	private VerifyScrollThread scrollUpdater = new VerifyScrollThread();
	private boolean touchStart = false;
	private GestureDetector detector;
	private TransformMap adapter;
	private int[] virtualCenterX = new int[]{0};
	private int[] virtualCenterY = new int[]{0};
	private int selection = 0;
	private int[] visibleViews;
	private final Object visibleViewsMutex = new Object();

	public GLGallery(Context context) {
		super(context);
		init(context);
	}

	public GLGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * sets mode of cell scrolling
	 *
	 * @param mode scroll mode
	 * @see com.arellomobile.android.libs.ui.openglgallery.GLGallery.CellIndependent
	 */
	public void setMode(CellIndependent mode) {
		this.mode = mode;
		switch (this.mode) {
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

	public boolean isCreated() {
		return isCreated;
	}

	public OnItemSelectedListener getOnItemSelectedListener() {
		return onItemSelectedListener;
	}

	/**
	 * Register a callback to be invoked when an item in this GLGallery has been selected
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.onItemSelectedListener = listener;
	}

	public OnScrollListener getOnScrollListener() {
		return onScrollListener;
	}

	/**
	 * Register a callback to be invoked when this GLGallery has been scrolled
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnScrollListener(OnScrollListener listener) {
		this.onScrollListener = listener;
	}

	/**
	 * @return The callback to be invoked with an item in this GLGallery has been clicked and held, or null id no callback as been set.
	 */
	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	/**
	 * Register a callback to be invoked when an item in this GLGallery has been clicked.
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.onItemClickListener = listener;
	}

	/**
	 * @return The callback to be invoked with an item in this GLGallery has been clicked, or null id no callback has been set.
	 */
	public OnItemLongClickListener getOnItemLongClickListener() {
		return onItemLongClickListener;
	}

	/**
	 * Register a callback to be invoked when an item in this GLGallery has been clicked and held.
	 *
	 * @param listener The callback that will be invoked.
	 */
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.onItemLongClickListener = listener;
	}

	/**
	 * Returns the adapter currently associated with this widget.
	 *
	 * @return The adapter used to provide this view's content.
	 */
	public TransformMap getAdapter() {
		return adapter;
	}

	/**
	 * Sets the adapter that provides the data and the views to represent the data in this widget.
	 * @param adapter The adapter to use to create this view's content/
	 */
	public void setAdapter(TransformMap adapter) {
		this.adapter = adapter;
	}

	/**
	 * Sets the currently selected item. To support accessibility subclasses that override this method must invoke the overriden super method first.
	 * @param positionIndex	Index (starting at 0) of the data item to be selected.
	 */
	public void setSelection(int positionIndex) {
		if (scrollStartNotified && onScrollListener != null) {
			onScrollListener.scrollEnd();
		}

		selection = positionIndex;

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
		if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
			Point position = adapter.getPosition(selection);
			virtualCenterX[0] = position.x;
			virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x] = position.y;
		}

		if (onItemSelectedListener != null) {
			onItemSelectedListener.onItemSelected(this, selection, adapter.getItemId(selection));
		}
	}

	/**
	 * Return the position of the currently selected item within the adapter's data set.
	 *
	 * @return int Position (starting at 0)
	 */
	public int getSelectedItemPosition() {
		return selection;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = 0;
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		int height = 0;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		verifyVisibleRect();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return event.dispatch(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if we tap on screen we scrolls to point and block user ui
		if (tapAfterScroll) return false;

		if (!scroller.isFinished()) return true;

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
		if (scroller.isFinished()) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
				if (getOnItemClickListener() != null) {
					getOnItemClickListener().onItemClick(this, getSelectedItemPosition(), getSelectedItemId());
					return true;
				}
				return false;
			}
		}

		return super.onKeyUp(keyCode, event);
	}


	private void verifyVisibleRect() {
		if (callback == null) return;
		if (visibleViews == null) visibleViews = new int[0];

		final int[] newVisibleViews = adapter.getVisibleViews(visibleViews, virtualCenterX, virtualCenterY);
		if (visibleViews == null || Arrays.equals(newVisibleViews, visibleViews)) return;
		synchronized (visibleViewsMutex) {
			visibleViews = newVisibleViews;
		}
	}

	private long getSelectedItemId() {
		return adapter.getItemId(getSelectedItemPosition());
	}

	private Point getCenterForView(int[]visibleViews, int childIndex) {
		if (childIndex < 0 || visibleViews == null) {
			log.severe("Unable to find view");
			return null;
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

		return new Point(cx, cy);
	}

	private void init(Context context) {
		setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setRenderer(new Renderer());
		setFocusable(true);
		scroller = new Scroller(getContext());
		detector = new GestureDetector(context, new GestureListener());
	}

	private void scrollInSlots() {
		log.config( "Scroll In Slots");
		if (touchStart) return;
		if (mode == CellIndependent.NO_INDEPENDENT) {
			int index = adapter.getIndexNear(virtualCenterX[0], virtualCenterY[0]);
			Point position = adapter.getPosition(index);
			if (horizontalCircle && virtualCenterX[0] - position.x > adapter.getXSize() / 2) {
				position.x += adapter.getXSize();
			}
			if (verticalCircle && virtualCenterY[0] - position.y > adapter.getYSize() / 2) {
				position.y += adapter.getYSize();
			}
			if (position.x - virtualCenterX[0] != 0 || position.y - virtualCenterY[0] != 0) {
				scroller.startScroll(virtualCenterX[0], virtualCenterY[0], position.x - virtualCenterX[0], position.y - virtualCenterY[0], ANIMATION_DURATION);
				post(scrollUpdater);
			} else {
				setSelection(index);
				if (tapAfterScroll && getOnItemClickListener() != null) {
					tapAfterScroll = false;
					getOnItemClickListener().onItemClick(this, getSelectedItemPosition(), getSelectedItemId());
				}
			}
		}

		if (mode == CellIndependent.INDEPENDENT_ROWS) {
			int positionY = virtualCenterY[0];
			int positionX = virtualCenterX[adapter.getPointNear(0, positionY).y];
			log.config( "Position = " + positionX + " " + positionY);
			int index = adapter.getIndexNear(positionX, positionY);
			Point position = adapter.getPosition(index);
			log.config( "Target Position = " + position.x + " " + position.y);
			if (horizontalCircle && positionX - position.x > adapter.getXSize() / 2) {
				position.x += adapter.getXSize();
			}
			if (verticalCircle && positionY - position.y > adapter.getYSize() / 2) {
				position.y += adapter.getYSize();
			}
			log.config( "Target Position = " + position.x + " " + position.y);
			if (position.x - positionX != 0 || position.y - positionY != 0) {
				scroller.startScroll(positionX, positionY, position.x - positionX, position.y - positionY, ANIMATION_DURATION);
				flingVertical = true;
				flingHorizontal = true;
				post(scrollUpdater);
			} else {
				setSelection(index);
				if (tapAfterScroll && getOnItemClickListener() != null) {
					tapAfterScroll = false;
					getOnItemClickListener().onItemClick(this, getSelectedItemPosition(), getSelectedItemId());
				}
			}
		}
		if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
			int index = adapter.getIndexNear(virtualCenterX[0], virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x]);
			Point position = adapter.getPosition(index);
			if (horizontalCircle && virtualCenterX[0] - position.x > adapter.getXSize() / 2) {
				position.x += adapter.getXSize();
			}
			if (verticalCircle && virtualCenterY[0] - position.y > adapter.getYSize() / 2) {
				position.y += adapter.getYSize();
			}
			if (position.x - virtualCenterX[0] != 0 || position.y - virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x] != 0) {
				scroller.startScroll(virtualCenterX[0], virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x], position.x - virtualCenterX[0], position.y - virtualCenterY[adapter.getPointNear(virtualCenterX[0], 0).x], ANIMATION_DURATION);
				flingVertical = true;
				flingHorizontal = true;
				post(scrollUpdater);
			} else {
				setSelection(index);
				if (tapAfterScroll && getOnItemClickListener() != null) {
					tapAfterScroll = false;
					getOnItemClickListener().onItemClick(this, getSelectedItemPosition(), getSelectedItemId());
				}
			}
		}
	}

	private Bitmap getView(int index) {
		View child = adapter.getView(index, null, null);
		child.measure(MeasureSpec.makeMeasureSpec(child.getLayoutParams().width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
		child.layout(0, 0, child.getLayoutParams().width, child.getLayoutParams().height);
		Bitmap src = Bitmap.createBitmap(child.getLayoutParams().width, child.getLayoutParams().height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(src);
		canvas.drawARGB(0, 0, 0, 0);
		child.draw(canvas);
		Bitmap result = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(result);
		canvas.drawARGB(0, 0, 0, 0);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(src);
		bitmapDrawable.setBounds(0, 0, child.getLayoutParams().width, child.getLayoutParams().height);
		Matrix matrix = new Matrix();
		matrix.setRectToRect(new RectF(0,0,child.getLayoutParams().width, child.getLayoutParams().height), new RectF(0, 0, 256, 256), Matrix.ScaleToFit.FILL);
		canvas.setMatrix(matrix);
		bitmapDrawable.draw(canvas);
		src.recycle();
		return result;

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
		scroller.startScroll(cx, 0, adapter.getPositionForPoint(new Point(twoDIndex.x - 1, 0)).x - cx, 0, ANIMATION_DURATION);
		flingHorizontal = true;
		tapAfterScroll = false;
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
		scroller.startScroll(cx, 0, adapter.getPositionForPoint(new Point(twoDIndex.x + 1, 0)).x - cx, 0, ANIMATION_DURATION);
		flingHorizontal = true;
		tapAfterScroll = false;
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
		scroller.startScroll(0, cy, 0, adapter.getPositionForPoint(new Point(0, twoDIndex.y + 1)).y - cy, ANIMATION_DURATION);
		flingVertical = true;
		tapAfterScroll = false;
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
		scroller.startScroll(0, cy, 0, adapter.getPositionForPoint(new Point(0, twoDIndex.y - 1)).y - cy, ANIMATION_DURATION);
		flingVertical = true;
		tapAfterScroll = false;
		post(scrollUpdater);
		return true;
	}

	/**
	 * 	Interface definition for a callback to be invoked when an item in this GLGallery has been clicked.
	 */
	public static interface OnItemClickListener {
		/**
		 * Callback method to be invoked when an item in this GLGallery has been clicked. Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
		 * @param glGallery The GLGallery where the click happened.
		 * @param position  The position of the view in the adapter.
		 * @param id        The row id of the item that was clicked.
		 */
		void onItemClick(GLGallery glGallery, int position, long id);
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this view has been clicked and held.
	 */
	public static interface OnItemLongClickListener {
		/**
		 * Callback method to be invoked when an item in this view has been clicked and held. Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
		 * @param glGallery The GLGallery where the click happened.
		 * @param position  The position of the view in the adapter.
		 * @param id        The row id of the item that was clicked.
		 * @return true if the callback consumed the long click, false otherwise
		 */
		boolean onItemLongClick(GLGallery glGallery, int position, long id);
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this view has been selected.
	 */
	public static interface OnItemSelectedListener {
		/**
		 * Callback method to be invoked when an item in this view has been selected. Impelmenters can call getItemAtPosition(position) if they need to access the data associated with the selected item.
		 * @param glGallery The GLGallery where the click happened.
		 * @param position  The position of the view in the adapter.
		 * @param id        The row id of the item that was clicked.
		 */
		void onItemSelected(GLGallery glGallery, int position, long id);
	}

	/**
	 * Interface definition for a callback to be invoked when this view has been scrolled
	 */
	public static interface OnScrollListener {
		/**
		 * Callback method to be invoked when an view has strat scrolling
		 */
		void scrollStart();
		/**
		 * Callback method to be invoked when an view has stop scrolling
		 */
		void scrollEnd();
	}

	/**
	 * Modes of cell scrolling (as independent rows, as independent columns, as 2d panel)
	 */
	public static enum CellIndependent {
		NO_INDEPENDENT, INDEPENDENT_ROWS, INDEPENDENT_COLUMNS
	}

	class VerifyScrollThread implements Runnable {

		public void run() {
			if (!scroller.computeScrollOffset()) {
				flingVertical = false;
				flingHorizontal = false;
				scrollInSlots();
			} else {
				if (mode == CellIndependent.NO_INDEPENDENT) {
					if (verticalCircle) {
						virtualCenterY[0] = adapter.getYSize() != 0 ? scroller.getCurrY() % adapter.getYSize() : 0;
						if (virtualCenterY[0] < 0) virtualCenterY[0] += adapter.getYSize();
					} else {
						virtualCenterY[0] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
					}
					if (horizontalCircle) {
						virtualCenterX[0] = adapter.getXSize() != 0 ? scroller.getCurrX() % adapter.getXSize() : 0;
						if (virtualCenterX[0] < 0) virtualCenterX[0] += adapter.getXSize();
					} else {
						virtualCenterX[0] = Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
					}
				}
				if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
					if (flingVertical) {
						int index = adapter.getPointNear(virtualCenterX[0], 0).x;
						if (verticalCircle) {
							virtualCenterY[index] = adapter.getYSize() != 0 ? scroller.getCurrY() % adapter.getYSize() : 0;
							if (virtualCenterY[index] < 0) virtualCenterY[index] += adapter.getYSize();
						} else {
							virtualCenterY[index] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
						}
					}
					if (flingHorizontal) {
						if (horizontalCircle) {
							virtualCenterX[0] = adapter.getXSize() != 0 ? scroller.getCurrX() % adapter.getXSize() : 0;
							if (virtualCenterX[0] < 0) virtualCenterX[0] += adapter.getXSize();
						} else {
							virtualCenterY[0] = Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
						}
					}
				}
				if (mode == CellIndependent.INDEPENDENT_ROWS) {
					if (flingHorizontal) {
						int index = adapter.getPointNear(0, virtualCenterY[0]).y;
						if (horizontalCircle) {
							virtualCenterX[index] = adapter.getXSize() != 0 ? scroller.getCurrX() % adapter.getXSize() : 0;
							if (virtualCenterX[index] < 0) virtualCenterX[index] += adapter.getXSize();
						} else {
							virtualCenterX[index] = Math.max(Math.min(scroller.getCurrX(), adapter.getXSize()), 0);
						}
					}
					if (flingVertical) {
						if (verticalCircle) {
							virtualCenterY[0] = adapter.getYSize() != 0 ? scroller.getCurrY() % adapter.getYSize() : 0;
							if (virtualCenterY[0] < 0) virtualCenterY[0] += adapter.getYSize();
						} else {
							virtualCenterY[0] = Math.max(Math.min(scroller.getCurrY(), adapter.getYSize()), 0);
						}
					}
				}
				verifyVisibleRect();
				reRender();
				post(this);
			}
		}
	}

	class GLViewPainter {
		public GLViewPainter() {

			// Buffers to be passed to gl*Pointer() functions
			// must be direct, i.e., they must be placed on the
			// native heap where the garbage collector cannot
			// move them.
			//
			// Buffers with multi-byte datatypes (e.g., short, int, float)
			// must have their byte order set to native order

			ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
			vbb.order(ByteOrder.nativeOrder());
			mFVertexBuffer = vbb.asFloatBuffer();

			ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
			tbb.order(ByteOrder.nativeOrder());
			mTexBuffer = tbb.asFloatBuffer();

			ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
			ibb.order(ByteOrder.nativeOrder());
			mIndexBuffer = ibb.asShortBuffer();

			// A unit-sided equilateral triangle centered on the origin.
			float[] coords = {
					// X, Y, Z
					-1f, 1f, 0,
					-1f, -1f, 0,
					1f, 1f, 0,
					1f, -1f, 0
			};
			float[] texCoords = {
					0f, 0f,
					0f, 1f,
					1f, 0f,
					1f, 1f
			};

			for (int i = 0; i < VERTS; i++) {
				for (int j = 0; j < 3; j++) {
					mFVertexBuffer.put(coords[i * 3 + j]);
				}
			}

			for (int i = 0; i < VERTS; i++) {
				for (int j = 0; j < 2; j++) {
					mTexBuffer.put(texCoords[i * 2 + j]);
				}
			}

			for (int i = 0; i < VERTS; i++) {
				mIndexBuffer.put((short) i);
			}

			mFVertexBuffer.position(0);
			mTexBuffer.position(0);
			mIndexBuffer.position(0);
		}

		public void draw(GL10 gl) {
			gl.glFrontFace(GL10.GL_CCW);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
			gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
		}

		private final static int VERTS = 4;

		private FloatBuffer mFVertexBuffer;
		private FloatBuffer mTexBuffer;
		private ShortBuffer mIndexBuffer;
	}

	class LoadTexturesThread extends Thread {
		protected final Object synchronizer = new Object();
		protected boolean synchronizeFlag = false;

		@Override
		public void run() {

			List<Long> usedIds = new ArrayList<Long>();
			for (int i = 0; i < texturesMapping.length - 1; i++) {
				if (usedIds.contains(adapter.getItemId(i))) continue;
				usedIds.add(adapter.getItemId(i));
				final Bitmap bitmap = getView(i);
				log.config("bitmap = " + bitmap);
				if (bitmap != null) {
					log.config("bitmap width = " + bitmap.getWidth());
					log.config("bitmap height = " + bitmap.getHeight());
				}

				final int textureId = texturesForView[texturesMapping[i]];
				synchronizeFlag = false;
				post(new Runnable() {
					@Override
					public void run() {
						callback.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

						callback.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

						GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
						bitmap.recycle();
						synchronized (synchronizer){
							synchronizeFlag = true;
							synchronizer.notifyAll();
						}
					}
				});
				synchronized (synchronizer) {
					while (!synchronizeFlag) {
						try{synchronizer.wait();}catch (InterruptedException e){/*pass*/}
					}
				}

			}

			if (getBackground() != null) {
				final Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
				Drawable drawable = getBackground();
				drawable.setBounds(0, 0, 256, 256);
				drawable.draw(new Canvas(bitmap));
				log.config("bitmap = " + bitmap);
				if (bitmap != null) {
					log.config("bitmap width = " + bitmap.getWidth());
					log.config("bitmap height = " + bitmap.getHeight());
				}
				final int textureId = texturesForView[texturesForView.length - 1];
				synchronizeFlag = false;
				post(new Runnable() {
					public void run() {
						callback.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
						callback.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

						callback.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

						GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
						bitmap.recycle();

						setBackgroundDrawable(null);
						synchronized (synchronizer){
							synchronizeFlag = true;
							synchronizer.notifyAll();
						}
					}
				});
				synchronized (synchronizer) {
					while (!synchronizeFlag) {
						try{synchronizer.wait();}catch (InterruptedException e){/*pass*/}
					}
				}
			}

			texturesLoaded = true;
			if (!isCreated) {
				log.config("Show gallery...");
				isCreated = true;
			}
			post(new Runnable() {
				@Override
				public void run() {
					reRender();
				}
			});
		}
	}

	class GestureListener implements GestureDetector.OnGestureListener{
		public boolean onDown(MotionEvent motionEvent) {
			// if we tap on screen we scrolls to point and block user ui
			if (tapAfterScroll) return false;
			isStop = !scroller.isFinished();
			if (isStop && scrollStartNotified && onScrollListener != null) {
				onScrollListener.scrollEnd();
			}
			scrollStartNotified = false;
			scroller.forceFinished(true);
			return true;
		}


		public void onShowPress(MotionEvent motionEvent) {
		}

		public boolean onSingleTapUp(MotionEvent motionEvent) {
			// if we tap on screen we scrolls to point and block user ui
			if (tapAfterScroll) return false;

			if (isStop) return false;

			//Search taped view

			int[] ints = adapter.getVisibleViews(visibleViews, virtualCenterX, virtualCenterY);


			//iterate views in desc (top views paints last)
			for (int i = ints.length - 1; i >= 0; i--) {
				int viewIndex = ints[i];

				// get transform for view
				Point center = getCenterForView(visibleViews, i);
				float[] transformMatrix = adapter.getTransformForView(viewIndex, center.x, center.y);

				// get projection camera matrix
				float[] projectionMatrix = new float[16];
				android.opengl.Matrix.frustumM(projectionMatrix, 0, PROJECTION_LEFT, PROJECTION_RIGHT, PROJECTION_BOTTOM, PROJECTION_TOP, PROJECTION_NEAR, PROJECTION_FAR);

				// aplay transformation to corner points
				float[] translateCoordinates = new float[8];
				android.opengl.Matrix.multiplyMV(translateCoordinates, 0,  transformMatrix, 0, new float[] { 1,  1, 0, 1}, 0);
				android.opengl.Matrix.multiplyMV(translateCoordinates, 4,  transformMatrix, 0, new float[] {-1,  -1, 0, 1}, 0);

				// aplay projection to corner points
				float[] coordinates = new float[8];
				android.opengl.Matrix.multiplyMV(coordinates, 0,  projectionMatrix, 0, translateCoordinates, 0);
				android.opengl.Matrix.multiplyMV(coordinates, 4,  projectionMatrix, 0, translateCoordinates, 4);


				// check valid coordinates
				if (motionEvent.getX() / getWidth() * 2 < coordinates[0] / coordinates[3] + 1
						&& motionEvent.getX() / getWidth() * 2 > coordinates[4 + 0] / coordinates[4+ 3] + 1
						&& 2 - (motionEvent.getY() / getHeight() * 2) < coordinates[1] / coordinates[3] + 1
						&& 2 - (motionEvent.getY() / getHeight() * 2) > coordinates[4 + 1] / coordinates[4+ 3] + 1) {
					if (viewIndex != getSelectedItemPosition()) {
						if (mode == CellIndependent.NO_INDEPENDENT){
							Point position = adapter.getPosition(viewIndex);
							flingVertical = true;
							flingHorizontal = true;
							scrollStartNotified = true;
							if (onScrollListener != null){
								onScrollListener.scrollStart();
							}
							scroller.startScroll(virtualCenterX[0], virtualCenterY[0], position.x - virtualCenterX[0], position.y - virtualCenterY[0], ANIMATION_DURATION);
							tapAfterScroll = true;
							post(scrollUpdater);
							return true;
						}
						if (mode == CellIndependent.INDEPENDENT_ROWS) {
							Point position = adapter.getPosition(viewIndex);
							Point targetPosition = adapter.getPointNear(position.x, position.y);
							int currentRow = adapter.getPointNear(0, virtualCenterY[0]).y;

							scrollStartNotified = true;
							if (onScrollListener != null){
								onScrollListener.scrollStart();
							}
							if (currentRow != targetPosition.y) {
								flingVertical = true;
								flingHorizontal = false;
								// if we change row, not tap on it
								tapAfterScroll = false;
								if (Math.abs(position.y - virtualCenterY[0]) < adapter.getYSize() - Math.abs(position.y - virtualCenterY[0]) || !verticalCircle) {
									scroller.startScroll(0, virtualCenterY[0], 0, position.y - virtualCenterY[0], ANIMATION_DURATION);
								} else if (virtualCenterY[0] < position.y) {
									scroller.startScroll(0, virtualCenterY[0], 0, (position.y - adapter.getYSize()) - virtualCenterY[0], ANIMATION_DURATION);
								} else {
									scroller.startScroll(0, virtualCenterY[0], 0, (position.y + adapter.getYSize()) - virtualCenterY[0], ANIMATION_DURATION);
								}
							} else {
								flingVertical = false;
								flingHorizontal = true;
								// tap on item after scroll
								tapAfterScroll = true;
								if (Math.abs(position.x - virtualCenterX[currentRow]) < adapter.getXSize() - Math.abs(position.x - virtualCenterX[currentRow]) || !horizontalCircle) {
									scroller.startScroll(virtualCenterX[currentRow], 0, position.x - virtualCenterX[currentRow], 0, ANIMATION_DURATION);
								} else if (virtualCenterX[currentRow] < position.x) {
									scroller.startScroll(virtualCenterX[currentRow], 0, (position.x - adapter.getXSize()) - virtualCenterX[currentRow], 0, ANIMATION_DURATION);
								} else {
									scroller.startScroll(virtualCenterX[currentRow], 0, (position.x + adapter.getXSize()) - virtualCenterX[currentRow], 0, ANIMATION_DURATION);
								}
							}
							post(scrollUpdater);
							return true;
						}
						if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
							return true;
						}
					} else {
						if (getOnItemClickListener() != null) {
							getOnItemClickListener().onItemClick(GLGallery.this, getSelectedItemPosition(), getSelectedItemId());
							return true;
						}
					}
				}

			}

			return false;
		}

		public void onLongPress(MotionEvent motionEvent) {
			if (getOnItemLongClickListener() != null) {
				getOnItemLongClickListener().onItemLongClick(GLGallery.this, getSelectedItemPosition(), getSelectedItemId());
			}
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			int maxX = horizontalCircle ? Integer.MAX_VALUE : adapter.getXSize();
			int maxY = verticalCircle ? Integer.MAX_VALUE : adapter.getYSize();
			int minX = horizontalCircle ? Integer.MIN_VALUE : 0;
			int minY = verticalCircle ? Integer.MIN_VALUE : 0;
			if (mode == CellIndependent.NO_INDEPENDENT) {
				scroller.fling(virtualCenterX[0], virtualCenterY[0], (int) -velocityX, (int) -velocityY, minX, maxX, minY, maxY);
			}
			if (mode == CellIndependent.INDEPENDENT_COLUMNS) {
				if (Math.abs(velocityX) > Math.abs(velocityY)) {
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
				if (Math.abs(velocityY) > Math.abs(velocityX)) {
					scroller.fling(0, virtualCenterY[0], 0, (int) -velocityY, minX, maxX, minY, maxY);
					flingHorizontal = false;
					flingVertical = true;
				} else {
					scroller.fling(virtualCenterX[adapter.getPointNear(0, virtualCenterY[0]).y], virtualCenterY[0], (int) -velocityX, 0, minX, maxX, minY, maxY);
					flingHorizontal = true;
					flingVertical = false;
				}
			}
			tapAfterScroll = false;
			post(scrollUpdater);
			return true;
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (!scrollStartNotified && onScrollListener != null){
				onScrollListener.scrollStart();
				scrollStartNotified = true;
			}
			if (mode == CellIndependent.NO_INDEPENDENT) {
				virtualCenterX[0] += distanceX;
				if (virtualCenterX[0] > adapter.getXSize()) {
					if (horizontalCircle) {
						virtualCenterX[0] -= adapter.getXSize();
					} else {
						virtualCenterX[0] = adapter.getXSize();
					}
				}
				if (virtualCenterX[0] < 0) {
					if (horizontalCircle) {
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
					if (horizontalCircle) {
						virtualCenterX[0] -= adapter.getXSize();
					} else {
						virtualCenterX[0] = adapter.getXSize();
					}
				}
				if (virtualCenterX[0] < 0) {
					if (horizontalCircle) {
						virtualCenterX[0] += adapter.getXSize();
					} else {
						virtualCenterX[0] = 0;
					}
				}
				int columnNum = adapter.getPointNear(virtualCenterX[0], 0).x;
				virtualCenterY[columnNum] += distanceY;
				if (virtualCenterY[columnNum] > adapter.getYSize()) {
					if (verticalCircle) {
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
					if (horizontalCircle) {
						virtualCenterX[rowNum] -= adapter.getXSize();
					} else {
						virtualCenterX[rowNum] = adapter.getXSize();
					}
				}
				if (virtualCenterX[rowNum] < 0) {
					if (horizontalCircle) {
						virtualCenterX[rowNum] += adapter.getXSize();
					} else {
						virtualCenterX[rowNum] = 0;
					}
				}
			}
			verifyVisibleRect();
			reRender();
			return true;
		}
	}

	class Renderer implements GLSurfaceView.Renderer {
		public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
			log.config( "GlGallery Surface Created");
			callback = gl10;
			callback.glClearColor(0, 0, 0, 0);

			 callback.glDisable(GL10.GL_DITHER);

			callback.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

			callback.glEnable(GL10.GL_BLEND);
			callback.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

			int texCount = 0;

			texturesMapping = new int[adapter.getCount() + 1];

			Map<Long,Integer> idPositionMapping = new HashMap<Long, Integer>();

			for (int i = 0; i < texturesMapping.length - 1; i++) {
				long id = adapter.getItemId(i);
				Integer idIndex = idPositionMapping.get(id);
				if (idIndex != null) {
					texturesMapping[i] = idIndex;
				} else {
					texturesMapping[i] = texCount;
					idPositionMapping.put(id, texCount);
					texCount += 1;
				}
			}

			texturesMapping[texturesMapping.length - 1] = texCount;

			texturesForView = new int[texCount + 1];
			callback.glGenTextures(texCount + 1, texturesForView, 0);

			verifyVisibleRect();
			new LoadTexturesThread().start();
		}

		public void onSurfaceChanged(GL10 gl10, int w, int h) {
			log.config("onSurfaceChanged w = " + w + " h = " + h);
			gl10.glViewport(0, 0, w, h);
			gl10.glMatrixMode(GL10.GL_PROJECTION);
			gl10.glLoadIdentity();
			gl10.glFrustumf(PROJECTION_LEFT, PROJECTION_RIGHT, PROJECTION_BOTTOM, PROJECTION_TOP, PROJECTION_NEAR, PROJECTION_FAR);
		}

		public void onDrawFrame(GL10 gl10) {
			if (!texturesLoaded) return;

			if (texturesForView == null || texturesForView.length == 0) return;


			gl10.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
			gl10.glDisable(GL10.GL_DEPTH_TEST);

			/*
			 * Usually, the first thing one might want to do is to clear
			 * the screen. The most efficient way of doing this is to use
			 * glClear().
			 */

			gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			/*
			 * Now we're ready to draw some 3D objects
			 */

			gl10.glMatrixMode(GL10.GL_MODELVIEW);
			gl10.glLoadIdentity();

			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			int[] visibleViews;

			synchronized (visibleViewsMutex) {
				visibleViews = GLGallery.this.visibleViews;
			}

			gl10.glDisable(GL10.GL_LIGHTING);

			gl10.glBindTexture(GL10.GL_TEXTURE_2D, texturesForView[texturesForView.length - 1]);
			gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			gl10.glLoadIdentity();
			gl10.glTranslatef(0,0,-1);
			glViewPainter.draw(gl10);

			gl10.glEnable(GL10.GL_LIGHTING);
			gl10.glLightModelx(GL10.GL_LIGHT_MODEL_TWO_SIDE, GL10.GL_TRUE);
			for (int i = 0; i < visibleViews.length; i ++) {
				gl10.glBindTexture(GL10.GL_TEXTURE_2D, texturesForView[texturesMapping[visibleViews[i]]]);
				gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
				gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

				int globalIndex = visibleViews[i];
				Point center = getCenterForView(visibleViews, i);

				float[] forView = adapter.getTransformForView(globalIndex, center.x, center.y);

				gl10.glLoadMatrixf(forView, 0);

				float alpha = adapter.getAlphaForView(globalIndex, center.x, center.y);

				ByteBuffer ebb = ByteBuffer.allocateDirect(4 * 4);
				ebb.order(ByteOrder.nativeOrder());
				FloatBuffer emissionBuff = ebb.asFloatBuffer();


				emissionBuff.put(new float[]{alpha, alpha, alpha, 1 - alpha});

				emissionBuff.position(0);

				ByteBuffer abb = ByteBuffer.allocateDirect(4 * 4);
				abb.order(ByteOrder.nativeOrder());
				FloatBuffer alphaBuff = abb.asFloatBuffer();

				alphaBuff.put(new float[]{1f, 1f, 1f, alpha});

				alphaBuff.position(0);

				gl10.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, emissionBuff);
				gl10.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, alphaBuff);


				glViewPainter.draw(gl10);
			}
		}

	}
}
