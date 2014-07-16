package edu.cicese.android.ans;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.ans.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import java.awt.Color;

public class ANS extends Activity {

	private static final String TAG = "AmbientNotificationSystem";
	public static Context context;
//	private static Preview preview;
//	private static TextView lblServer;

	private Server serverThread;
	private TQueries queryThread;
	private TRepository repositoryThread;

	private TVibratingNotification vibratingNotificationThread;
	private TAudioNotification audioNotificationThread;
	private MediaPlayer mediaPlayer;

	private Dialog tagFoundDialog, imageDialog;
	private JSONObject joArea;
	private JSONArray jaFPS;
	private float x1, y1, x2, y2;

	private String tagID, spaceID;

	private static DrawOnTop mDraw;
	private static DrawCapture mCapture;
	private static Preview mPreview;

	public static String img;
	
	final private Handler messageHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {  
	    	showMessage(msg.getData());
//	    	showTag(msg.getData());
	    }
	};

	private Handler dialogHandler = new Handler() {
		@Override
	    public void handleMessage(Message msg) {
	    	closeDialog();
	    }
	};
	
//	final private Handler errorHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			showError(msg.getData().getString("msg"));
//		}
//	};
	  
	
//	private OrientationEventListener orientationEventListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// no display timeout
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
//		setContentView(R.layout.main);

		try {
			Utilities.sdcard = Environment.getExternalStorageDirectory().getCanonicalPath();
		} catch (IOException e) { e.printStackTrace(); }

		// fullscreen application
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// check working directories
		Utilities.checkDirs();

//		preview = (Preview) findViewById(R.id.lyPreview);
//		preview = new Preview(this);
//		((FrameLayout) findViewById(R.id.lyPreview)).addView(preview);
//		SurfaceHolder mSurfaceHolder = preview.getHolder();
//		mSurfaceHolder.addCallback(this);
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


