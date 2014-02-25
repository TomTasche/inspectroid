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

	protected static boolean running;
	protected static boolean filtering;

	private static final int notificationId = 1993;

	private int blockedRequests = 0;

	private ProxyLight proxy;

	private RequestDatabaseManager requestDatabase;
	private NotificationManager notificationManager;

	private Handler handler;

	@Override
	public void onCreate() {
		super.onCreate();

		requestDatabase = new RequestDatabaseManager(this);
		requestDatabase.initialize(true);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		handler = new Handler();

		try {
			proxy = new ProxyLight();
			proxy.setPort(8080);
			proxy.getRequestFilters().add(new RequestFilter() {

				@Override
				public boolean filter(final Request request) {
					boolean filter = request.getPort() != 443;
					if (filter) {
						requestDatabase.addRequest(request);
					}

					if (!filtering) {
						return false;
					}

					if (filter) {
						// TODO: use messages instead
						handler.post(new Runnable() {

							@Override
							public void run() {
								updateNotification(request);
							}
						});
					}

					return filter;
				}
			});
			proxy.start();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e.getMessage());
		}

		Notification notification = new Notification();
		notification.icon = R.drawable.ic_notification;
		notification.when = System.currentTimeMillis();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "waiting for connections",
				"proxy waiting for connections at localhost:8080",
				pendingIntent);

		startForeground(notificationId, notification);

		running = true;
	}

	private void updateNotification(Request request) {
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_notification;
		notification.number = ++blockedRequests;
		notification.when = System.currentTimeMillis();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "insecure requests blocked",
				"lots of insecure requests blocked! :)", pendingIntent);

		notificationManager.notify(notificationId, notification);
	}

	@Override
	public void onDestroy() {
		running = false;

		requestDatabase.close();

		proxy.stop();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
