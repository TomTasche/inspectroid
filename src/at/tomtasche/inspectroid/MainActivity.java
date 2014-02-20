package at.tomtasche.inspectroid;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;

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
				android.R.layout.simple_list_item_1, requests,
				new String[] { RequestDatabaseHelper.URL },
				new int[] { android.R.id.text1 });

		getListView().setAdapter(cursorAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		((Switch) menu.findItem(R.id.http_switch).getActionView())
				.setOnCheckedChangeListener(this);

		return super.onCreateOptionsMenu(menu);
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
		requestDatabase.close();

		requestDatabase.clear();

		super.onStop();
	}
}
