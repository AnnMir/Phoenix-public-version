package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.support.annotation.NonNull;
import android.view.Surface;

/**
 * Holds state associated with a Surface used for MediaCodec encoder input.
 * <p>
 * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses
 * that to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to
 * be sent to the video encoder.
 * <p>
 * This object owns the Surface - releasing this will release the Surface too.
 */
class CodecInputSurface {
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay EGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext EGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface EGLSurface = EGL14.EGL_NO_SURFACE;

    private Surface surface;

    /**
     * Creates a CodecInputSurface from a Surface.
     */
    public CodecInputSurface(@NonNull Surface surface) throws OpenGLException {
        if (surface == null) {
            throw new NullPointerException();
        }
        this.surface = surface;

        eglSetup();
    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
     */
    private void eglSetup() throws OpenGLException {
        EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (EGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new OpenGLException(OpenGLException.CAN_NOT_GET_EGL_DISPLAY);
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(EGLDisplay, version, 0, version, 1)) {
            throw new OpenGLException(OpenGLException.EGL_DISPLAY_INIT_ERROR);
        }

        // Configure EGL for recording and OpenGL ES 2.0.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(EGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0);
        checkEglError("eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        EGLContext = EGL14.eglCreateContext(EGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        EGLSurface = EGL14.eglCreateWindowSurface(EGLDisplay, configs[0], surface,
                surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
    }

    /**
     * Discards all resources held by this class, notably the EGL context.  Also releases the
     * Surface that was passed to our constructor.
     */
    public void release() {
        if (EGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(EGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(EGLDisplay, EGLSurface);
            EGL14.eglDestroyContext(EGLDisplay, EGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(EGLDisplay);
        }
        surface.release();

        EGLDisplay = EGL14.EGL_NO_DISPLAY;
        EGLContext = EGL14.EGL_NO_CONTEXT;
        EGLSurface = EGL14.EGL_NO_SURFACE;

        surface = null;
    }

//    /**
//     * Makes our EGL context and surface current.
//     */
//    public void makeCurrent() {
//        EGL14.eglMakeCurrent(EGLDisplay, EGLSurface, EGLSurface, EGLContext);
//        checkEglError("eglMakeCurrent");
//    }
//
//    /**
//     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
//     */
//    public boolean swapBuffers() {
//        boolean result = EGL14.eglSwapBuffers(EGLDisplay, EGLSurface);
//        checkEglError("eglSwapBuffers");
//        return result;
//    }
//
//    /**
//     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
//     */
//    public void setPresentationTime(long nsecs) {
//        EGLExt.eglPresentationTimeANDROID(EGLDisplay, EGLSurface, nsecs);
//        checkEglError("eglPresentationTimeANDROID");
//    }
//
    /**
     * Checks for EGL errors.  Throws an exception if one is found.
     */
    private void checkEglError(String message) throws OpenGLException {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new OpenGLException(error, message);
        }
    }
}
