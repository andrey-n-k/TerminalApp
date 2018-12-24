/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.circlelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Grafeev
 */
public class CircleLayout extends ViewGroup {

	private static final int RADIUS_UNSPECIFIED = -1;

	protected int radius = RADIUS_UNSPECIFIED;

	public CircleLayout(Context context) {
		super(context);
	}

	public CircleLayout(Context context, int radius) {
		super(context);
		this.radius = radius;
	}

    public CircleLayout(Context context, AttributeSet attrs) {
	    super(context, attrs);
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec) , MeasureSpec.getSize(heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int width = right - left - getPaddingRight() - getPaddingLeft();
		int height = bottom - top - getPaddingBottom() - getPaddingTop();
		int maxChildWidth = 0;
		int maxChildHeight = 0;
		int childsCount = getChildCount();
		for (int i = 0; i < childsCount; ++i) {
			View child = getChildAt(i);
			int childMesureWidth = child.getLayoutParams().width > 0 ? MeasureSpec.makeMeasureSpec(child.getLayoutParams().width, MeasureSpec.EXACTLY) : MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
			int childMesureHeight = child.getLayoutParams().height > 0 ? MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY) : MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
			child.measure(childMesureWidth, childMesureHeight);
			if (maxChildWidth < child.getMeasuredWidth())
				maxChildWidth = child.getMeasuredWidth();
			if (maxChildHeight < child.getMeasuredHeight())
				maxChildHeight = child.getMeasuredHeight();
		}
		if (radius == RADIUS_UNSPECIFIED) {
			radius = Math.max(Math.min(((width / 2) - maxChildWidth), ((height / 2) - maxChildHeight)), 0);
		}

		double angle = -Math.PI / 2;
		switch (childsCount) {
			case 1:
				angle = width > height ? -Math.PI / 2 : 0;
				break;
			case 2:
				angle = width > height ? -Math.PI / 2 : 0;
				break;
			case 4:
				angle = -Math.PI / 4;
				break;
		}
		int centerX = width / 2;
		int centerY = height / 2;

		for (int i = 0; i < childsCount; ++i) {
			View child = getChildAt(i);
			int childRadius;
			if (!(child.getLayoutParams() instanceof LayoutParams) || (childRadius = ((LayoutParams) child.getLayoutParams()).radius) == RADIUS_UNSPECIFIED) {
				childRadius = radius;
			}
			int childCenterX = (int)(childRadius * Math.cos(angle) + 0.5) + centerX;
			int childCenterY = (int)(childRadius * Math.sin(angle) + 0.5) + centerY;
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			child.layout(childCenterX - (childWidth / 2) , childCenterY - (childHeight / 2) , childCenterX + (childWidth / 2) , childCenterY + (childHeight / 2));

			angle += (Math.PI * 2) / childsCount;
		}
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getRadius() {
		return radius;
	}

	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

    protected LayoutParams generateDefaultLayoutParams() {
	    return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, radius);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
	    return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
	    return super.checkLayoutParams(p);
    }

    public static class LayoutParams extends android.view.ViewGroup.MarginLayoutParams {
        public int radius = RADIUS_UNSPECIFIED;

        public LayoutParams(android.content.Context c, AttributeSet attrs) {
	        super(c, attrs);
        }

        public LayoutParams(int width, int height) {
	        super(width, height);
        }

        public LayoutParams(int width, int height, int radius) {
	        super(width, height);
	        this.radius = radius;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
	        super(p);
        }

        public LayoutParams(android.view.ViewGroup.MarginLayoutParams source) {
	        super(source);
        }

    }

}
