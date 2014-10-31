package com.bm.gg;

import com.bm.constant.GGConstant;

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
			if (GGConstant.INTENT_RECOG_SERVICE.equals(service.service
					.getClassName()))

			{
				isServiceRunning = true;
			}
			if (!isServiceRunning) {
				Intent recogIntent = new Intent(GGConstant.INTENT_RECOG_SERVICE);
				context.startService(recogIntent);
			}
		}

	}
}
