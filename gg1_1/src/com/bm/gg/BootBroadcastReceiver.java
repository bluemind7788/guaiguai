package com.bm.gg;

import com.bm.constant.GGConstant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
	static final String action_boot = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(action_boot)) {
			Intent recogIntent = new Intent(GGConstant.INTENT_RECOG_SERVICE);
			context.startService(recogIntent);
		}

	}

}