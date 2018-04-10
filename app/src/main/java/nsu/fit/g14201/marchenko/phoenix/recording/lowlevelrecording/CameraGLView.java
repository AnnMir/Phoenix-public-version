package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Sub class of GLSurfaceView to display camera preview and write video frame to capturing surface
 */
public class CameraGLView extends GLSurfaceView {
    private final CameraSurfaceRenderer renderer;

    private static final boolean VERBOSE = true;

    public CameraGLView(Context context) {
        this(context, null, 0);
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraGLView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        renderer = new CameraSurfaceRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
		// the frequency of refreshing of camera preview is at most 15 fps
		// and RENDERMODE_WHEN_DIRTY is better to reduce power consumption
//		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private static final class CameraSurfaceRenderer implements GLSurfaceView.Renderer {
        private final WeakReference<CameraGLView> surface;
        private final float[] mvpMatrix = new float[16];

        CameraSurfaceRenderer(CameraGLView surface) {
            this.surface = new WeakReference<>(surface);
            Matrix.setIdentityM(mvpMatrix, 0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // This renderer required OES_EGL_image_external extension
            String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);	// API >= 8
            if (!extensions.contains("OES_EGL_image_external"))
                throw new RuntimeException("This system does not support OES_EGL_image_external.");
//            // create textur ID
//            hTex = GLDrawer2D.initTex();
//            // create SurfaceTexture with texture ID.
//            mSTexture = new SurfaceTexture(hTex);
//            mSTexture.setOnFrameAvailableListener(this);
//            // clear screen with yellow color so that you can see rendering rectangle
//            GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
//            final CameraGLView parent = mWeakParent.get();
//            if (parent != null) {
//                parent.mHasSurface = true;
//            }
//            // create object for preview display
//            mDrawer = new GLDrawer2D();
//            mDrawer.setMatrix(mMvpMatrix, 0);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }
    }
}
