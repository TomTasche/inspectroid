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

	private static final int RESTART_INTERVAL = 24 * 60 * 60 * 1000;

	protected static boolean running;
	protected static boolean filtering;

	private static final int notificationId = 1993;

	private int blockedRequests = 0;

	private ProxyLight proxy;
	private RestartProxyRunnable restartRunnable;

	private RequestDatabaseManager requestDatabase;
	private NotificationManager notificationManager;

	private Handler handler;

	@Override
	public void onCreate() {
		super.onCreate();

		restartRunnable = new RestartProxyRunnable();

		requestDatabase = new RequestDatabaseManager(this);
		requestDatabase.initialize(true);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		handler = new Handler();

		Notification notification = new Notification();
		notification.icon = R.drawable.ic_notification;
		notification.when = System.currentTimeMillis();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "waiting for connections",
				"proxy waiting for connections at localhost:8080",
				pendingIntent);

		startForeground(notificationId, notification);

		restartProxy();

		running = true;
	}

	private void restartProxy() {
		stopProxy();
		startProxy();

		handler.postDelayed(restartRunnable, RESTART_INTERVAL);
	}

	private void startProxy() {
		try {
			proxy = new ProxyLight();
			proxy.setPort(8080);
			proxy.getRequestFilters().add(new RequestFilter() {

				@Override
				public boolean filter(final Request request) {
					boolean filter = request.getPort() != 443;

					if (filter) {
						requestDatabase.addRequest(request);

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
	}

	private void stopProxy() {
		if (proxy != null) {
			proxy.stop();
		}
	}

	private void updateNotification(Request request) {
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_notification;
		notification.number = ++blockedRequests;
		notification.when = System.currentTimeMillis();

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		String title;
		String message;
		if (filtering) {
			title = "insecure requests blocked";
			message = "lots of insecure requests blocked! :)";
		} else {
			title = "logging insecure requests";
			message = "not blocking any requests currently";
		}

		notification.setLatestEventInfo(this, title, message, pendingIntent);

		notificationManager.notify(notificationId, notification);
	}

	@Override
	public void onDestroy() {
		running = false;

		stopProxy();

		requestDatabase.close();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class RestartProxyRunnable implements Runnable {

		@Override
		public void run() {
			restartProxy();
		}
	}
}
