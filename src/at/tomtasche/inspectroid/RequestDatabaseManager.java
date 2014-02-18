package at.tomtasche.inspectroid;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mba.proxylight.Request;

public class RequestDatabaseManager {
	private RequestDatabaseHelper helper;
	private SQLiteDatabase database;

	private RequestDatabaseThread thread;
	private BlockingQueue<RequestWrapper> queue;

	public RequestDatabaseManager(Context context) {
		helper = new RequestDatabaseHelper(context);

		queue = new LinkedBlockingQueue<RequestWrapper>();
	}

	public void initialize(boolean writable) {
		if (thread != null) {
			throw new IllegalStateException("manager already initialized");
		}

		if (writable) {
			database = helper.getWritableDatabase();

			thread = new RequestDatabaseThread();
			thread.start();
		} else {
			database = helper.getReadableDatabase();
		}
	}

	public Cursor getRequests() {
		Cursor cursor = database.query(RequestDatabaseHelper.TABLE_NAME, null,
				null, null, null, null, RequestDatabaseHelper.WHEN + " DESC");

		return cursor;
	}

	public void addRequest(Request request) {
		RequestWrapper wrapper = new RequestWrapper();
		wrapper.request = request;
		wrapper.time = System.currentTimeMillis();

		queue.add(wrapper);
	}

	public void clear() {
		// TODO: implement
	}

	public void close() {
		database.close();
		helper.close();

		if (thread != null) {
			thread.running = false;
			thread.interrupt();
		}
	}

	private class RequestWrapper {
		long time;

		Request request;
	}

	private class RequestDatabaseThread extends Thread {
		boolean running = true;

		@Override
		public void run() {
			while (running) {
				try {
					RequestWrapper request = queue.take();

					ContentValues values = new ContentValues();
					values.put(RequestDatabaseHelper.URL,
							request.request.getUrl());
					values.put(RequestDatabaseHelper.WHEN, request.time);

					long newId = database.insert(
							RequestDatabaseHelper.TABLE_NAME, null, values);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
