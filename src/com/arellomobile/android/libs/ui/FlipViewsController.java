/*
 * Arello Mobile Mobile Framework Except where otherwise noted, this work is
 * licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui;

import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Helper class to switch views by 3D rotation.
 * 
 * @author Swift
 */
public class FlipViewsController implements Animation.AnimationListener
{

	Activity callback;
	View fromView;
	View toView;
	ViewGroup container;
	int duration;
	Mode mode;
	final Object startedAnimationsMutex = new Object();
	int startedAnimations = 2;
	int stoppedAnimations = 2;
	Listener animationListener;

	/**
	 * creates controller with parameters
	 * 
	 * @param callback
	 *            context for work with gui functions
	 * @param container
	 *            container of views
	 * @param fromView
	 *            view to hide
	 * @param toView
	 *            view to show
	 * @param mode
	 *            mode of 3D rotation
	 * @param duration
	 *            time of animation
	 */
	public FlipViewsController(Activity callback, ViewGroup container, View fromView, View toView, Mode mode, int duration)
	{
		this.callback = callback;
		this.container = container;
		this.duration = duration;
		this.fromView = fromView;
		this.mode = mode;
		this.toView = toView;
	}

	/**
	 * set listener object for view
	 * 
	 * @param animationListener
	 *            new listener
	 */
	public void setAnimationListener(Listener animationListener)
	{
		this.animationListener = animationListener;
	}

	/**
	 * starts animated change
	 */
	public void start()
	{
		if (startedAnimations != stoppedAnimations || startedAnimations != 2)
		{
			return;
		}
		startedAnimations = 0;
		stoppedAnimations = 0;
		FlipAnimation outAnimation = null;
		FlipAnimation inAnimation = null;

		switch (mode)
		{
		case bottomTop:
			outAnimation = new FlipAnimation(0, -90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, true);
			inAnimation = new FlipAnimation(90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, true);
			break;
		case leftRight:
			outAnimation = new FlipAnimation(0, 90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, false);
			inAnimation = new FlipAnimation(-90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, false);
			break;
		case rightLeft:
			outAnimation = new FlipAnimation(0, -90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, false);
			inAnimation = new FlipAnimation(90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, false);
			break;
		case topBottom:
			outAnimation = new FlipAnimation(0, 90, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, true, true);
			inAnimation = new FlipAnimation(-90, 0, fromView.getWidth() / 2, fromView.getHeight() / 2, 200, false, true);
			break;
		}

		outAnimation.setDuration(duration / 2);
		outAnimation.setInterpolator(new AccelerateInterpolator());
		outAnimation.setFillAfter(true);
		outAnimation.setAnimationListener(this);
		fromView.setAnimation(outAnimation);

		inAnimation.setDuration(duration / 2);
		inAnimation.setInterpolator(new DecelerateInterpolator());
		inAnimation.setFillAfter(true);
		inAnimation.setAnimationListener(this);
		inAnimation.setStartOffset(duration / 2);

		AnimationSet inAnimationSet = new AnimationSet(false);
		inAnimationSet.addAnimation(inAnimation);

		fromView.setAnimation(outAnimation);
		toView.setAnimation(inAnimationSet);

		callback.runOnUiThread(new Runnable()
		{
			public void run()
			{
				toView.setVisibility(View.VISIBLE);
				fromView.setVisibility(View.INVISIBLE);
/*				int index = container.indexOfChild(fromView);
				if (index > -1)
				{
					container.removeViewAt(index);
				}
				container.addView(toView, index);
*/			}
		});
	}

	public void onAnimationStart(Animation animation)
	{
		synchronized (startedAnimationsMutex)
		{
			startedAnimations++;
		}
	}

	public void onAnimationEnd(Animation animation)
	{
		boolean isEnd;
		synchronized (startedAnimationsMutex)
		{
			stoppedAnimations++;
			isEnd = startedAnimations == stoppedAnimations && startedAnimations == 2;
		}
		if (isEnd && animationListener != null)
		{
			animationListener.flipEnd();

			fromView.setAnimation(null);
			toView.setAnimation(null);
		}
	}

	public void onAnimationRepeat(Animation animation)
	{
	}

	class FlipAnimation extends Animation
	{
		private final double fromDegrees;
		private final double toDegrees;
		private final double centerX;
		private final double centerY;
		private final double depthZ;
		private final boolean reverse;
		private boolean horizontal;
		private Camera camera;

		public FlipAnimation(float fromDegrees, float toDegrees, float centerX, float centerY, float depthZ, boolean reverse, boolean horizontal)
		{
			this.fromDegrees = fromDegrees;
			this.toDegrees = toDegrees;
			this.centerX = centerX;
			this.centerY = centerY;
			this.depthZ = depthZ;
			this.reverse = reverse;
			this.horizontal = horizontal;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight)
		{
			super.initialize(width, height, parentWidth, parentHeight);
			camera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t)
		{
			double degrees = fromDegrees + ((toDegrees - fromDegrees) * interpolatedTime);

			final Matrix matrix = t.getMatrix();

			camera.save();
			if (reverse)
			{
				camera.translate(0.0f, 0.0f, (float) (depthZ * interpolatedTime));
//				camera.translate(0.0f, 0.0f, 0 * interpolatedTime);
			}
			else
			{
				camera.translate(0.0f, 0.0f, (float) (depthZ * (1.0d - interpolatedTime)));
//				camera.translate(0.0f, 0.0f, 0 * (1.0f - interpolatedTime));
			}
			if (horizontal)
			{
				camera.rotateX((float) degrees);
			}
			else
			{
				camera.rotateY((float) degrees);
			}
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate((float) -centerX, (float) -centerY);
			matrix.postTranslate((float) centerX, (float) centerY);
		}
	}

	/**
	 * Animation modes for {@link FlipViewsController}
	 */
	public static enum Mode
	{
		leftRight, rightLeft, topBottom, bottomTop
	}

	/**
	 * Listener interface to receive end of animation
	 */
	public static interface Listener
	{
		public void flipEnd();
	}

}
