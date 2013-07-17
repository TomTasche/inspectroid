package at.tomtasche.inspectroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RequestDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "request";
	public static final String URL = "url";
	public static final String WHEN = "time";
	private static final String TABLE_CREATE = "CREATE TABLE "
			+ TABLE_NAME + " (" + URL + " TEXT, " + WHEN + " LONG);";

	public RequestDatabaseHelper(Context context) {
		super(context, "requests", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