//		FrameLayout mOverlay = (FrameLayout) findViewById(R.id.lyOverlay);
//				new Preview(this);
//		((FrameLayout) findViewById(R.id.lyOverlay)).addView(mOverlay);
//		mOverlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		mPreview = new Preview(this);
//
//		SurfaceHolder mHolder = mPreview.getHolder();
//
//		mHolder.setFixedSize(getWindow().getWindowManager()
//                .getDefaultDisplay().getWidth(), getWindow().getWindowManager()
//                .getDefaultDisplay().getHeight());

        setContentView(mPreview);

		mDraw = new DrawOnTop(this);
        addContentView(mDraw, new ViewGroup.LayoutParams
		(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		mCapture = new DrawCapture(this);
        addContentView(mCapture, new ViewGroup.LayoutParams
		(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//		mPreview.setOnTouchListener(new View.OnTouchListener() {
//			public boolean onTouch(View v, MotionEvent event) {
//				System.out.println("TOUCH Preview");
//				return false;
//			}
//		});
//		mDraw.setOnTouchListener(new View.OnTouchListener() {
//			public boolean onTouch(View v, MotionEvent event) {
//				System.out.println("TOUCH Overlay");
//				return false;
//			}
//		});

		Utilities.SERVER_ADDR = getServerAddress();

		// server incoming requests Thread
		serverThread = new Server(messageHandler/*, tagThread*/);
		serverThread.start();

		// query Thread
		queryThread = new TQueries(messageHandler);
		queryThread.start();

		// Repository updating Thread
		repositoryThread = new TRepository(messageHandler);
		repositoryThread.start();

		mediaPlayer = MediaPlayer.create(this, R.raw.tagalert);
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			img = "img03.jpg";
			mCapture.handler.sendMessage(new Message());
			displayDialog("001", "04");
			return false;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			img = "img02.jpg";
			mCapture.handler.sendMessage(new Message());
			displayDialog("002", "04");
			return false;
		}
		//mCapture.handler.sendMessage(new Message());
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public void onStop() {
		super.onStop();
		try {
			queryThread.done();
//			tagThread.done();
//			locationThread.done();
			repositoryThread.done();
			Utilities.serverBusy = false;
			Utilities.checkingRep = false;
//			Utilities.findingTags = false;
//			Utilities.locatingUser = false;

			if (!Utilities.menuClicked) {
				System.out.println("ONSTOP! !Utilities.menuClicked");
				System.exit(0);
			}
			/*else {
				System.out.println("ONSTOP! now resume..");
				Utilities.menuClicked = false;
			}*/
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}

//		wifiThread.done();
//		try {
//			unregisterReceiver(receiver);
//		}
//		catch (RuntimeException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (Utilities.menuClicked) {
			try {
				// Query Thread
//				Utilities.serverBusy = false;
				queryThread = new TQueries(messageHandler);
				queryThread.start();

				/*// Tag requesting Thread
				tagThread = new TTag(messageHandler);
				tagThread.start();

				// Location requesting Thread
				locationThread = new TLocation();
				locationThread.start();*/

				// Repository updating Thread
				repositoryThread = new TRepository(messageHandler);
				repositoryThread.start();
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}

			System.out.println("now resume..");
			Utilities.menuClicked = false;
		}

//		tagFoundDialog = new Dialog(this);
//		if (wifiThread.isDone()) {
//			startWifiThread();
//		}
	}

	public static void query() {
		Utilities.serverBusy = true;
		Log.d("COMMAND", "[" + Command.QUERY + "] Query.");
		capturePicture("img01.jpg");
		mCapture.handler.sendMessage(new Message());
		Server.sendQuery();
	}

	//! Shows a message toast
	public void showMessage(Bundle bundle) {
		switch (bundle.getInt("type")) {
			case Command.MSG_TAG:
				showTag(bundle);
				break;
			case Command.MSG_INFO:
				showInfo(bundle.getString("msg"));
				break;
		}
	}
	
	//! Shows the tag annotation found
	private void showTag(Bundle bundle) {
		boolean tagFound = !bundle.getString("tagID").equals("000");
		if (tagFound) {
			Utilities.displayingToast = true;
			System.out.println("TAGID " + bundle.getString("tagID"));

			notifyUser(/*Command.TYPE_BOTH*/);
//			SoundManager soundManager = new SoundManager(Utilities.tagDir + "01/002/002.wav");
//			soundManager.start();

			try {
				joArea = (JSONObject) new JSONTokener(bundle.getString("area")).nextValue();
				jaFPS = (JSONArray) new JSONTokener(bundle.getString("fps")).nextValue();
			} catch (JSONException e) {
				e.printStackTrace();
			}
//			x1 = bundle.getFloat("x1");
//			y1 = bundle.getFloat("y1");
//			x2 = bundle.getFloat("x2");
//			y2 = bundle.getFloat("y2");


			displayDialog(bundle.getString("tagID"), bundle.getString("spaceID"));
		}


		/*boolean tagFound = !bundle.getString("tagID").equals("000");
		if (tagFound) {
			Utilities.displayingToast = true;

			System.out.println("TAGID " + bundle.getString("tagID"));

			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.toast_tag_layout,
										   (ViewGroup) findViewById(R.id.toastLayout));
			ImageView image = (ImageView) layout.findViewById(R.id.toastImage);
			File imgFile = new File(bundle.getString("imgPath"));
			if (imgFile.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(imgFile.toString());
				image.setImageBitmap(bm);
			}
			else {
				image.setImageResource(R.drawable.annotation);
			}

			TextView text = (TextView) layout.findViewById(R.id.toastText);
			text.setText(bundle.getString("msg"));
		
			Toast toast = new Toast(getApplicationContext());
			toast.setGravity(Gravity.BOTTOM, 0, -20);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();

			notifyUser(Command.TYPE_BOTH);
			SoundManager soundManager = new SoundManager(Utilities.tagDir + "01/006/006.wav");
			soundManager.start();
//			playAudioClip(bundle.getString("tagID"), bundle.getString("spaceID"));

			try {
				Thread.sleep(1000);
				Utilities.displayingToast = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			vibratingNotificationThread =
					new TVibratingNotification((Vibrator) getSystemService(Context.VIBRATOR_SERVICE), 2);
			vibratingNotificationThread.start();
		}*/
	}

	/**
	 * Notifies the user with a brief sound and/or vibration.
	 *
	 * {@code TYPE_SOUND}, {@code TYPE_VIBRATION}, or {@code TYPE_BOTH}
	 */
	private void notifyUser(/*int notificationType*/) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean vibrationPreference = preferences.getBoolean("vibPref", true);
		boolean audiblePreference = preferences.getBoolean("audPref", true);
		if (vibrationPreference) {
//			vibrationThread.start();
			vibratingNotificationThread =
					new TVibratingNotification((Vibrator) getSystemService(Context.VIBRATOR_SERVICE), 1);
			vibratingNotificationThread.start();

			/*// Get instance of Vibrator from current Context
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			// Start immediately, vibrate for 500ms, sleep for 300ms, vibrate for 500ms
			//	v.vibrate(1000);
			long[] pattern = { 0, 500, 300, 500 };
			v.vibrate(pattern, -1);*/
		}
		if (audiblePreference) {
			audioNotificationThread = new TAudioNotification(mediaPlayer);
			audioNotificationThread.start();

			/*MediaPlayer MPX = MediaPlayer.create(this, R.raw.tagalert);
			MPX.start();
			try {
				Thread.sleep(MPX.getDuration());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}
	
	//! Shows an error toast
	private void showInfo(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
//		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.RIGHT, -5, 0);
//		toast
//		toast.setText(msg);
		toast.show();
//		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	//! Capture picture
	public static void capturePicture(String imageName){
/*		FileOutputStream outStream = null;
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;

	        System.out.println("SIZE " + mPreview.imageData.length);

            Bitmap myImage = BitmapFactory.decodeByteArray(mPreview.imageData, 0,
                    mPreview.imageData.length);


            // Write to SD Card
			outStream = new FileOutputStream(Utilities.imageDirectory + imageName);

	        System.out.println(myImage.getHeight());

//            BufferedOutputStream bos = new BufferedOutputStream(outStream);

            myImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

//            bos.flush();
            outStream.close();

        } catch (FileNotFoundException e) {
            Log.d("FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            Log.d("IOException", e.getMessage());
        }*/


		FileOutputStream outStream;
		try {
			// Write to SD Card
			outStream = new FileOutputStream(Utilities.imageDirectory + imageName);

			/*//Nexus S
			YuvImage yuvimage = new YuvImage(mPreview.imageData,
					ImageFormat.NV21, 720, 480, null);*/
			/*//G2
			YuvImage yuvimage = new YuvImage(mPreview.imageData,
					ImageFormat.NV21, 800, 480, null);*/

			YuvImage yuvimage = new YuvImage(mPreview.imageData,
					ImageFormat.NV21, Utilities.PREVIEW_WIDTH, Utilities.PREVIEW_HEIGHT, null);

			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Rect rect = new Rect();
			/*//Nexus S
			rect.set(0, 0, 720, 480);*/
			/*//G2
			rect.set(0, 0, 800, 480);*/

			rect.set(0, 0, Utilities.PREVIEW_WIDTH, Utilities.PREVIEW_HEIGHT);
			yuvimage.compressToJpeg(rect, 95, outStream);

			// outStream.write(preview.imageData);
			outStream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		/* finally {}*/
		
		// preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_menu, menu);
//		Utilities.menuClicked = true;
		return true;
	}

	// This method is called once the menu is selected
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// We have only one menu option
		case R.id.preferences:
			// Launch Preference activity
			Intent i = new Intent(ANS.this, Preferences.class);
			startActivity(i);
			// Some feedback to the user
//			Toast.makeText(AmbientNotificationSystem.this,
//					"Here you can maintain your user credentials.",
//					Toast.LENGTH_LONG).show();
			break;

		}
		return true;
	}

	public String getServerAddress() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		return preferences.getString("serverPref", "n/a");
	}

	//! Updates the server address in the GUI (Remove from final release)
	public static void changeServer(String serverAddr) {
//		lblServer.setText(serverAddr);
		mDraw.invalidate();
	}

	public static void updateLocation(boolean userLocated) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putBoolean("userLocated", userLocated);
		msg.setData(bundle);
		mDraw.handler.sendMessage(msg);
	}

	/**
	 * Displays a custom {@link Dialog} with the tag annotation.
	 * {@code btnAudio} plays the tag audio clip (if exists),
	 * {@code btnTag} displays another {@code Dialog} with the image
	 * of the object of interest, {@code btnOK} closes the {@code Dialog}.
	 *
	 * @param tagID the tag id
	 * @param spaceID the space id
	 */
	private void displayDialog(String tagID, String spaceID) {
		//set up dialog

		this.tagID = tagID;
		this.spaceID = spaceID;

		final TDialog tDialog = new TDialog(dialogHandler);
		tagFoundDialog = new Dialog(this) {
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
//				System.err.println("CODE " + keyCode);
				/*if (keyCode == KeyEvent.KEYCODE_BACK) {
					System.err.println("Esta seguro? " + keyCode);
					return false;
				}*/
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					if (!tDialog.isAlive()) {
						tDialog.start();
					}
					playClip();
					return false;
				}
				return super.onKeyDown(keyCode, event);
			}
		};

		TNotification tNotification = new TNotification(dialogHandler, tDialog);
