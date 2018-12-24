package com.arellomobile.android.libs.ui.optimized_gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Date: 10.01.12
 * Time: 11:08
 *
 * @author MiG35
 */
public class IPhoneStyleOptimizedGallery extends OptimizedGallery
{
    private boolean mIsInMove;

    public IPhoneStyleOptimizedGallery(Context context)
    {
        this(context, null);
    }

    public IPhoneStyleOptimizedGallery(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public IPhoneStyleOptimizedGallery(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * HACK: Prevents drop and gallery offset from being reset when Child View content is changed
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // Do not do anything if user is draggin the gallery
        if (mIsInMove)
        {
            return;
        }
        super.onLayout(changed, l, t, r, b);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Detects content dragging
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            mIsInMove = true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
        {
            mIsInMove = false;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL)
        {
            return false;
        }
        return super.onTouchEvent(event);
    }

    /**
     * HACK: sending keyboard message to perform animated scrolling
     */
    public void next()
    {
        onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
        onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
    }

    /**
     * HACK: sending keyboard message to perform animated scrolling
     */
    public void previous()
    {
        onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
        onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
    }

    /**
     * HACK: replacing onFling to prev/next
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        if (velocityX < 0)
        {
            next();
            return true;
        }
        else if (velocityX > 0)
        {
            previous();
            return true;
        }
        return false;
    }
}
