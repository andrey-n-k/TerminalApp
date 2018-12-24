/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Camera view class
 *
 * @author Swift
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder holder;
    Camera camera;

	CameraView(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
        try {
			camera = Camera.open();
    	    camera.setPreviewDisplay(holder);
        } catch (Exception exception) {
			if (camera != null){
				camera.release();
			}
            camera = null;
            // TODO: add more exception handling logic here
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera == null) return;
		
        camera.stopPreview();
		camera.release();
        camera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (camera == null) return;
        /*Camera.Parameters parameters = camera.getParameters();

//		Camera.Size currentSize = null;
//
//		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//
//		for (Camera.Size size : sizes) {
//			if (currentSize == null || size.width < currentSize.width){
//				if (w < size.width && h < size.height) {
//					currentSize = size;
//				}
//			}
//		}
//
//		if (currentSize != null) {
//			parameters.setPreviewSize(currentSize.width, currentSize.height);
//		}
        camera.setParameters(parameters);*/
        camera.startPreview();
    }
}
