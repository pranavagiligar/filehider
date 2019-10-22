package com.quadcore.filehider;

import java.io.File;
import java.util.Random;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private EditText password;
	private TextView forgotPassword, remainingTime;
	private Button login;

	private OTPDialogFragment otpDialog;

	private static Handler mHandler = new Handler();
	private Handler hackHandler = new Handler();
	private static Runnable thread;
	private Runnable hackAttemptThread;

	//private Intent serviceIntent;
	private FileHiderService fileHiderService;
	//private ServiceConnection mConnection;

	private int otp;
	private static int i = 2;
	private static int j = 59;
	private static int countLoop = 1;

	private int hackAttempts = 0;
	private int secondCounter = 1;
	private int waitingTime = 10;

	public static boolean isThreadRequired = true;
	private boolean isHacking = false;
	private boolean isFirstTime = true;
	
	private boolean haveCameraAccess = true;
	private boolean haveStorageAccess = true;
	
	private static final int READ_REQUEST_CODE = 66655;
	private static final int ACCESS_CAMERA_CODE = 55566;
	private static final int BOTH_REUEST_CODE = 88899;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);

		password = (EditText) findViewById(R.id.editText_login_pass);
		forgotPassword = (TextView) findViewById(R.id.textView_forgot_pass);
		login = (Button) findViewById(R.id.login_button);
		remainingTime = (TextView) findViewById(R.id.login_waiting_time_textview);
		forgotPassword.setOnClickListener(this);
		login.setOnClickListener(this);
		
		int permForReadExternal = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		int permForCamera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
		
		if (permForReadExternal != PackageManager.PERMISSION_GRANTED && permForReadExternal != PackageManager.PERMISSION_GRANTED) {
			boolean flagReadExternal = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
			boolean flagCamera = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
			ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA }, BOTH_REUEST_CODE);
		}
		else if (permForReadExternal != PackageManager.PERMISSION_GRANTED) {
			boolean flagReadExternal = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
			if (flagReadExternal) {
				// Show rationale.
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_REQUEST_CODE);
			} else {
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_REQUEST_CODE);
			}
		}
		else if(permForCamera != PackageManager.PERMISSION_GRANTED) {
			boolean flagCamera = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
			if (flagCamera) {
				// Show rationale.
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.CAMERA }, ACCESS_CAMERA_CODE);
			} else {
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.CAMERA }, ACCESS_CAMERA_CODE);
			}
		}

		// ///////////////////////////////////////////////////////
		/*mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {

				FileHiderService.LocalBinder binder = (FileHiderService.LocalBinder) service;
				fileHiderService = binder.getServiceInstance();
				fileHiderService.registerActivity(LoginActivity.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
		};
		// ///////////////////////////////////////////////////////
		serviceIntent = new Intent(LoginActivity.this, FileHiderService.class);*/
		//bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
	}	//End of onCreate
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		if (requestCode == READ_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {} 
			else {
				haveStorageAccess = false;
				//Toast.makeText(getApplicationContext(), "No Storage permission", Toast.LENGTH_SHORT).show();
			}
		}
		else if(requestCode == ACCESS_CAMERA_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {} 
			else {
				haveCameraAccess = false;
				//Toast.makeText(getApplicationContext(), "No Camera permission", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
			else {
				haveStorageAccess = false;
			}
			if(grantResults.length == 2 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {}
			else {
				haveCameraAccess = false;
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sharedPref = getSharedPreferences("pid_file", 0);
		Editor editor = sharedPref.edit();
		int pid = android.os.Process.myPid();
		editor.putInt("pid", pid);
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		SharedPreferences sharedPrefhack = getSharedPreferences("hack_file", 0);
		long hack_time = sharedPrefhack.getLong("time_of_hack", 0);
		if(isFirstTime) {
			if(fileHiderService != null) {
				//fileHiderService.sendSnap();
			}
			isFirstTime = false;
		}
		if (v == forgotPassword) {
			try {
				// if(isConnected()) {
				Random r = new Random(System.currentTimeMillis());
				otp = r.nextInt(65000 - 10000 + 1) + 10000;
				DatabaseHandlerUser dbUser = new DatabaseHandlerUser(this);
				String content[] = dbUser.retrieveField();
				String emailAddress = content[1];
				dbUser.close();
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
				Mail mail = new Mail("email","password");//Put your username and password
				String[] toArr = { emailAddress }; // This is an array, you can
													// add more emails, just
													// separate them with a coma
				mail.setTo(toArr); // load array to setTo function
				mail.setFrom("email"); // who is sending
															// the email
				mail.setSubject("FileHider OTP");
				mail.setBody("Your OTP is \"" + otp + "\"");
				try {
					if (mail.send()) {
						Toast.makeText(this, "OTP is sent to " + emailAddress,
								Toast.LENGTH_SHORT).show();
						otpDialog = new OTPDialogFragment();
						otpDialog.setLoginActivity(this);
						otpDialog.show(getFragmentManager(), "OTPDIALOG");
						otpDialog.setCancelable(false);

						thread = new Runnable() {

							@Override
							public void run() {
								if (j < 10) {
									otpDialog.countdown.setText(i + ":0" + j);
								} else
									otpDialog.countdown.setText(i + ":" + j);
								Log.d("TIME", i + ":" + j);
								j--;
								if (j == -1) {
									j = 59;
									i--;
								}

								if (countLoop != 180) {
									countLoop++;
									if (isThreadRequired)
										mHandler.postDelayed(this, 1000);
								}
								if (countLoop == 180) {
									otpDialog.dismiss();
									i = 2;
									j = 59;
									countLoop = 1;
									Toast.makeText(otpDialog.loginActivity,
											"Timed out", Toast.LENGTH_SHORT)
											.show();
								}
							}
						};
						mHandler.postDelayed(thread, 1000);
					}
				} catch (Exception e) {
					Toast.makeText(this, "Problem occur. Try Again later.",
							Toast.LENGTH_SHORT).show();
				}
				// }
				// else {
				// Toast.makeText(this, "No Intenet",
				// Toast.LENGTH_SHORT).show();
				// }
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "Problem in Connection",
						Toast.LENGTH_SHORT).show();
			}
		} else if (!isHacking && hack_time==0) {
			DatabaseHandlerUser dbUser = new DatabaseHandlerUser(this);
			String[] cont = dbUser.retrieveField();
			dbUser.close();
			String hashedPassword = Hash.hash(password.getText().toString());
			if (hashedPassword.equals(cont[3])) {
				Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT)
						.show();
				SharedPreferences sharedPref = getSharedPreferences(
						"state_file", 0);
				String path = sharedPref.getString("path", Environment
						.getExternalStorageDirectory().getPath() + "/");
				Intent intent = new Intent(this, FileExplorer.class);
				// /////////////////////////////////////////////////////////////////////////
				SharedPreferences sharedPrefPid = getSharedPreferences(
						"pid_file", 0);
				int pid = sharedPrefPid.getInt("pid", 0);
				if (pid != android.os.Process.myPid()) {
					path = Environment.getExternalStorageDirectory().getPath();
				}
				// ////////////////////////////////////////////////////////////////////////
				intent.putExtra("current_path", path);
				startActivity(intent);
				finish();
			} else {
				hackAttempts++;
				if (hackAttempts == 3) {
					// ////////////////////////////////////////////////////////////////////////////
					if(Integer.parseInt(Build.VERSION.SDK) < 21) {
						if(haveCameraAccess && haveStorageAccess) {
							int numberOfCamera = Camera.getNumberOfCameras();
							if (numberOfCamera == 2)
								startActivity(new Intent(this, CaptureActivity.class));
						}
					}
					// ////////////////////////////////////////////////////////////////////////////
					SharedPreferences sharedPref = getSharedPreferences("hack_file", 0);
					Editor editor = sharedPref.edit();
					editor.putLong("time_of_hack", System.currentTimeMillis()+waitingTime*1000+4000);
					editor.commit();

					isHacking = true;
					Toast.makeText(this, "Login after " + waitingTime + " sec",
							Toast.LENGTH_SHORT).show();
					hackAttemptThread = new Runnable() {

						@Override
						public void run() {
							remainingTime.setText("Reamining time : "
									+ (waitingTime - secondCounter)
									+ " seconds");
							secondCounter++;
							if (secondCounter == waitingTime + 1) { // Waiting
																	// time for
																	// login
																	// attempt.
								secondCounter = 1;
								waitingTime += 10;
								if(waitingTime > 30)
									waitingTime = 30;
								isHacking = false;
								remainingTime.setText("");
								SharedPreferences sharedPref = getSharedPreferences("hack_file", 0);
								Editor editor = sharedPref.edit();
								editor.putLong("time_of_hack", 0);
								editor.commit();
							} else {
								hackHandler.postDelayed(this, 1000);
							}
						}
					};
					hackHandler.postDelayed(hackAttemptThread, 1000);
					hackAttempts = 0;
				} else {
					Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT)
							.show();
					password.setText("");
				}
			}
		}
		else if(hack_time != 0) {
			hack_time = System.currentTimeMillis() - hack_time;
			if(hack_time < 0) {
				Toast.makeText(this, milliSecondsToTimer(-hack_time) + " remaining", Toast.LENGTH_SHORT).show();
				
			}
			else {
				SharedPreferences sharedPref = getSharedPreferences("hack_file", 0);
				Editor editor = sharedPref.edit();
				editor.putLong("time_of_hack", 0);
				editor.commit();
			}
		}
	}
	
	public String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	/*
	 * private boolean isConnected() throws InterruptedException, IOException {
	 * String command = "ping -c 1 google.com"; return
	 * (Runtime.getRuntime().exec (command).waitFor() == 0); }
	 */

	private boolean checkOTP(int parseInt) {
		return (otp == parseInt) ? true : false;
	}

	public static class OTPDialogFragment extends DialogFragment {

		private LoginActivity loginActivity;
		TextView countdown;
		public OTPDialogFragment(){}
		/*public OTPDialogFragment(LoginActivity la) {
			loginActivity = la;
		}*/
		void setLoginActivity(LoginActivity la) {
			loginActivity = la;
		}
		@SuppressLint("InflateParams")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			View dialogView = inflater.inflate(R.layout.otp_view, null);
			builder.setView(dialogView);
			final EditText enterOTP = (EditText) dialogView
					.findViewById(R.id.enterOTP_ExitText);
			countdown = (TextView) dialogView
					.findViewById(R.id.otp_countdown_textView);
			// Add action buttons
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							if (!enterOTP.getText().toString().equals("")) {
								boolean isMatched = loginActivity
										.checkOTP(Integer.parseInt(enterOTP
												.getText().toString()));
								if (isMatched) {
									setCancelable(true);
									dismiss();
									isThreadRequired = false;
									mHandler.removeCallbacks(thread);
									i = 2;
									j = 59;
									countLoop = 1;
									ChangePasswordDialogFragment cpdf = new ChangePasswordDialogFragment();
									cpdf.setLoginActivity(loginActivity);
									cpdf.show(getFragmentManager(),
											"CHANGEPASSWORDDIALOG");
								} else {
									Toast.makeText(loginActivity, "Wrong OTP",
											Toast.LENGTH_LONG).show();
									setCancelable(true);
									dismiss();
								}
							}
						}

					});
			return builder.create();
		}
	}

}
