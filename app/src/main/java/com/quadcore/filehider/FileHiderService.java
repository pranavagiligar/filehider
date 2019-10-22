package com.quadcore.filehider;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

public class FileHiderService extends Service {
	LoginActivity loginActivity;
	Runnable thread;
	Handler mHandler;
	String[] toArr;
	boolean isInProgress = false;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		sendSnap();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// returns the instance of the service
	public class LocalBinder extends Binder {
		public FileHiderService getServiceInstance() {
			return FileHiderService.this;
		}
	}

	public void registerActivity(LoginActivity ref) {
		loginActivity = ref;
	}

	public void sendSnap() {
		if(!isInProgress) {
			DatabaseHandlerUser dbUser = new DatabaseHandlerUser(this);
			String content[] = dbUser.retrieveField();
			String[] to = { content[1] };
			toArr = to;
			dbUser.close();
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
			Upload task = new Upload();
			task.execute();
		}
	}
	
	class Upload extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {
			boolean flag = true;
			while(flag) {
				
				try {
					File edir = Environment.getExternalStorageDirectory();
					String dirPATH = edir.getAbsolutePath()+"/Android/data/.com.quadcore.capture/"+CaptureActivity.IMAGE_DIRECTORY_NAME;
					File photos[] = new File(dirPATH).listFiles();
					if(photos.length > 0) {
						Date lastModDate = new Date(photos[0].lastModified());
						DateFormat formater = DateFormat.getDateTimeInstance();
						String date_modify = formater.format(lastModDate);
						Mail mail = new Mail("email", "password");
						mail.setTo(toArr);
						mail.setFrom("email");
						mail.setSubject("FileHider: Unathorized attempt");
						mail.setBody("At \"" + date_modify + "\" some one attempt to login."); // Date and time of the captured file.
						mail.addAttachment(photos[0].getAbsolutePath());
						if (mail.send()) {
							photos[0].delete();
						}
					}
				} catch (Exception e) {}
			}
			return this;
		}
		
	}
}
