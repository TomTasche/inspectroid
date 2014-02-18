package at.tomtasche.inspectroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.mba.proxylight.ProxyLight;
import com.mba.proxylight.Request;
import com.mba.proxylight.RequestFilter;

public class ProxyService extends Service {

	private static final int notificationId = 1993;

	private int blockedRequests = 0;

	private Handler handler;
	private RequestDatabaseManager requestDatabase;
	private NotificationManager notificationManager;

	@Override
	public void onCreate() {
		super.onCreate();

		requestDatabase = new RequestDatabaseManager(this);
		requestDatabase.initialize(true);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		handler = new Handler();

		try {
			ProxyLight p = new ProxyLight();
			p.setPort(8080);
			p.getRequestFilters().add(new RequestFilter() {

				@Override
				public boolean filter(final Request request) {
					boolean filter = request.getPort() != 443;
					if (filter) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								createNotification(request);
							}
						});
					}

					return filter;
				}
			});
			p.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createNotification(Request request) {
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.number = ++blockedRequests;
		notification.when = System.currentTimeMillis();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "insecure requests blocked",
				"lots of insecure requests blocked! :)", pendingIntent);

		notificationManager.notify(notificationId, notification);

		requestDatabase.addRequest(request);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		requestDatabase.close();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
