package com.quadcore.filehider;
 
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandlerFile extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "QuadcoreFilehiderDB1";
    // Table name
    private final String TABLE_NAME = "FileInfo";
    //Attributes name
    private final String O_FILENAME = "o_filename";
   // private final String O_EXTENTION = "o_extention";
    private final String O_PATH = "o_path";
    private final String CURRENT_LOC = "current_loc";
    private final String UNIQUE_ID = "unique_id";
 
    public DatabaseHandlerFile(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + O_FILENAME +  " TEXT,"
               // + O_EXTENTION +  " TEXT,"
                + O_PATH +  " TEXT,"
                + CURRENT_LOC + " TEXT,"
                + UNIQUE_ID + " TEXT" + ")";
        try {
        	db.execSQL(CREATE_TABLE);
        	//db.close();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
    	/*try {
    		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
        // Create tables again
        onCreate(db);*/
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    //This is used when encrypt a file to store it's details.
    long addField(String[] cont) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(O_FILENAME, cont[0]);
			//values.put(O_EXTENTION, cont[1]);
			values.put(O_PATH, cont[1]);
			values.put(CURRENT_LOC, cont[2]);
			values.put(UNIQUE_ID, cont[3]);
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

    //This is used to list all the hidden files.
	ArrayList<FileAttributeHolder> retrieveField() {
		ArrayList<FileAttributeHolder> files = new ArrayList<FileAttributeHolder>();
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String selectQuery = "SELECT  * FROM " + TABLE_NAME;
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor.moveToFirst()) {
				do {
					FileAttributeHolder holder = new FileAttributeHolder();
					holder.setFileName(cursor.getString(0));
					holder.setPath(cursor.getString(1));
					holder.setCurrentPath(cursor.getString(2));
					holder.setUnique_id(cursor.getString(3));
					files.add(holder);
				} while (cursor.moveToNext());
			}
			//cursor.close();
			//db.close();
			return files;
		} catch (Exception e) {
			e.printStackTrace();
			return files;
		}
	}
	
	//This is used when decrypt a file.
	void deleteField(String path) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, O_PATH + " = ?", new String [] {path});
		//db.close();
	}
}