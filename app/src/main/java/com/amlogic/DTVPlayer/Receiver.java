package com.amlogic.DTVPlayer;

import com.excel.excelclasslibrary.UtilSharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
	
	public static final String TAG = "Receiver";
	SharedPreferences spfs;
	
	@Override
	public void onReceive( Context context, Intent intent ) {
		String action = intent.getAction();
		spfs = UtilSharedPreferences.createSharedPreference( context, "md5" );
		
		if( action.equals( "download_iptv_channels" ) ){
			Log.i( TAG, "action : "+action );
			
			Intent in = new Intent( context, TVChannelDownloaderService.class );
			context.startService( in );
			
		}
		else if( action.equals( "android.net.conn.CONNECTIVITY_CHANGE" ) || action.equals( "CONNECTIVITY_CHANGE" ) ){
			Log.i( TAG, "action : "+action );
			
			context.sendBroadcast( new Intent( "download_iptv_channels" ) );
		}
	}

}
