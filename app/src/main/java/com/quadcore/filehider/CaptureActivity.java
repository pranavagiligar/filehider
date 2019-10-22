package com.quadcore.filehider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.view.SurfaceView;

public class CaptureActivity extends Activity {
	private Camera camera; // camera object
	File mediaFile;
	public static final String IMAGE_DIRECTORY_NAME = "Captured_Images";
	int currentBright;
	int CAMERA_ID = 1;  // 1 for front camera, and 0 for rear camera

	public void setBrightness(int brightness){
		
	    //constrain the value of brightness
	    if(brightness < 0)
	        brightness = 0;
	    else if(brightness > 255)
	        brightness = 255;


	    ContentResolver cResolver = this.getApplicationContext().getContentResolver();
	    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
		    currentBright = android.provider.Settings.System.getInt(
		    getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		setBrightness(255);
		camera = Camera.open(CAMERA_ID);
		camera.startFaceDetection();
		
		SurfaceView view = new SurfaceView(this);
		try {
			camera.setPreviewDisplay(view.getHolder()); // feed dummy surface to
														// surface
		} catch (IOException e) {
			e.printStackTrace();
		}
		new CountDownTimer(5, 1000) {
			@Override
			public void onFinish() {
				camera.takePicture(null, null, null, jpegCallBack);
			}

			@Override
			public void onTick(long millisUntilFinished) {}
		}.start();
		camera.startPreview();
		
	}

	Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			File edir = Environment.getExternalStorageDirectory();
			String dirPATH = edir.getAbsolutePath()+"/Android/data/.com.quadcore.capture/"+CaptureActivity.IMAGE_DIRECTORY_NAME;
			File mediaStorageDir = new File(dirPATH);
			// Create the storage directory if it does not exist
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
				}
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
					Locale.getDefault()).format(new Date());
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".png");
			try {
				Bitmap userImage = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(CAMERA_ID, info);
				userImage = rotate(userImage, info.orientation);
				// set file out stream
				FileOutputStream out = new FileOutputStream(mediaFile);
				// set compress format quality and stream
				userImage.compress(Bitmap.CompressFormat.JPEG, 10, out);
				camera.release();
				setBrightness(currentBright);
				finish();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		public Bitmap rotate(Bitmap bitmap, int degree) {
		    int w = bitmap.getWidth();
		    int h = bitmap.getHeight();

		    Matrix mtx = new Matrix();
		    mtx.postRotate(degree);

		    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
		}
	};
}