//		tNotification.start();

		tagFoundDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		tagFoundDialog.setContentView(R.layout.maindialog);
//		tagFoundDialog.setTitle("ETIQUETA ENCONTRADA");
		tagFoundDialog.setCancelable(true);

		//set up text
		TextView text = (TextView) tagFoundDialog.findViewById(R.id.dialogText);
		text.setText(Utilities.getTagAnnotation(tagID, spaceID));

		//set up image view
		ImageView titleImage = (ImageView) tagFoundDialog.findViewById(R.id.dialogImage);
		titleImage.setImageResource(R.drawable.title);



		//set up buttons
		Button btnOK = (Button) tagFoundDialog.findViewById(R.id.btnOK);
		btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.ok));
		btnOK.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) {
				tagFoundDialog.dismiss();
//				tagFoundDialog.closeOptionsMenu();
			}
		});
		Button btnTag = (Button) tagFoundDialog.findViewById(R.id.btnTag);
		btnTag.setBackgroundDrawable(getResources().getDrawable(R.drawable.tag));
		btnTag.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) {
				tagFoundDialog.hide();
				displayImageDialog();
			}
		});
		final String audioFile = Utilities.tagDir + spaceID + "/" + tagID + "/" + tagID + ".wav";
		Button btnAudio = (Button) tagFoundDialog.findViewById(R.id.btnAudioClip);
		btnAudio.setBackgroundDrawable(getResources().getDrawable(R.drawable.audio));
		btnAudio.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) {
				//TODO: Block dialog until the audio clip is finished
				SoundManager soundManager = new SoundManager(audioFile);
				soundManager.start();
			}
		});




		//set up image with object of interest
		ImageView imageInterestObject = (ImageView) tagFoundDialog.findViewById(R.id.dialogImageObject);
		titleImage.setImageResource(R.drawable.title);

		//uncomment for DEMO
