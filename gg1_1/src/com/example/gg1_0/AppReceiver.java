package com.example.gg1_0;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppReceiver extends BroadcastReceiver
{
    private static final int PACKAGE_NAME_START_INDEX = 8;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null)
        {
            return;
        }
        
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
        {
            String data = intent.getDataString();
            
            if(data == null || data.length() <= PACKAGE_NAME_START_INDEX)
            {
                return;
            }
            
            String packageName = data.substring(PACKAGE_NAME_START_INDEX);
            
            if("com.iflytek.speechcloud".equals(packageName))
            {
            	Intent recogIntent = new Intent("com.example.gg1_0.RecogService");
            	context.startService(recogIntent);
            }
        }
        
    }

}