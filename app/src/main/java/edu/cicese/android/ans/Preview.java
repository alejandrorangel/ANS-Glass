package edu.cicese.android.ans;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

//import android.util.Log;

class Preview extends SurfaceView implements SurfaceHolder.Callback { // <1>
//	private static final String TAG = "Preview";

	SurfaceHolder mHolder;
	public byte[] imageData;
	public Camera camera;
	public Camera.Parameters parameters;

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		
		
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


	}

	// Called once the holder is ready
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();
		
		
		parameters = camera.getParameters();
		
		/*List<Camera.Size> picSizes = parameters.getSupportedPictureSizes();
		  List<Camera.Size> previewSizes =
		  parameters.getSupportedPreviewSizes();
		  
		  System.out.println("PictureSizes"); for (Camera.Size size : picSizes)
		  { System.out.println(size.width + "x" + size.height); }
		  System.out.println("PreviewSizes"); for (Camera.Size size :
		  previewSizes) { System.out.println(size.width + "x" + size.height); }*/

		/*List<String> flashmodes = parameters.getSupportedFlashModes();
		List<String> focusmodes = parameters.getSupportedFocusModes();
		List<String> scenemodes = parameters.getSupportedSceneModes();

		System.out.println("flashmodes"); for (String string : flashmodes) { 
			System.out.println(string);
		}
		System.out.println("focusmodes"); for (String string : focusmodes) {
			System.out.println(string);
		}
		System.out.println("scenemodes"); for (String string : scenemodes) {
			System.out.println(string);
		}*/


		/*//G2
		parameters.setPictureSize(640, 480);
		parameters.setPreviewSize(800, 480);*/
		/*//Nexus S
		parameters.setPictureSize(640, 480);
		parameters.setPreviewSize(720, 480);*/

		parameters.setPictureSize(640, 480);
		parameters.setPreviewSize(Utilities.PREVIEW_WIDTH, Utilities.PREVIEW_HEIGHT);
		parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewFpsRange(30000,30000);
		parameters.setJpegQuality(100);

		camera.setParameters(parameters);
		
		try {
			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new PreviewCallback() {
				// Called for each frame previewed
				public void onPreviewFrame(byte[] data, Camera camera) { // <11>
					// Log.d(TAG, "onPreviewFrame called at: " +
					// System.currentTimeMillis());
					imageData = data;
			//		Preview.this.invalidate(); // <12>
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Called when the holder is destroyed
	public void surfaceDestroyed(SurfaceHolder holder) { // <14>
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		// camera = null;
	}

	// Called when holder has changed
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { // <15>
/*		parameters = camera.getParameters();
		parameters.setPictureSize(640, 480);
		// parameters.setPreviewSize(320, 240);
		parameters.setPreviewSize(640, 480);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setJpegQuality(100);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // FOCUS_MODE_CONTINUOUS_VIDEO
		// API 9
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF); // TORCH =
		// Always
		// ON.
		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);*/
		
		  /*List<Camera.Size> picSizes = parameters.getSupportedPictureSizes();
		  List<Camera.Size> previewSizes =
		  parameters.getSupportedPreviewSizes();
		  
		  System.out.println("PictureSizes"); for (Camera.Size size : picSizes)
		  { System.out.println(size.width + "x" + size.height); }
		  System.out.println("PreviewSizes"); for (Camera.Size size :
		  previewSizes) { System.out.println(size.width + "x" + size.height); }*/
//		camera.setParameters(parameters);
		camera.startPreview();
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent me) {
//		System.out.println("TOUCH PREVIEW");
//		super.onTouchEvent(me);
////		canvas.drawText(me.getX() + "", 50, 50, new Paint());
//		return true;
//	}
}