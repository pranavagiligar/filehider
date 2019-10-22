package com.quadcore.filehider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandlerUser extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "QuadcoreFilehiderDB";

	// Table name
	private final String TABLE_NAME = "UserCredential";

	// Attributes names

	private final String USERNAME = "username";
	private final String HASH = "hash";
	private final String EMAIL = "email";
	private final String PHONE = "phone";
	private final String KEY = "key";

	public DatabaseHandlerUser(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + USERNAME
				+ " TEXT," + EMAIL + " TEXT," + PHONE + " TEXT," + HASH
				+ " TEXT," + KEY + " TEXT" + ")";
		try {
			db.execSQL(CREATE_TABLE);
			//db.close();	// WARNING : Don't write db close in any program
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		/*
		 * try { db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); }
		 * catch(Exception e) { e.printStackTrace(); } // Create tables again
		 * onCreate(db);
		 */
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */
	public long addField(String[] cont) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(USERNAME, cont[0]);
			values.put(EMAIL, cont[1]);
			values.put(PHONE, cont[2]);
			values.put(HASH, cont[3]);
			values.put(KEY, cont[4]);
			
			// Inserting Row
			long status = db.insert(TABLE_NAME, null, values);
			// check status. If -1 then Registration failed.
			//db.close(); // Closing database connection
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	// Getting single user
	public String [] retrieveField() {
		String [] members = new String[5];
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String selectQuery = "SELECT  * FROM " + TABLE_NAME;
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor.moveToFirst()) {
				do {
					members[0] = cursor.getString(0);
					members[1] = cursor.getString(1);
					members[2] = cursor.getString(2);
					members[3] = cursor.getString(3);
					members[4] = cursor.getString(4);
				} while (cursor.moveToNext());
			}
			//cursor.close();
			//db.close();
			return members;
		} catch (Exception e) {
			e.printStackTrace();
			return members;
		}
	}
	
	// TODO : Update when forgot password option is chosen.
	public long updateCredential(String[] cont) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(USERNAME, cont[0]);
			values.put(EMAIL, cont[1]);
			values.put(PHONE, cont[2]);
			values.put(HASH, cont[3]);
			values.put(KEY, cont[4]);
			// Inserting Row
			long status = db.update(TABLE_NAME, values, USERNAME + " = ?", new String [] {cont[0]});
			// check status. If -1 then Registration failed.
			//db.close(); // Closing database connection
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}