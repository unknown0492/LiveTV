package com.amlogic.DTVPlayer;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.excel.excelclasslibrary.UtilFile;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilSQLite;
import com.excel.excelclasslibrary.UtilSharedPreferences;
import com.excel.excelclasslibrary.UtilShell;

public class TVChannelDownloaderService extends Service {
	
	static final String TAG = "TVChannelDownloaderService";
	Context context = this;
	SharedPreferences spfs;
	
	@Override
	public IBinder onBind( Intent intent ) {
		return null;
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) {
		Log.i( TAG, "inside the TVChannelDownloader Service !" );
		
		spfs = UtilSharedPreferences.createSharedPreference( context, "md5" );
		
		// Check if tv channels were downloaded in this session
		String is_iptv_channels_synced = UtilShell.executeShellCommandWithOp( "getprop is_iptv_channels_synced" );
		if( is_iptv_channels_synced.trim().equals( "1" ) ){
			// quit
		}
		else
			startDownloadingTvChannels();
		
		return START_NOT_STICKY;
	}
	
	public void startDownloadingTvChannels(){
		new AsyncTask< Void, Void, String >(){
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				
			}
			
			@Override
			protected String doInBackground( Void... params ) {
				String cms_ip = UtilFile.getCMSIpFromTextFile();
				String URL = String.format( "http://%s/appstv/webservice.php", cms_ip );
				
				Log.d( TAG, URL );
				
				String result = UtilNetwork.makeRequestForData( URL, "POST", "what_do_you_want=get_iptv_channels&mac_address="+UtilNetwork.getMacAddress( context ) );
				
				return result;
			}
			
			@Override
			protected void onPostExecute( String result ) {
				super.onPostExecute( result );
				
				if( result == null ){
					Toast.makeText( context, "Cant sync TV Channels !", 5000 ).show();
					
					// Set a Timer to try after 30 seconds
					setRetryTimer();
					return;
				}
				
				Log.d( TAG, result );
				try {
					JSONObject jsonObject = new JSONObject( result );
					String md5 = jsonObject.getString( "md5" );
					
					String old_md5 = (String) UtilSharedPreferences.getSharedPreference( spfs, "md5", "" );
					if( old_md5.equals( "" ) ){
						createTvChannelsDatabase();
					}
					
					Log.d( TAG, String.format( "old md5 : %s, new md5 : %s", old_md5, md5 ) );
					
					
					/*if( ! old_md5.equals( "" ) && old_md5.equals( md5 ) ){
						// No need to update
						return;
					}*/
					
					// update the tv list
					UtilSharedPreferences.editSharedPreference( spfs, "md5", md5 );
					
					JSONArray categories = jsonObject.getJSONArray( "categories" );
					JSONArray channels = jsonObject.getJSONArray( "channels" );
					
					// Get Database Instance
					SQLiteDatabase sqldb = UtilSQLite.makeDatabase( "tv_channels.db", context );
					// Truncate the tables
					UtilSQLite.executeQuery( sqldb, "DELETE FROM categories", true );
					UtilSQLite.executeQuery( sqldb, "DELETE FROM channels", true );
					
					String sql1 = "INSERT INTO categories( id, sequence, category_name ) VALUES";
					for( int i = 0 ; i < categories.length() ; i++ ){
						jsonObject = categories.getJSONObject( i );
						sql1 += String.format( "( %s, %s, '%s' ),", jsonObject.getString( "id" ), jsonObject.getString( "sequence" ), jsonObject.getString( "category_name" ) );
					}
					sql1 = sql1.substring( 0, sql1.length() - 1 );
					Log.d( TAG, sql1 );
					
					String sql2 = "INSERT INTO channels( id, category_id, sequence, icon, channel_name, channel_url ) VALUES";
					for( int i = 0 ; i < channels.length() ; i++ ){
						jsonObject = channels.getJSONObject( i );
						sql2 += String.format( "( %s, %s, %s, '%s', '%s', '%s' ),", jsonObject.getString( "id" ), jsonObject.getString( "category_id" ), jsonObject.getString( "sequence" ), jsonObject.getString( "icon" ), jsonObject.getString( "channel_name" ), jsonObject.getString( "channel_url" ) );
					}
					sql2 = sql2.substring( 0, sql2.length() - 1 );
					Log.d( TAG, sql2 );
					
					// Insert new values in the database
					UtilSQLite.executeQuery( sqldb, sql1, true );
					UtilSQLite.executeQuery( sqldb, sql2, true );
					
					UtilShell.executeShellCommandWithOp( "setprop is_iptv_channels_synced 1" );
					
					// Restart TV DTVPlayer
					
				} 
				catch ( JSONException e ) {
					e.printStackTrace();
				}
				
				
			}
			
		}.execute();
	}
	
	public void setRetryTimer(){
		new Handler().postDelayed( new Runnable() {
			
			@Override
			public void run() {
				Intent in = new Intent( context, TVChannelDownloaderService.class );
				startService( in );
			}
		}, 30000 );
	}
	
	public void createTvChannelsDatabase(){
		SQLiteDatabase sqldb = UtilSQLite.makeDatabase( "tv_channels.db", context );
		String sql1 = "CREATE TABLE categories (sequence NUMERIC, id INTEGER PRIMARY KEY, category_name TEXT)";
		String sql2 = "CREATE TABLE channels (icon TEXT, category_id NUMERIC, id INTEGER PRIMARY KEY, sequence NUMERIC, channel_name TEXT, channel_url TEXT)";
		// UtilSQLite.executeQuery( sqldb, sql1, false );
		// UtilSQLite.executeQuery( sqldb, sql2, false );
		sqldb.execSQL( sql1 );
		sqldb.execSQL( sql2 );
	}

}
