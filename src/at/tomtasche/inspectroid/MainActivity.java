package at.tomtasche.inspectroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private TextView requestsText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		requestsText = (TextView) findViewById(R.id.requestsText);

		RequestDatabaseManager requestDatabase = new RequestDatabaseManager(
				this);
		requestDatabase.initialize(false);

		Cursor requests = requestDatabase.getRequests();

		int urlColumnIndex = requests.getColumnIndex(RequestDatabaseHelper.URL);
		while (requests.moveToNext()) {
			String url = requests.getString(urlColumnIndex);

			requestsText.append(url + System.getProperty("line.separator"));
		}

		requestDatabase.close();

		requestDatabase.clear();
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
			stopService(intent);
		} else {
			startService(intent);
		}
	}
}
