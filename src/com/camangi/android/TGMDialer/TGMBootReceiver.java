package com.camangi.android.TGMDialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TGMBootReceiver extends BroadcastReceiver {
	
	private static final String LOG_TAG="TGMBootReceiver";
	
	@Override
	public void onReceive(Context cxt, Intent intent) {
		
		Log.d(LOG_TAG,"onReceive");
		
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			
			Log.d(LOG_TAG,"onReceive ACTION_BOOT_COMPLETED start");
			
			Intent startTGMServiceIntent = new Intent(cxt, TGMService.class);
			
			cxt.startService(startTGMServiceIntent);
			
			Log.d(LOG_TAG,"onReceive ACTION_BOOT_COMPLETED end");
		}

	}

}
