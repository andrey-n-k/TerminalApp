/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui.openglgallery;

import android.opengl.GLDebugHelper;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.GL;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * An EGL helper class.
 */

class EglHelper {
	private int mDebugFlags;
	private GLSurfaceView.GLWrapper mGLWrapper;
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private EGLConfigChooser mEGLConfigChooser;
	private EGLContextFactory mEGLContextFactory;
	private Logger log = Logger.getLogger(getClass().getName());
	private int mEGLContextClientVersion;

	public EglHelper() {
		if (mEGLConfigChooser == null) {
			mEGLConfigChooser = new SimpleEGLConfigChooser(true);
		}
		if (mEGLContextFactory == null) {
			mEGLContextFactory = new DefaultContextFactory();
		}
		if (mEGLWindowSurfaceFactory == null) {
			mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		}
	}

	public void setEGLContextClientVersion(int mEGLContextClientVersion) {
		this.mEGLContextClientVersion = mEGLContextClientVersion;
	}

	public void setEGLConfigChooser(boolean needDepth) {
		setEGLConfigChooser(new EglHelper.SimpleEGLConfigChooser(needDepth));
	}

	public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
		setEGLConfigChooser(new EglHelper.ComponentSizeChooser(redSize, greenSize,
				blueSize, alphaSize, depthSize, stencilSize));
	}

	public void setEGLConfigChooser(EGLConfigChooser mEGLConfigChooser) {
		this.mEGLConfigChooser = mEGLConfigChooser;
	}

	public void setDebugFlags(int mDebugFlags) {
		this.mDebugFlags = mDebugFlags;
	}

	public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory mEGLWindowSurfaceFactory) {
		this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
	}

	public void setEGLContextFactory(EGLContextFactory mEGLContextFactory) {
		this.mEGLContextFactory = mEGLContextFactory;
	}

	public void setGLWrapper(GLSurfaceView.GLWrapper mGLWrapper) {
		this.mGLWrapper = mGLWrapper;
	}

	/**
	 * Initialize EGL for a given configuration spec.
	 */
	public void start() {
		log.warning("start() tid=" + Thread.currentThread().getId());
		/*
					 * Get an EGL instance
					 */
		mEgl = (EGL10) EGLContext.getEGL();

		/*
		 * Get to the default display.
		 */
		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
			throw new RuntimeException("eglGetDisplay failed");
		}

		/*
		 * We can now initialize EGL for that display
		 */
		int[] version = new int[2];
		if(!mEgl.eglInitialize(mEglDisplay, version)) {
			throw new RuntimeException("eglInitialize failed");
		}
		mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

		/*
		* Create an EGL context. We want to do this as rarely as we can, because an
		* EGL context is a somewhat heavy object.
		*/
		mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
		if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
			mEglContext = null;
			throwEglException("createContext");
		}
		log.warning("createContext " + mEglContext + " tid=" + Thread.currentThread().getId());

		mEglSurface = null;
	}

	/*
	 * React to the creation of a new surface by creating and returning an
	 * OpenGL interface that renders to that surface.
	 */
	public GL createSurface(SurfaceHolder holder) {
		log.warning("createSurface()  tid=" + Thread.currentThread().getId());
		/*
					 * Check preconditions.
					 */
		if (mEgl == null) {
			throw new RuntimeException("egl not initialized");
		}
		if (mEglDisplay == null) {
			throw new RuntimeException("eglDisplay not initialized");
		}
		if (mEglConfig == null) {
			throw new RuntimeException("mEglConfig not initialized");
		}
		/*
		 *  The window size has changed, so we need to create a new
		 *  surface.
		 */
		if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {

			/*
			 * Unbind and destroy the old EGL surface, if
			 * there is one.
			 */
			mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
		}

		/*
		 * Create an EGL surface we can render into.
		 */
		mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl,
				mEglDisplay, mEglConfig, holder);

		if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
			int error = mEgl.eglGetError();
			if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
				log.severe("createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
				return null;
			}
			throwEglException("createWindowSurface", error);
		}

		/*
		 * Before we can issue GL commands, we need to make sure
		 * the context is current and bound to a surface.
		 */
		if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
			throwEglException("eglMakeCurrent");
		}

		GL gl = mEglContext.getGL();
		if (mGLWrapper != null) {
			gl = mGLWrapper.wrap(gl);
		}

		if ((mDebugFlags & (GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS)) != 0) {
			int configFlags = 0;
			Writer log = null;
			if ((mDebugFlags & GLSurfaceView.DEBUG_CHECK_GL_ERROR) != 0) {
				configFlags |= GLDebugHelper.CONFIG_CHECK_GL_ERROR;
			}
			if ((mDebugFlags & GLSurfaceView.DEBUG_LOG_GL_CALLS) != 0) {
				log = new GLSurfaceView.LogWriter();
			}
			gl = GLDebugHelper.wrap(gl, configFlags, log);
		}
		return gl;
	}

	/**
	 * Display the current render surface.
	 * @return false if the context has been lost.
	 */
	public boolean swap() {
		if (! mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {

			/*
			 * Check for EGL_CONTEXT_LOST, which means the context
			 * and all associated data were lost (For instance because
			 * the device went to sleep). We need to sleep until we
			 * get a new surface.
			 */
			int error = mEgl.eglGetError();
			switch(error) {
			case EGL11.EGL_CONTEXT_LOST:
				return false;
			case EGL10.EGL_BAD_NATIVE_WINDOW:
				// The native window is bad, probably because the
				// window manager has closed it. Ignore this error,
				// on the expectation that the application will be closed soon.
				log.severe("eglSwapBuffers returned EGL_BAD_NATIVE_WINDOW. tid=" + Thread.currentThread().getId());
				break;
			default:
				throwEglException("eglSwapBuffers", error);
			}
		}
		return true;
	}

	public void destroySurface() {
		log.warning("destroySurface()  tid=" + Thread.currentThread().getId());
		if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
			mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_SURFACE,
					EGL10.EGL_NO_CONTEXT);
			mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
			mEglSurface = null;
		}
	}

	public void finish() {
		log.warning("finish() tid=" + Thread.currentThread().getId());
		if (mEglContext != null) {
			mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
			mEglContext = null;
		}
		if (mEglDisplay != null) {
			mEgl.eglTerminate(mEglDisplay);
			mEglDisplay = null;
		}
	}

	private void throwEglException(String function) {
		throwEglException(function, mEgl.eglGetError());
	}

	private void throwEglException(String function, int error) {
		String message = function + " failed";
		log.severe("throwEglException tid=" + Thread.currentThread().getId() + " " + message);
		throw new RuntimeException(message);
	}

	EGL10 mEgl;
	EGLDisplay mEglDisplay;
	EGLSurface mEglSurface;
	EGLConfig mEglConfig;
	EGLContext mEglContext;

	/**
	 * An interface for choosing an EGLConfig configuration from a list of
	 * potential configurations.
	 * <p>
	 * This interface must be implemented by clients wishing to call
	 * {@link com.arellomobile.android.libs.ui.openglgallery.GLSurfaceView#setEGLConfigChooser(EGLConfigChooser)}
	 */
	public interface EGLConfigChooser {
		/**
		 * Choose a configuration from the list. Implementors typically
		 * implement this method by calling
		 * {@link javax.microedition.khronos.egl.EGL10#eglChooseConfig} and iterating through the results. Please consult the
		 * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
		 * @param egl the EGL10 for the current display.
		 * @param display the current display.
		 * @return the chosen configuration.
		 */
		EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
	}

	private abstract class BaseConfigChooser
			implements EGLConfigChooser {


		public BaseConfigChooser(int[] configSpec) {
			mConfigSpec = filterConfigSpec(configSpec);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			int[] num_config = new int[1];
			if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
					num_config)) {
				throw new IllegalArgumentException("eglChooseConfig failed");
			}

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}

			EGLConfig[] configs = new EGLConfig[numConfigs];
			if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
					num_config)) {
				throw new IllegalArgumentException("eglChooseConfig#2 failed");
			}
			EGLConfig config = chooseConfig(egl, display, configs);
			if (config == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return config;
		}

		abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs);

		protected int[] mConfigSpec;

		private int[] filterConfigSpec(int[] configSpec) {
			if (mEGLContextClientVersion != 2) {
				return configSpec;
			}
			/* We know none of the subclasses define EGL_RENDERABLE_TYPE.
			 * And we know the configSpec is well formed.
			 */
			int len = configSpec.length;
			int[] newConfigSpec = new int[len + 2];
			System.arraycopy(configSpec, 0, newConfigSpec, 0, len-1);
			newConfigSpec[len-1] = EGL10.EGL_RENDERABLE_TYPE;
			newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
			newConfigSpec[len+1] = EGL10.EGL_NONE;
			return newConfigSpec;
		}
	}

	/**
	 * Choose a configuration with exactly the specified r,g,b,a sizes,
	 * and at least the specified depth and stencil sizes.
	 */
	private class ComponentSizeChooser extends BaseConfigChooser {
		public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
				int alphaSize, int depthSize, int stencilSize) {
			super(new int[] {
					EGL10.EGL_RED_SIZE, redSize,
					EGL10.EGL_GREEN_SIZE, greenSize,
					EGL10.EGL_BLUE_SIZE, blueSize,
					EGL10.EGL_ALPHA_SIZE, alphaSize,
					EGL10.EGL_DEPTH_SIZE, depthSize,
					EGL10.EGL_STENCIL_SIZE, stencilSize,
					EGL10.EGL_NONE});
			mValue = new int[1];
			mRedSize = redSize;
			mGreenSize = greenSize;
			mBlueSize = blueSize;
			mAlphaSize = alphaSize;
			mDepthSize = depthSize;
			mStencilSize = stencilSize;
	   }

		@Override
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);
				if ((d >= mDepthSize) && (s >= mStencilSize)) {
					int r = findConfigAttrib(egl, display, config,
							EGL10.EGL_RED_SIZE, 0);
					int g = findConfigAttrib(egl, display, config,
							 EGL10.EGL_GREEN_SIZE, 0);
					int b = findConfigAttrib(egl, display, config,
							  EGL10.EGL_BLUE_SIZE, 0);
					int a = findConfigAttrib(egl, display, config,
							EGL10.EGL_ALPHA_SIZE, 0);
					if ((r == mRedSize) && (g == mGreenSize)
							&& (b == mBlueSize) && (a == mAlphaSize)) {
						return config;
					}
				}
			}
			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}

		private int[] mValue;
		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		}

	/**
	 * This class will choose a RGB_565 surface with
	 * or without a depth buffer.
	 *
	 */
	private class SimpleEGLConfigChooser extends ComponentSizeChooser {
		public SimpleEGLConfigChooser(boolean withDepthBuffer) {
			super(5, 6, 5, 0, withDepthBuffer ? 16 : 0, 0);
		}
	}

	/**
	 * An interface for customizing the eglCreateContext and eglDestroyContext calls.
	 * <p>
	 * This interface must be implemented by clients wishing to call
	 * {@link com.arellomobile.android.libs.ui.openglgallery.GLSurfaceView#setEGLContextFactory(EGLContextFactory)}
	 */
	public interface EGLContextFactory {
		EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);
		void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
	}

	private class DefaultContextFactory implements EGLContextFactory {
		private Logger log = Logger.getLogger(getClass().getName());

		private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		private DefaultContextFactory() {
		}

		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
			int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
					EGL10.EGL_NONE };

			return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
					mEGLContextClientVersion != 0 ? attrib_list : null);
		}

		public void destroyContext(EGL10 egl, EGLDisplay display,
				EGLContext context) {
			if (!egl.eglDestroyContext(display, context)) {
				log.severe("egl error: " + egl.eglGetError());
				log.severe("display: " + display + " context: " + context + " tid: " + Thread.currentThread().getId());
				//throw new RuntimeException("eglDestroyContext failed");
			}
		}
	}

	/**
	 * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
	 * <p>
	 * This interface must be implemented by clients wishing to call
	 * {@link com.arellomobile.android.libs.ui.openglgallery.GLSurfaceView#setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory)}
	 */
	public interface EGLWindowSurfaceFactory {
		EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
				Object nativeWindow);
		void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
	}

	private class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

		public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
				EGLConfig config, Object nativeWindow) {
			return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
		}

		public void destroySurface(EGL10 egl, EGLDisplay display,
				EGLSurface surface) {
			egl.eglDestroySurface(display, surface);
		}
	}
}