//		File imgFile = new File(Utilities.imageDirectory + tagID + ".jpg");
		File imgFile = new File(Utilities.imageDirectory + "img01.jpg");
		if (imgFile.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(imgFile.toString());
			int width = bm.getWidth();
			int height = bm.getHeight();
			int newWidth = 480;
			int newHeight = 288;
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			// create matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(scaleWidth, scaleHeight);

			Bitmap bmTag = Bitmap.createBitmap(newWidth, newHeight, bm.getConfig());
			Canvas canvas = new Canvas(bmTag);
			canvas.drawBitmap(bm, matrix, null);
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.argb(170, 250, 125, 0));

//			//Uncomment for DEMO
//			try {
//				JSONArray ja = new JSONArray(Utilities.loadString(Utilities.imageDirectory + tagID + ".txt"));
//				joArea = ja.getJSONObject(0).getJSONObject("area");
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}


			//draw points
			try {
				int x, y;
				JSONArray jaPoints = joArea.getJSONArray("points");
				JSONObject joPoint;
				for (int i = 0; i < jaPoints.length(); i++) {
					joPoint = jaPoints.getJSONObject(i);
					x = joPoint.getInt("x");
					y = joPoint.getInt("y");
							//fillOval((int)(x * MATCH_SCALE) - 3, (int)(y * MATCH_SCALE) - 3, 6, 6);
					canvas.drawOval(new RectF(
							x * scaleWidth - 6, y * scaleHeight - 6,
							x * scaleWidth + 6, y * scaleHeight + 6
					), paint);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			/*//draw false positives, according to the Scale Consistency Check
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(5);
			paint.setColor(Color.argb(170, 0, 125, 255));
			try {
				int x, y;
//				JSONArray jaPoints = joArea.getJSONArray("fps");
//				System.out.println("FPS " + jaPoints.length());
				JSONObject joPoint;
				for (int i = 0; i < jaFPS.length(); i++) {
					joPoint = jaFPS.getJSONObject(i);
					x = joPoint.getInt("x");
					y = joPoint.getInt("y");
							//fillOval((int)(x * MATCH_SCALE) - 3, (int)(y * MATCH_SCALE) - 3, 6, 6);
					canvas.drawOval(new RectF(
							x * scaleWidth - 6, y * scaleHeight - 6,
							x * scaleWidth + 6, y * scaleHeight + 6
					), paint);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}*/

			//draw area
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(9);
			paint.setColor(Color.argb(50, 0, 0, 255));
			float x1, y1, x2, y2;


			try {
				/*x1 = Float.parseFloat(joArea.getString("x1"));
				y1 = Float.parseFloat(joArea.getString("y1"));
				x2 = Float.parseFloat(joArea.getString("x2"));
				y2 = Float.parseFloat(joArea.getString("y2"));
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4, y1 - (y2 - y1) / 4,
						x2 + (x2 - x1) / 4, y2 + (y2 - y1) / 4), paint
				);

				paint.setStrokeWidth(2);
				paint.setColor(Color.argb(50, 255, 255, 255));
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4 - 5, y1 - (y2 - y1) / 4 - 5,
						x2 + (x2 - x1) / 4 + 5, y2 + (y2 - y1) / 4 + 5), paint
				);
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4 + 5, y1 - (y2 - y1) / 4 + 5,
						x2 + (x2 - x1) / 4 - 5, y2 + (y2 - y1) / 4 - 5), paint
				);*/


				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.argb(50, 0, 0, 255));
				canvas.drawRect(
						Float.parseFloat(joArea.getString("xs1")) - 6,
						Float.parseFloat(joArea.getString("ys1")) - 6,
						Float.parseFloat(joArea.getString("xs2")) + 6,
						Float.parseFloat(joArea.getString("ys2")) + 6,
						paint);
			} catch (JSONException e) {
				e.printStackTrace();
			}

//			canvas.drawRect(x1, y1, x2, y2, paint);

			imageInterestObject.setImageBitmap(bmTag);
		}










		//now that the dialog is set up, it's time to show it
		tagFoundDialog.show();
		tagFoundDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				tDialog.done();
				Utilities.displayingToast = false;
			}
		});
		tagFoundDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				tDialog.done();
				Utilities.displayingToast = false;
			}
		});
	}

	/**
	 * Displays a custom {@link Dialog} the captured image
	 * with the object of interest selected.
	 */
	private void displayImageDialog() {
		// set up dialog
		imageDialog = new Dialog(this);
		imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		imageDialog.setContentView(R.layout.imagedialog);
//		imageDialog.setTitle("OBJETO");
		imageDialog.setCancelable(true);

		// set up image view
		ImageView image = (ImageView) imageDialog.findViewById(R.id.dialogImage);
//		titleImage.setImageResource(R.drawable.annotation);
		File imgFile = new File(Utilities.imageDirectory + "img01.jpg");
		if (imgFile.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(imgFile.toString());
			int width = bm.getWidth();
			int height = bm.getHeight();
			int newWidth = 700;
			int newHeight = 420;
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			// create matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(scaleWidth, scaleHeight);

			Bitmap bmTag = Bitmap.createBitmap(newWidth, newHeight, bm.getConfig());
			Canvas canvas = new Canvas(bmTag);
			canvas.drawBitmap(bm, matrix, null);
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.argb(170, 250, 125, 0));
			//draw points
			try {
				int x, y;
				JSONArray jaPoints = joArea.getJSONArray("points");
				JSONObject joPoint;
				for (int i = 0; i < jaPoints.length(); i++) {
					joPoint = jaPoints.getJSONObject(i);
					x = joPoint.getInt("x");
					y = joPoint.getInt("y");
							//fillOval((int)(x * MATCH_SCALE) - 3, (int)(y * MATCH_SCALE) - 3, 6, 6);
					canvas.drawOval(new RectF(
							x * scaleWidth - 6, y * scaleHeight - 6,
							x * scaleWidth + 6, y * scaleHeight + 6
					), paint);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//draw false positives, according to the Scale Consistency Check
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(5);
			paint.setColor(Color.argb(170, 0, 125, 255));
			try {
				int x, y;
//				JSONArray jaPoints = joArea.getJSONArray("fps");
//				System.out.println("FPS " + jaPoints.length());
				JSONObject joPoint;
				for (int i = 0; i < jaFPS.length(); i++) {
					joPoint = jaFPS.getJSONObject(i);
					x = joPoint.getInt("x");
					y = joPoint.getInt("y");
							//fillOval((int)(x * MATCH_SCALE) - 3, (int)(y * MATCH_SCALE) - 3, 6, 6);
					canvas.drawOval(new RectF(
							x * scaleWidth - 6, y * scaleHeight - 6,
							x * scaleWidth + 6, y * scaleHeight + 6
					), paint);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			//draw area
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(9);
			paint.setColor(Color.argb(50, 0, 0, 255));
			float x1, y1, x2, y2;


			try {
				/*x1 = Float.parseFloat(joArea.getString("x1"));
				y1 = Float.parseFloat(joArea.getString("y1"));
				x2 = Float.parseFloat(joArea.getString("x2"));
				y2 = Float.parseFloat(joArea.getString("y2"));
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4, y1 - (y2 - y1) / 4,
						x2 + (x2 - x1) / 4, y2 + (y2 - y1) / 4), paint
				);

				paint.setStrokeWidth(2);
				paint.setColor(Color.argb(50, 255, 255, 255));
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4 - 5, y1 - (y2 - y1) / 4 - 5,
						x2 + (x2 - x1) / 4 + 5, y2 + (y2 - y1) / 4 + 5), paint
				);
				canvas.drawOval(new RectF(x1 - (x2 - x1) / 4 + 5, y1 - (y2 - y1) / 4 + 5,
						x2 + (x2 - x1) / 4 - 5, y2 + (y2 - y1) / 4 - 5), paint
				);*/
				

				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.argb(50, 0, 0, 255));
				canvas.drawRect(
						Float.parseFloat(joArea.getString("x1")) - 6,
						Float.parseFloat(joArea.getString("y1")) - 6,
						Float.parseFloat(joArea.getString("x2")) + 6,
						Float.parseFloat(joArea.getString("y2")) + 6,
						paint);
			} catch (JSONException e) {
				e.printStackTrace();
			}

//			canvas.drawRect(x1, y1, x2, y2, paint);

			image.setImageBitmap(bmTag);
		}

		image.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				imageDialog.dismiss();
				tagFoundDialog.show();
			}
		});

		//set up button
		/*Button btnImageOK = (Button) imageDialog.findViewById(R.id.btnImageOK);
		btnImageOK.setOnClickListener(new View.OnClickListener() {
		@Override
			public void onClick(View v) {
				imageDialog.dismiss();
				tagFoundDialog.show();
			}
		});*/
		//now that the dialog is set up, it's time to show it
		imageDialog.show();

		imageDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
