package com.example.gg1_0;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot="android.intent.action.BOOT_COMPLETED";
 
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)){
        	Intent recogIntent = new Intent("com.example.gg1_0.RecogService");
            context.startService(recogIntent);
            
//            Intent sayHelloIntent=new Intent("com.example.gg1_0.MainActivity");
//            sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(sayHelloIntent);
        }
 
    }
 
}