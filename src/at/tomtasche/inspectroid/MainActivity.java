package at.tomtasche.inspectroid;

import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends ListActivity implements
		OnCheckedChangeListener {

	private RequestDatabaseManager requestDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestDatabase = new RequestDatabaseManager(this);
		requestDatabase.initialize(false);

		Cursor requests = requestDatabase.getRequests();
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, requests,
				new String[] { RequestDatabaseHelper.URL,
						RequestDatabaseHelper.WHEN }, new int[] {
						android.R.id.text1, android.R.id.text2 });

		final int whenColumnIndex = requests
				.getColumnIndex(RequestDatabaseHelper.WHEN);

		cursorAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (columnIndex == whenColumnIndex) {
					long when = cursor.getLong(whenColumnIndex);
					// TODO: this is probably very inefficient
					Date date = new Date(when);

					TextView textView = (TextView) view;
					textView.setText(date.toString());

					return true;
				}

				return false;
			}
		});

		getListView().setAdapter(cursorAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		Switch proxySwitch = (Switch) menu.findItem(R.id.proxy_switch)
				.getActionView();
		proxySwitch.setChecked(ProxyService.running);
		proxySwitch.setOnCheckedChangeListener(this);

		MenuItem httpItem = menu.findItem(R.id.http_toggle);
		toggleHttpItemText(httpItem);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.http_toggle:
			ProxyService.filtering = !ProxyService.filtering;

			toggleHttpItemText(item);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleHttpItemText(MenuItem httpItem) {
		String title;
		String titleCondensed;
		if (ProxyService.filtering) {
			title = "Blocking HTTP";
			titleCondensed = "Blocking";
		} else {
			title = "Not blocking HTTP";
			titleCondensed = "Not blocking";
		}

		httpItem.setTitle(title);
		httpItem.setTitleCondensed(titleCondensed);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Intent intent = new Intent(this, ProxyService.class);
		if (isChecked) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}

	@Override
	protected void onStop() {
		requestDatabase.clear();
		requestDatabase.close();

		super.onStop();
	}
}
