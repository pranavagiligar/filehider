package com.quadcore.filehider;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Splash extends Activity implements Runnable{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
    
    @Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(this, FileHiderService.class));
		Thread th = new Thread(this);
		th.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	public void run() {
		try {
			Thread.sleep(500);
		}
		catch(InterruptedException e) {}
		Intent startingPoint;
		SharedPreferences sharedPref = getSharedPreferences("db_existence_check_file", 0);
		boolean isExist = sharedPref.getBoolean("isExist",false);
		if(isExist)
			startingPoint = new Intent(getApplicationContext(),LoginActivity.class);
		else
			startingPoint = new Intent(getApplicationContext(),RegisterActivity.class);
		startActivity(startingPoint);
	}
}