//				Utilities.displayingToast = false;
			}
		});
		imageDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				Utilities.displayingToast = false;
			}
		});
	}

	
	private void playClip() {
//		if (Utilities.displayingToast) {
			String tagPath = Utilities.tagDir + spaceID + "/" + tagID + "/" + tagID + ".wav";
			SoundManager soundManager = new SoundManager(tagPath);
			soundManager.start();
//		}
	}

	private void closeDialog() {
		try {
			tagFoundDialog.dismiss();
			imageDialog.dismiss();
		}
		catch (NullPointerException e) {}
	}


	class DrawOnTop extends View {
//		float x, y;
//		Path path = new Path();
		boolean userLocated = false;
		int green = Color.rgb(0, 255, 33);
		int red = Color.RED;

        public DrawOnTop(Context context) {
			super(context);
        }
        @Override 
        protected void onDraw(Canvas canvas) {
			Paint paint = new Paint();
//			paint.setStyle(Paint.Style.STROKE);
//			paint.setColor(Color.rgb(255, 106, 0));
//			paint.setStrokeWidth(3);
//			paint.setTextSize(30);
//			canvas.drawText(Utilities.SERVER_ADDR, 10, 30, paint);
//			paint.setColor(Color.argb(200, 0, 148, 255));
//			paint.setStrokeWidth(9);
//			canvas.drawPath(path, paint);

//	        draw user location
	        int color = userLocated ? green : red;
	        paint.setColor(color);
	        canvas.drawRect(
			        new Rect(5, 5, 30, 30),
			        paint);

			paint.setStrokeWidth(3);
			paint.setTextSize(30);
	        canvas.drawText("(" + Utilities.userLocationStrength + ") - " + Utilities.userLocation, 40, 25, paint);

			super.onDraw(canvas);
        }

		public Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();
				userLocated = bundle.getBoolean("userLocated");
				invalidate();
			}
        };

