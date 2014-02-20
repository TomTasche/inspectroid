package at.tomtasche.inspectroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.mba.proxylight.ProxyLight;
import com.mba.proxylight.Request;
import com.mba.proxylight.RequestFilter;

public class ProxyService extends Service {

	private static final int notificationId = 1993;

	private int blockedRequests = 0;

	private ProxyLight proxy;

	private Handler handler;
	private RequestDatabaseManager requestDatabase;
	private NotificationManager notificationManager;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.e("smn", "ProxyService started");

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
			proxy.start();
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException(e.getMessage());
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
		requestDatabase.close();
		
		proxy.stop();

		Log.e("smn", "ProxyService stopped");
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
