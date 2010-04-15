package com.camangi.android.TGMDialer;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

public class TGMService extends Service {
	
	private static final String LOG_TAG = "TGMService";

	private static TGM tgm;
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		
		if(tgm == null){
		
			tgm = TGM.getInstance(this);
		
		}
		
	}

	@Override
	public void onRebind(Intent intent) {
		
		super.onRebind(intent);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		super.onStart(intent, startId);
		
		Log.d(LOG_TAG, "onService");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		
		return null;
	}

}