/*		@Override
		public boolean onTouchEvent(MotionEvent me) {
			switch (me.getAction()) {
				case MotionEvent.ACTION_DOWN:
					path.reset();
					x = me.getX();
					y = me.getY();
					path.moveTo(x, y);
					break;
				case MotionEvent.ACTION_MOVE:
					path.lineTo(me.getX(), me.getY());
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					path.lineTo(x, y);
					invalidate();
					capturePicture("img01.jpg");
					Server.requestTags();
//					mPreview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
					break;
			}
			return true;
		}*/
	}

	class DrawCapture extends View {
//		int width = 720;
//		int width = 800;
//		int height = 480;

		int width = Utilities.PREVIEW_WIDTH;
		int height = Utilities.PREVIEW_HEIGHT;
		int newWidth = 200;
		int newHeight = 120;
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		public DrawCapture(Context context) {
			super(context);
        }

		@Override
        protected void onDraw(Canvas canvas) {
			try {
				img = "img01.jpg"; //Comment for DEMO
				if (new File(Utilities.imageDirectory + img).exists()) {
					Bitmap bm = BitmapFactory.decodeFile(Utilities.imageDirectory + img);
//					while (bm == null) {
//						Thread.sleep(50);
//						bm = BitmapFactory.decodeFile(Utilities.imageDirectory + "img01.jpg");
//						System.err.println("NULLLLL");
//					}
					if (bm != null) {
						// create matrix for the manipulation
						Matrix matrix = new Matrix();
						// resize the bit map
						matrix.postScale(scaleWidth, scaleHeight);
						Bitmap bmPreview = Bitmap.createBitmap(newWidth, newHeight, bm.getConfig());
						Canvas canvasPreview = new Canvas(bmPreview);
						canvasPreview.drawBitmap(bm, matrix, null);

						Paint paint = new Paint();
						paint.setColor(Color.argb(170, 255, 255, 255));
						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth(4);
						canvas.drawBitmap(bmPreview, 600, 0, new Paint());
						canvas.drawRect(new Rect(602, 2, 798, 118), paint);
						super.onDraw(canvas);
					}
					else
						System.err.println("NULLLLL");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

        }

		public Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				invalidate();
			}
        };
	}
}