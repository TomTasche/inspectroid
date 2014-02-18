package at.tomtasche.inspectroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView requestsText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(this, ProxyService.class));

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
}
