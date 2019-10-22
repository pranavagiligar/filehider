package com.quadcore.filehider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {
	
	private EditText name, email, phone, pass, confirmPass;
	private Button submit;
	private String [] cont = new String[5];
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_loyout);
        name = (EditText) findViewById(R.id.editText_name);
        email = (EditText) findViewById(R.id.editText_email);
        phone = (EditText) findViewById(R.id.editText_phone);
        pass = (EditText) findViewById(R.id.editText_pass);
        confirmPass = (EditText) findViewById(R.id.editText_confirm_pass);
        submit = (Button) findViewById(R.id.register_submit);
        
        submit.setOnClickListener(this);
    }

	boolean validationCheck() {
		if(	cont[0].equals("") ||
			cont[1].equals("") ||
			cont[2].equals("") ||
			cont[3].equals("") ||
			cont[4].equals("")) {
			return false;
		}
		// TODO : CHECK EMAIL VALIDATION.
		else return true;
	}
	
	@Override
	public void onClick(View v) {
		cont[0] = name.getText().toString();
		cont[1] = email.getText().toString();
		cont[2] = phone.getText().toString();
		cont[3] = pass.getText().toString();
		cont[4] = confirmPass.getText().toString();
		if(!validationCheck()) {
			Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show();
		}
		else {
			if(cont[3].equals(cont[4])) { // PASSWORD Matching check.
				//Calculate hash
				cont[3] = Hash.hash(cont[3]);
				// Calculate Time for Key.
				cont[4] = Hash.hash(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime()));
				
				DatabaseHandlerUser dbUser = new DatabaseHandlerUser(this);
				long status = dbUser.addField(cont);
				dbUser.close();
				if(status != -1) {
					Toast.makeText(this, "Register Successful", Toast.LENGTH_LONG).show();
					File edir = Environment.getExternalStorageDirectory();
					String dirPATH = edir.getAbsolutePath()+"/Android/data/.com.quadcore.capture/"+CaptureActivity.IMAGE_DIRECTORY_NAME;
					File mediaStorageDir = new File(dirPATH);
					mediaStorageDir.delete();
					SharedPreferences sharedPref = getSharedPreferences("db_existence_check_file", 0);
					Editor editor = sharedPref.edit();
					editor.putBoolean("isExist", true);
					editor.commit();
					Intent intent = new Intent(this, LoginActivity.class);
					startActivity(intent);
					finish();
				}
				else {
					Toast.makeText(this, "Register Failed !", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(this, "Password Not Matched!", Toast.LENGTH_LONG).show();
				pass.setText("");
				confirmPass.setText("");
			}
		}
	}
}