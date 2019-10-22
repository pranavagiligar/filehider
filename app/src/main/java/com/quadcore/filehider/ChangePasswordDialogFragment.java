package com.quadcore.filehider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordDialogFragment extends DialogFragment {

	LoginActivity context;
	public ChangePasswordDialogFragment(){

	}
	/*public ChangePasswordDialogFragment(LoginActivity context) {
		this.context = context;
		context.isThreadRequired = true;
		
	}*/

	void setLoginActivity(LoginActivity context) {
		this.context = context;
		context.isThreadRequired = true;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog
		// layout
		View dialogView = inflater
				.inflate(R.layout.changepassword_dialog, null);
		builder.setView(dialogView);
		final EditText password = (EditText) dialogView
				.findViewById(R.id.changepassword_password_edittext);
		final EditText confirmPassword = (EditText) dialogView
				.findViewById(R.id.changepassword_confirm_password_edittext);
		final Button submitButton = (Button) dialogView.findViewById(R.id.changepassword_submit_button);

		submitButton.setOnClickListener(new OnClickListener() {
		//builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			//public void onClick(DialogInterface dia, int which) {
		
				String pass = password.getText().toString();
				String conPass = confirmPassword.getText().toString();
				if(pass.equals("") && conPass.equals("")) {
					Toast.makeText(context, "Password not changed", Toast.LENGTH_SHORT).show();
					dismiss();
				} else if (pass.equals(conPass)) {
					DatabaseHandlerUser dbUser = new DatabaseHandlerUser(context);
					String cont[] = dbUser.retrieveField();
					cont[3] = Hash.hash(pass);
					dbUser.updateCredential(cont);
					Toast.makeText(context, "Password Successfully Changed.", Toast.LENGTH_SHORT).show();
					setCancelable(true);
					dismiss();
				} else {
					Toast.makeText(context, "Password not matched", Toast.LENGTH_SHORT).show();
					password.setText("");
					confirmPassword.setText("");
				}
			}
		});

		return builder.create();
	}
}
