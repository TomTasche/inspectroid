package at.tomtasche.inspectroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RequestDatabase {

	private RequestDatabaseHelper requestDatabaseHelper;
	private SQLiteDatabase database;

	public RequestDatabase(Context context, boolean writable) {
		requestDatabaseHelper = new RequestDatabaseHelper(context);

		if (writable) {
			database = requestDatabaseHelper.getWritableDatabase();
		} else {
			database = requestDatabaseHelper.getReadableDatabase();
		}
	}

	public Cursor getRequests() {
		Cursor cursor = database.query(RequestDatabaseHelper.TABLE_NAME, null,
				null, null, null, null, RequestDatabaseHelper.WHEN + " DESC");

		return cursor;
	}

	public void addRequest(String url, long when) {
		ContentValues values = new ContentValues();
		values.put(RequestDatabaseHelper.URL, url);
		values.put(RequestDatabaseHelper.WHEN, when);

		long newId = database.insert(RequestDatabaseHelper.TABLE_NAME, null,
				values);

		Log.e("smn", "new: " + newId);
	}

	public void close() {
		database.close();
		requestDatabaseHelper.close();
	}
}
