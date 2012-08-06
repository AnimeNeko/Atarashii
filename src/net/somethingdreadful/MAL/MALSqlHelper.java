package net.somethingdreadful.MAL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MALSqlHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "MAL.db";
	private static final int DATABASE_VERSION = 1;
	
	
	public static final String COLUMN_ID = "_id";
	public static final String TABLE_ANIME = "anime";
	public static final String TABLE_MANGA = "manga";
	
	private static final String CREATE_ANIME_TABLE = "create table "
			+ TABLE_ANIME + "(" + COLUMN_ID  +" integer primary key autoincrement, "
			+ "recordID text, "
			+ "recordName text, "
			+ "imageUrl text";
	
	
	public MALSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
