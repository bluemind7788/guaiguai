package com.bm.gg.reciever;

import com.bm.gg.constant.GGConstant;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean isServiceRunning = false;
		if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

			// ¼ì²éService×´Ì¬

			ActivityManager manager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager
					.getRunningServices(Integer.MAX_VALUE)) {
				if (GGConstant.INTENT_RECOG_SERVICE.equals(service.service
						.getClassName()))

				{
					isServiceRunning = true;
				}

			}
			if (!isServiceRunning) {
				Intent recogIntent = new Intent(GGConstant.INTENT_RECOG_SERVICE);
				context.startService(recogIntent);
			}

		}
	}

}
