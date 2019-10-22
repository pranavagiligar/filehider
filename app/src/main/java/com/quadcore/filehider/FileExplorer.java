package com.quadcore.filehider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorer extends ListActivity {

	private File currentDir;
	private static File selectedFile;
	private MenuDialog md;
	private String dbPath;
	private FileArrayAdapter adapter;

	public boolean isLongClicked = false;
	private boolean lockCheck = false;
	// private boolean isPaused = false;
	// private boolean isStoped = false;
	private boolean isUnhideListReturned = false;
	private boolean dontCloseThisActivity = false;
	private static final int UNHIDE_ACTIVITY_RESULT_CODE = 24566;
	private static final int READ_REQUEST_CODE = 55566;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String path = intent.getStringExtra("current_path");
		currentDir = new File(path);
		int perm = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		if (perm == PackageManager.PERMISSION_GRANTED) {
			fill(currentDir);
		} else {
			dontCloseThisActivity = true;
			boolean flag = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
			if (flag) {
				// Show rationale.
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_REQUEST_CODE);
			} else {
				ActivityCompat.requestPermissions(this, new String [] { Manifest.permission.READ_EXTERNAL_STORAGE }, READ_REQUEST_CODE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {
		if (requestCode == READ_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				fill(currentDir);
			} else {
				Toast.makeText(getApplicationContext(), "No read permission", Toast.LENGTH_SHORT).show();
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
		if (isUnhideListReturned) {
			isUnhideListReturned = false;
		} else {
			if (lockCheck) {
				lock();
			}
		}
		lockCheck = false;
		// isPaused = false;
		// isStoped = false;

	}

	@Override
	protected void onPause() {
		super.onPause();
		if(!dontCloseThisActivity) {
			lockCheck = true;
			// isPaused = true;
			SharedPreferences sharedPref = getSharedPreferences("pid_file", 0);
			Editor editor = sharedPref.edit();
			int pid = android.os.Process.myPid();
			editor.putInt("pid", pid);
			editor.commit();
		} else {
			dontCloseThisActivity = false;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		lockCheck = true;
		// isStoped = true;
	}

	@Override
	protected void onDestroy() {
		lockCheck = true;
		super.onDestroy();
	}

	private void lock() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void fill(File f) {
		File[] dirs = f.listFiles();
		this.setTitle("Current Dir: " + f.getName());
		try {
			dbPath = f.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		SharedPreferences sharedPref = getSharedPreferences("state_file", 0);
		Editor editor = sharedPref.edit();
		editor.putString("path", dbPath);
		editor.commit();
		List<Item> dir = new ArrayList<Item>();
		List<Item> fls = new ArrayList<Item>();
		try {
			for (File ff : dirs) {
				Date lastModDate = new Date(ff.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if (ff.isDirectory()) {

					File[] fbuf = ff.listFiles();
					int buf = 0;
					if (fbuf != null) {
						buf = fbuf.length;
					} else
						buf = 0;
					String num_item = String.valueOf(buf);
					if (buf == 0)
						num_item = num_item + " item";
					else
						num_item = num_item + " items";

					String directoryname = ff.getName();
					if (directoryname.equalsIgnoreCase("dcim")) {
						dir.add(new Item(ff.getName(), num_item, date_modify,
								ff.getCanonicalPath(), "dcim_icon"));
					} else {
						if (directoryname.equalsIgnoreCase("download")) {
							dir.add(new Item(ff.getName(), num_item,
									date_modify, ff.getCanonicalPath(),
									"download_icon"));
						} else {
							if (directoryname.equalsIgnoreCase("hike")) {
								dir.add(new Item(ff.getName(), num_item,
										date_modify, ff.getCanonicalPath(),
										"hike_icon"));
							} else {
								if (directoryname.equalsIgnoreCase("shareit")) {
									dir.add(new Item(ff.getName(), num_item,
											date_modify, ff.getCanonicalPath(),
											"shareit_icon"));
								} else {
									if (directoryname
											.equalsIgnoreCase("whatsapp")) {
										dir.add(new Item(ff.getName(),
												num_item, date_modify, ff
														.getCanonicalPath(),
												"whatsapp_icon"));
									} else {
										dir.add(new Item(ff.getName(),
												num_item, date_modify, ff
														.getCanonicalPath(),
												"directory_icon"));
									}
								}
							}
						}
					}
				} else {
					/* changing of icons according to type */
					String filename = ff.getName();
					int dat = filename.lastIndexOf('.');
					filename = filename.substring(dat);
					if (filename.equalsIgnoreCase(".jpg")
							|| filename.equalsIgnoreCase(".png")) {
						fls.add(new Item(ff.getName(), ff.length() + " Byte",
								date_modify, ff.getCanonicalPath(), "image"));
					} else {
						if (filename.equalsIgnoreCase(".pdf")) {
							fls.add(new Item(ff.getName(), ff.length()
									+ " Byte", date_modify, ff
									.getCanonicalPath(), "pdf_icon"));
						} else {
							if (filename.equalsIgnoreCase(".mp4")
									|| filename.equalsIgnoreCase(".3gp")
									|| filename.equalsIgnoreCase(".avi")
									|| filename.equalsIgnoreCase(".mkv")
									|| filename.equalsIgnoreCase(".mov")
									|| filename.equalsIgnoreCase(".mpeg")) {
								fls.add(new Item(ff.getName(), ff.length()
										+ " Byte", date_modify, ff
										.getCanonicalPath(), "video_icon"));
							} else {
								if (filename.equalsIgnoreCase(".mp3")
										|| filename.equalsIgnoreCase(".wav")
										|| filename.equalsIgnoreCase(".acc")
										|| filename.equalsIgnoreCase(".wma")) {
									fls.add(new Item(ff.getName(), ff.length()
											+ " Byte", date_modify, ff
											.getCanonicalPath(), "audio"));

								} else {
									if (filename.equalsIgnoreCase(".doc")
											|| filename
													.equalsIgnoreCase(".docx")
											|| filename
													.equalsIgnoreCase(".xls")
											|| filename
													.equalsIgnoreCase(".xlsx")
											|| filename
													.equalsIgnoreCase(".ppt")
											|| filename
													.equalsIgnoreCase(".pptx")
											|| filename
													.equalsIgnoreCase(".txt")) {
										fls.add(new Item(ff.getName(), ff
												.length() + " Byte",
												date_modify, ff
														.getCanonicalPath(),
												"file_icon"));
									} else {
										if (filename.equalsIgnoreCase(".apk")) {
											fls.add(new Item(ff.getName(), ff
													.length() + " Byte",
													date_modify,
													ff.getCanonicalPath(),
													"apk_icon"));
										} else {
											if (filename
													.equalsIgnoreCase(".gif")) {
												fls.add(new Item(ff.getName(),
														ff.length() + " Byte",
														date_modify,
														ff.getCanonicalPath(),
														"gif_icon"));
											} else {
												fls.add(new Item(ff.getName(),
														ff.length() + " Byte",
														date_modify,
														ff.getCanonicalPath(),
														"unknown"));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase(
				Environment.getExternalStorageDirectory().getName()))
			dir.add(0, new Item("..", "Parent Directory", "", f.getParent(),
					"directory_up"));
		adapter = new FileArrayAdapter(this, R.layout.file_view, dir, this);
		this.setListAdapter(adapter);
	}

	public void onItemLongClick(int position, View view) {
		Item o = adapter.getItem(position);
		selectedFile = new File(o.getPath());
		view.setBackgroundColor(getResources().getColor(R.color.fuchsia));
		md = new MenuDialog();
		md.setExplorerAndView(this, view);
		md.show(getFragmentManager(), "FragmentManager");
	}

	public void onItemClick(int position) {
		if (!isLongClicked) {
			Item o = adapter.getItem(position);
			if (o.getImage().equalsIgnoreCase("directory_icon")
					|| o.getImage().equalsIgnoreCase("dcim_icon")
					|| o.getImage().equalsIgnoreCase("download_icon")
					|| o.getImage().equalsIgnoreCase("hike_icon")
					|| o.getImage().equalsIgnoreCase("shareit_icon")
					|| o.getImage().equalsIgnoreCase("whatsapp_icon")
					|| o.getImage().equalsIgnoreCase("directory_up")) {
				currentDir = new File(o.getPath());
				fill(currentDir);
			} else {
				File temp_file = new File(o.getPath());
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(temp_file),
						getMimeType(temp_file.getAbsolutePath()));
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(this, "Unknown type file", Toast.LENGTH_LONG)
							.show();
				}
			}
		}
		isLongClicked = false;
	}

	public String getMimeType(String url) {
		String parts[] = url.split("\\.");
		String extension = parts[parts.length - 1];
		String type = null;
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}

	@Override
	public void onBackPressed() {
		if (Environment.getExternalStorageDirectory().getPath()
				.equals(currentDir.getPath())) {
			SharedPreferences sharedPref = getSharedPreferences("state_file", 0);
			Editor editor = sharedPref.edit();
			editor.putString("path", Environment.getExternalStorageDirectory()
					.getPath());
			editor.commit();
			super.onBackPressed();
		} else {
			currentDir = currentDir.getParentFile();
			fill(currentDir);
		}
	}

	public static class MenuDialog extends DialogFragment {

		FileExplorer fe;
		View view;
		public MenuDialog(){}
		/*public MenuDialog(FileExplorer fe, View view) {
			this.fe = fe;
			this.view = view;
		}*/

		public void setExplorerAndView(FileExplorer fe, View view) {
			this.fe = fe;
			this.view = view;
		};

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String options[] = new String[] { "Hide", "Rename", "Delete",
					"Cancel" };
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Options").setItems(options,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								try {
									new Crypt(fe.getApplicationContext())
											.encrypt(selectedFile);
									String parentDir = selectedFile.getParent();
									selectedFile.delete();
									fe.sendBroadcast(new Intent(
											Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
											Uri.fromFile(selectedFile)));
									fe.fill(new File(parentDir));

								} catch (Exception e) {
									Log.e("EncryptError", e.toString());
								}
							} else if (which == 1) {
								dismiss();
								RenameDialogFragment rdf = new RenameDialogFragment();
								rdf.setFile(selectedFile,fe);
								rdf.show(getFragmentManager(), "renameconfirm");

							} else if (which == 2) {
								dismiss();
								ConfirmDialogFragment cdf = new ConfirmDialogFragment();
								cdf.setFile(selectedFile, fe);
								cdf.show(getFragmentManager(), "deleteconfirm");
							} else if (which == 3) {
								setCancelable(true);
								dismiss();
							}
							view.setBackgroundColor(getResources().getColor(
									R.color.white));
						}
					});
			setCancelable(false);
			return builder.create();
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.unhide) {
			Intent intent = new Intent(this, HideList.class);
			startActivityForResult(intent, UNHIDE_ACTIVITY_RESULT_CODE);
		} else if (item.getItemId() == R.id.about_us) {
			// About our team.
			TextView tv = new TextView(this);
			tv.setBackgroundColor(getResources().getColor(R.color.teal));
			tv.setTextColor(getResources().getColor(R.color.white));
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			tv.setText("It is a file hider which support builtin encryption security.");

			Dialog ad = new Dialog(this);
			ad.setTitle("About");
			ad.setContentView(tv);
			ad.show();
		} else {
			FileExplorer.ChangePasswordDialogFragment cp = new FileExplorer.ChangePasswordDialogFragment();
			cp.setFileExplorer(this);
			cp.show(getFragmentManager(), "CHANGEPASSWORDOLD");
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UNHIDE_ACTIVITY_RESULT_CODE) {
			if (resultCode == 1397) {
				isUnhideListReturned = true;
				fill(currentDir);
			} else if (resultCode == 7931) {
				isUnhideListReturned = false;
				lockCheck = true;
			}
		}
	}

	public static class ConfirmDialogFragment extends DialogFragment { // This dialog
																	// is for
																	// Delete
																	// Confirmation.

		private File file;
		private FileExplorer fe;
		public ConfirmDialogFragment(){}
		/*public ConfirmDialogFragment(File file) {
			this.file = file;
		}*/
		void setFile(File file, FileExplorer fe) {
			this.file = file;
			this.fe = fe;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Are you Sure?")
					.setPositiveButton("YES",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									file.delete();
									fe.fill(new File(selectedFile.getParent()));
								}
							})
					.setNegativeButton("NO",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dismiss();
								}
							});
			return builder.create();
		}
	}

	public static class RenameDialogFragment extends DialogFragment {

		private File file;
		private FileExplorer fe;
		public RenameDialogFragment(){}
		/*public RenameDialogFragment(File file) {
			this.file = file;
		}*/
		void setFile(File file,FileExplorer fe) {
			this.file = file;
			this.fe = fe;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			View dialogView = inflater.inflate(R.layout.dialog_rename, null);
			builder.setView(dialogView);
			final EditText renEditText = (EditText) dialogView
					.findViewById(R.id.renameEditText);
			// Add action buttons
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							if (!renEditText.getText().toString().equals("")) {
								file.renameTo(new File(file.getParent() + "/"
										+ renEditText.getText().toString()));
								fe.fill(new File(file.getParent() + "/"));
							}
						}
					}).setNegativeButton("CANCEL",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dismiss();
						}
					});

			return builder.create();
		}
	}

	public static class ChangePasswordDialogFragment extends DialogFragment {

		FileExplorer context;

		public ChangePasswordDialogFragment(){}
		/*public ChangePasswordDialogFragment(FileExplorer context) {
			this.context = context;
		}*/
		void setFileExplorer(FileExplorer context) {
			this.context = context;
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
			View dialogView = inflater.inflate(
					R.layout.changepassword_dialog_old, null);
			builder.setView(dialogView);
			final EditText oldPassword = (EditText) dialogView
					.findViewById(R.id.changepassword_oldpassword_edittext_old);
			final EditText password = (EditText) dialogView
					.findViewById(R.id.changepassword_password_edittext_old);
			final EditText confirmPassword = (EditText) dialogView
					.findViewById(R.id.changepassword_confirm_password_edittext_old);
			final Button submitButton = (Button) dialogView
					.findViewById(R.id.changepassword_submit_button_old);

			submitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String oldPass = oldPassword.getText().toString();
					String pass = password.getText().toString();
					String conPass = confirmPassword.getText().toString();

					if (oldPass.equals("") && pass.equals("")
							&& conPass.equals("")) {
						Toast.makeText(context, "Password not changed",
								Toast.LENGTH_SHORT).show();
						dismiss();
					} else if (pass.equals(conPass)) {
						DatabaseHandlerUser dbUser = new DatabaseHandlerUser(
								context);
						String cont[] = dbUser.retrieveField();
						String op = Hash.hash(oldPass);
						if (op.equals(cont[3])) {
							cont[3] = Hash.hash(pass);
							dbUser.updateCredential(cont);
							Toast.makeText(context,
									"Password Successfully Changed.",
									Toast.LENGTH_SHORT).show();
							setCancelable(true);
							dismiss();
							// ////////////////////////////////////////////////
							SharedPreferences sharedPref = context.getSharedPreferences(
									"state_file", 0);
							Editor editor = sharedPref.edit();
							editor.putString("path", Environment
									.getExternalStorageDirectory().getPath());
							editor.commit();
							context.finish();
							startActivity(new Intent(context.getApplicationContext(),
									LoginActivity.class));
							// ////////////////////////////////////////////////
						} else {
							oldPassword.setText("");
							Toast.makeText(context, "Old password is worng",
									Toast.LENGTH_SHORT).show();
						}

					} else {
						Toast.makeText(context, "Password not matched",
								Toast.LENGTH_SHORT).show();
						password.setText("");
						confirmPassword.setText("");
					}
				}
			});

			return builder.create();
		}
	}
}
