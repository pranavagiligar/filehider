package com.quadcore.filehider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class Crypt {
	private Context context;

	public Crypt(Context context) {
		this.context = context;
	}

	public void encrypt(File selectedFile) throws Exception {
		FileInputStream fin;
		FileOutputStream fout;
		
		fin = new FileInputStream(selectedFile);
		
		/**************************************************************************************/
		int siz = fin.available();
		byte[] byt = new byte[siz];
		fin.read(byt, 0, siz);
		// *******************************************************
		DatabaseHandlerUser dbUser = new DatabaseHandlerUser(context);
		String cont[] = dbUser.retrieveField();
		dbUser.close();
		// *******************************************************
		byte dig[] = cont[4].getBytes();
		int passIndex = 0;
		for (int i = 0; i < siz - 2; i++) {
			byt[i] = (byte) (byt[i] ^ dig[passIndex]); // [byt] holds encrypted
														// data.
			if (passIndex < (dig.length - 1))
				passIndex++;
			else
				passIndex = 0;
		}
		/**************************************************************************************/
		String destinationPath = getDestinationPath(selectedFile);
		String currentFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS")
				.format(Calendar.getInstance().getTime()) + ".crypt";
		
		String outFile = destinationPath + currentFileName;

		fout = new FileOutputStream(outFile);
		fout.write(byt, 0, siz);

		fin.close();
		fout.close();

		String content [] = {selectedFile.getName(), selectedFile.getCanonicalPath(), destinationPath, currentFileName};
		DatabaseHandlerFile dbFile = new DatabaseHandlerFile(context);
		dbFile.addField(content);
		dbFile.close();
	}

	private String getDestinationPath(File selectedFile) throws Exception { //TODO : Problem in Android 7
		String path = selectedFile.getParent()+"/";
		String pt = "/";
		boolean flag = true;
		while (flag) {
			for (int i = 1; i < path.length(); i++) {
				char c = path.charAt(i);
				if (c != '/') {
					pt += c;
				} else {
					try {
						pt += "/";
						if(pt.equals("/storage/emulated/")) pt += "0/"; //TODO :  This is for emulated not have read permission.
						File file[] = new File(pt).listFiles();
						for (File f : file) {
							if (f.getName().equals("Android")) {
								pt += "Android/data/com.quadcore";
								flag = false;
								i = path.length();
								break;
							}
						}
					}
					catch(Exception e) {
						//Toast.makeText(context, "At for each ", Toast.LENGTH_SHORT).show();
						//throw e;
					}
				}
			}
		}
		File dir = new File(pt);
		dir.mkdir();

		return pt + "/";
	}
	
	public void decrypt(FileAttributeHolder fileAttributeHolder) throws Exception {
		DatabaseHandlerUser dbUser = new DatabaseHandlerUser(context);
		String cont[] = dbUser.retrieveField();
		
		FileInputStream fin;
		FileOutputStream fout;
		
		fin = new FileInputStream(fileAttributeHolder.getCurrentPath()+fileAttributeHolder.getUnique_id());
		/**************************************************************************************/
		int siz = fin.available();
		byte byt[] = new byte[siz];

		fin.read(byt);
		
		byte dig[] = cont[4].getBytes();
		int passIndex = 0;
		for(int i = 0; i < siz-2; i++) {
			byt[i] = (byte)(byt[i] ^ dig[passIndex]);	//[byt] contains decrypted data.
			
			if(passIndex < (dig.length - 1)) passIndex++;
			else passIndex = 0;
		}
		/**************************************************************************************/
		
		fout = new FileOutputStream(fileAttributeHolder.getPath());
		fout.write(byt, 0, siz);
		
		fin.close();
		fout.close();
	}
}
