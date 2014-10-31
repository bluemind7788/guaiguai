package com.example.gg1_0;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class ServiceUtil {
	public static void startRecogService(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		boolean isServiceRunning = false;
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.example.gg1_0.RecogService".equals(service.service
					.getClassName()))

			{
				isServiceRunning = true;
			}
			if (!isServiceRunning) {
				Intent recogIntent = new Intent(
						"com.example.gg1_0.RecogService");
				context.startService(recogIntent);
			}
		}

	}
}
