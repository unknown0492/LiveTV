package com.amlogic.DTVPlayer;

import static com.amlogic.DTVPlayer.Constants.SPFS_LAST_CHANNEL_HISTORY;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.excel.excelclasslibrary.UtilSQLite;
import com.excel.excelclasslibrary.UtilSharedPreferences;
import com.excel.excelclasslibrary.UtilShell;

public class TvListSimple extends Activity {
	
	public static final String TAG = "TvListSimple";
	
	ListView list_main;
	Context context = this;
	
	SQLiteDatabase sqldb; 
	SharedPreferences spfs;
	
	public static final int CATEGORY_ID   = 0;
	public static final int CATEGORY_NAME = 1;
	
	public static final int CHANNEL_ID	  		= 0;
	public static final int CHANNEL_SEQUENCE  	= 1;
	public static final int CHANNEL_NAME		= 2;
	public static final int CHANNEL_URL			= 3;
	public static final int CHANNEL_ICON		= 4;
	
	final static int TV_LIST_DISAPPEAR_SECONDS	= 8;
	
	AppPreferences app_prefs;
	
	String[][] main_list_items;
	boolean is_channel_nos_needed = false;
	
	SubListAdapter mla;
	final Handler tvlist_idle_timeout = new Handler();
	long current_timestamp;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_tvlist_simple );
		
		init();
	}
	
	@SuppressLint("ResourceAsColor") 
	private void init(){
		list_main = (ListView) findViewById( R.id.list_main );
		
		app_prefs = new AppPreferences( context );
		
		initializeSQLite();
		// initializeSharedPreferences();
		
		mla = new SubListAdapter( context, main_list_items, true, R.layout.list_item_subitem );
		list_main.setAdapter( mla ); 
		list_main.setBackgroundResource( R.drawable.list_background_blue );
		list_main.getBackground().setAlpha( 75 );  
		
		setOnMainListClickListener();     
		
		// Current Time in Milliseconds when activity is loaded
		current_timestamp = System.currentTimeMillis();
		idleCheckTimer();
		
		// Set as on tv list
		AppPreferences.setBackFromTVList( true );
	}
	
	public void idleCheckTimer(){
		// Start 10 second timer
		tvlist_idle_timeout.postDelayed( new Runnable() {
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				long difference = ( now - current_timestamp )/1000;
				
				//Log.d( TAG, "idleCheckTimer() run after 10 seconds, now : "+now+", current_timestamp : "+current_timestamp+", difference : "+difference );
				
				if( difference < TV_LIST_DISAPPEAR_SECONDS ){
					idleCheckTimer();
				}
				else{
					// Close the TV List
					finish();
					TvListSimple.this.overridePendingTransition( R.anim.show_tv_list_anim, R.anim.hide_tv_list_anim );
				}
			}
			
		}, 5000 );
		
	}
	
	
	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		//Log.d( TAG, "onUserInteraction()" );
		
		current_timestamp = System.currentTimeMillis();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d( TAG, "inside onResume()" );
		
		showCurrentChannelOnTvList();
		
	}
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		//Log.d( TAG, "onBackPressed()" );
		finish();  
		this.overridePendingTransition( R.anim.show_tv_list_anim, R.anim.hide_tv_list_anim );
	
	}

	private void setOnMainListClickListener(){
		list_main.setOnItemClickListener( new OnItemClickListener() { 

			@Override
			public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
				LinearLayout ll = (LinearLayout) view;
				TextView tv_channel_url  = (TextView) ll.findViewById( R.id.tv_channel_url );
				TextView tv_channel_id   = (TextView) ll.findViewById( R.id.tv_channel_id );
				TextView tv_channel_name = (TextView) ll.findViewById( R.id.tv_channel_name );
				
				String tv_ch_url = tv_channel_url.getText().toString();
				String tv_ch_id = tv_channel_id.getText().toString();
				//Log.i( TAG, "position : "+position+", Url : "+tv_ch_url );        
				
				String sql = "SELECT b.sequence as category_sequence, b.id as category_id, b.category_name, a.id as channel_id, a.sequence as channel_sequence, " +
						"a.channel_name, a.channel_url, a.icon as channel_icon FROM channels a, categories b  WHERE a.id = "+ tv_ch_id +" and (b.id=a.category_id)";
				Log.d( TAG, sql );
				Cursor 	 c = UtilSQLite.executeQuery( sqldb, sql, false );
				if( c == null ){
					Toast.makeText( context, "Error occurred !", Toast.LENGTH_LONG ).show();
					tv_ch_url = "";
					return;
				}
				if( c.getCount() == 0 ){
					Toast.makeText( context, "Channel does not exist !", Toast.LENGTH_LONG ).show();
					return;
				}
				c.moveToNext();
				
				Intent intent = new Intent( "switch_channel" );
				intent.putExtra( "url", tv_ch_url );
				intent.putExtra( "channel_number", tv_ch_id );
				
				LocalBroadcastManager.getInstance( TvListSimple.this ).sendBroadcast( intent );
				
				setCurrentChannelNumber( tv_ch_id );
				
				app_prefs.setAllPreferences( c.getString( c.getColumnIndex( "channel_id" ) ), c.getString( c.getColumnIndex( "channel_name" ) ), c.getString( c.getColumnIndex( "channel_sequence" ) ), 
						c.getString( c.getColumnIndex( "channel_url" ) ), c.getString( c.getColumnIndex( "channel_icon" ) ), 
						c.getString( c.getColumnIndex( "category_id" ) ), c.getString( c.getColumnIndex( "category_name" ) ), 
						c.getString( c.getColumnIndex( "category_sequence" ) ) );
				
				showCurrentChannelOnTvList();
			}
			
		});
	}
	
	private void initializeSQLite(){
		sqldb = UtilSQLite.makeDatabase( "tv_channels.db", context ); 
		
		String sql = "SELECT * FROM channels ORDER BY id";
		Cursor 	 c = UtilSQLite.executeQuery( sqldb, sql, false );
		
		if( c == null ){
			Toast.makeText( context, "TV Channels have not yet synchronized !", Toast.LENGTH_LONG ).show();
			finish();
			//return;
		}
		
		main_list_items = new String[ c.getCount() ][ c.getColumnCount() ];
		for( int i = 0 ; i < c.getCount() ; i++ ){
			c.moveToNext();
			main_list_items[ i ][ CHANNEL_ID ] = c.getString( c.getColumnIndex( "id" ) );
			main_list_items[ i ][ CHANNEL_SEQUENCE ] = c.getString( c.getColumnIndex( "sequence" ) );
			main_list_items[ i ][ CHANNEL_NAME ] = c.getString( c.getColumnIndex( "channel_name" ) );
			main_list_items[ i ][ CHANNEL_URL ] = c.getString( c.getColumnIndex( "channel_url" ) );
			main_list_items[ i ][ CHANNEL_ICON ] = c.getString( c.getColumnIndex( "icon" ) );
		}		
	}
	
	private void initializeSharedPreferences(){
		spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_LAST_CHANNEL_HISTORY );
	}
	
	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event ) {
		Log.i( TAG, "Key pressed : "+event.getKeyCode() );
		
		int code = event.getKeyCode();
		
		if( ( code == 20 ) ){ // Down pressed and Last element in the list has been reached
			list_main.setSelected( true ); 
			list_main.setSelection( 0 );
			
			return true;
		}
				
		
		return super.onKeyDown( keyCode, event );
	}

	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		sqldb.close();
	}

	
	public static String getCurrentChannelNumber(){
		String channel_number = UtilShell.executeShellCommandWithOp( "getprop current_ch_number" );
		return channel_number.trim();
	}
	
	public static void setCurrentChannelNumber( String channel_number ){
		UtilShell.executeShellCommandWithOp( "setprop current_ch_number "+channel_number );
	}
	
	public void showCurrentChannelOnTvList(){
		String ch[] = getFirstChannelNameAndImage();
		spfs = app_prefs.getSharedPreferences();
		String channel_name = spfs.getString( "channel_name", ch[ 0 ] );
		String icon = spfs.getString( "icon", ch[ 1 ] );
		Log.d( TAG, "channel_name : "+channel_name+", channel_name1 : "+ch[ 0 ] );
		
		if( ! channel_name.equals( ch[ 0 ] ) ){
			icon = app_prefs.getCurrentChannelIcon(); //c.getString( c.getColumnIndex( "icon" ) );
		}
		//LinearLayout ll = (LinearLayout) View.inflate( context, R.layout.tv_ch_info, null );
		TextView tv_channel_name = (TextView) findViewById( R.id.tv_channel_name1 );
		tv_channel_name.setText( channel_name );
		
		ImageView iv_channel_icon = (ImageView) findViewById( R.id.iv_channel_icon1 );
		int resID = context.getResources().getIdentifier( icon, "drawable",  context.getPackageName() );
		iv_channel_icon.setBackgroundResource( resID );
	}
	
	public String[] getFirstChannelNameAndImage(){
		String sql = "SELECT * FROM channels WHERE id = 1";
		Cursor 	 c = UtilSQLite.executeQuery( sqldb, sql, false );
		if( c == null ){
			Toast.makeText( context, "TV Channels have not yet synchronized !", Toast.LENGTH_LONG ).show();
			return null;
		}
		if( c.getCount() == 0 ){
			Toast.makeText( context, "Channel 1 does not exist !", Toast.LENGTH_LONG ).show();
			return null;
		} 
		c.moveToNext();
		return new String[]{ c.getString( c.getColumnIndex( "channel_name" ) ), c.getString( c.getColumnIndex( "icon" ) ) };
	}
}
