package com.amlogic.DTVPlayer;

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

import static com.amlogic.DTVPlayer.Constants.SPFS_LAST_CHANNEL_HISTORY;

public class TvList extends Activity {
	
	public static final String TAG = "TvList";
	
	ListView list_main;
	ListView list_sub;
	Context context = this;
	
	public static int focus = 0;
	public static int FOCUSSED_LEFT = 0;
	public static int FOCUSSED_RIGHT = 1;
	
	SQLiteDatabase sqldb; 
	SharedPreferences spfs;
	
	ImageView iv_left_arrow, iv_right_arrow;
	TextView tv_category_name;
	
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
	String[] sub_list_items;
	boolean is_channel_nos_needed = false;
	
	String[] s1;
	String[] s2;
	String[] s3;
	
	SubListAdapter sla;
	MainListAdapter mla;
	final Handler tvlist_idle_timeout = new Handler();
	long current_timestamp;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_tvlist );
		
		init();
	}
	
	@SuppressLint("ResourceAsColor") 
	private void init(){
		list_main = (ListView) findViewById( R.id.list_main );
		list_sub  = (ListView) findViewById( R.id.list_sub ); 
		
		sub_list_items = new String[]{ "One", "Two", "Three", "Four", "One", "Two", "Three", "Four", "One", "Two", "Three", "Four", "One", "Two", "Three", "Four", "One", "Two", "Three", "Four", "One", "Two", "Three", "Four" };
		
		app_prefs = new AppPreferences( context );
		
		initializeSQLite();
		// initializeSharedPreferences();
		
		s1 = new String[]{ "aaa", "bbb", "ccc" };
		s2 = new String[]{ "ddd", "eee", "fff" };
		s3 = new String[]{ "ggg", "hhh", "zzz" }; 
		
		mla = new MainListAdapter( context, main_list_items, R.layout.list_item );
		list_main.setAdapter( mla ); 
		list_main.setBackgroundResource( R.drawable.list_background_blue );
		list_main.getBackground().setAlpha( 75 );  
		
		list_sub.setAdapter( sla );
		
		iv_left_arrow = (ImageView) findViewById( R.id.iv_left_arrow );
		iv_right_arrow = (ImageView) findViewById( R.id.iv_right_arrow );
		tv_category_name = (TextView) findViewById( R.id.tv_category_name );
		
		setOnMainListClickListener();     
		setOnSubListClickListener();     
		
		setOnMainListFocusChangedListener();
		setOnSubListFocusChangedListener();
		  
		list_main.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
				showSubList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
			
		});
		
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
					TvList.this.overridePendingTransition( R.anim.show_tv_list_anim, R.anim.hide_tv_list_anim );
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

	public int index_main_list = 0;
	
	private void setOnMainListFocusChangedListener(){
		// list_main.setSelection( -1 );
		list_main.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange( View v, boolean hasFocus ) {
				ListView lv = (ListView) v;
				
				if( hasFocus ){
					list_main.setSelection( index_main_list );
					switchFocus( FOCUSSED_LEFT );
					animateSubListOut();
					showSubList();	
				}
				else{
					index_main_list = list_main.getSelectedItemPosition();
					//Log.i( TAG, "Focus lost from main List, main list position : "+index_main_list );   
					switchFocus( FOCUSSED_RIGHT );
				}
			}
		});
	}
	
	private void setOnSubListFocusChangedListener(){
		list_sub.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange( View v, boolean hasFocus ) {
				if( hasFocus ){
					//Log.i( TAG, "Focus gained on sub List" );        
					switchFocus( FOCUSSED_RIGHT );
					// Log.i( TAG, "Position of focussed item on main list : "+position );
				}
				else{
					switchFocus( FOCUSSED_LEFT );
					//Log.i( TAG, "Focus lost from Sub List" );
				}
			}
		});
	}
	
	private void setOnMainListClickListener(){
		list_main.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//Log.i( TAG, "setOnItemSelectedListener called" );
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
			
		});
	}
	
	private void setOnSubListClickListener(){
		list_sub.setOnItemClickListener( new OnItemClickListener() { 

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
				
				LocalBroadcastManager.getInstance( TvList.this ).sendBroadcast( intent );
				
				// Store the data into spfs
				/*UtilSharedPreferences.editSharedPreference( spfs, "channel_id", tv_channel_id.getText().toString() );
				UtilSharedPreferences.editSharedPreference( spfs, "channel_url", tv_ch_url );
				UtilSharedPreferences.editSharedPreference( spfs, "channel_name", tv_channel_name.getText().toString() );
				UtilSharedPreferences.editSharedPreference( spfs, "channel_position", String.valueOf( position ) );
				UtilSharedPreferences.editSharedPreference( spfs, "category_position", String.valueOf( list_main.getSelectedItemPosition() ) );*/
				
				setCurrentChannelNumber( tv_ch_id );
				
				/*app_prefs.setCurrentChannelID( tv_channel_id.getText().toString() );
				app_prefs.setCurrentChannelURL( tv_ch_url );
				app_prefs.setCurrentChannelName( tv_channel_name.getText().toString() );
				app_prefs.setCurrentChannelSequence( String.valueOf( position + 1 ) );
				app_prefs.setCurrentCategorySequence( String.valueOf( list_main.getSelectedItemPosition() + 1 ) );*/
				
				app_prefs.setAllPreferences( c.getString( c.getColumnIndex( "channel_id" ) ), c.getString( c.getColumnIndex( "channel_name" ) ), c.getString( c.getColumnIndex( "channel_sequence" ) ), 
						c.getString( c.getColumnIndex( "channel_url" ) ), c.getString( c.getColumnIndex( "channel_icon" ) ), 
						c.getString( c.getColumnIndex( "category_id" ) ), c.getString( c.getColumnIndex( "category_name" ) ), 
						c.getString( c.getColumnIndex( "category_sequence" ) ) );
				
				
				/*Intent in = new Intent( TvList.this, DTVPlayer.class );
				in.putExtra( "url", tv_ch_url );
				startActivity( in );*/
				finish();
				showCurrentChannelOnTvList();
			}
			
		});
	}
	
	
	
	
	private void initializeSQLite(){
		sqldb = UtilSQLite.makeDatabase( "tv_channels.db", context ); 
		
		String sql = "SELECT count(*) FROM categories as count";
		Cursor 	 c = UtilSQLite.executeQuery( sqldb, sql, false );
		
		if( c == null ){
			Toast.makeText( context, "TV Channels have not yet synchronized !", Toast.LENGTH_LONG ).show();
			finish();
			//return;
		}
		
		int no_of_categories = c.getCount();
		
		// sql = "SELECT * FROM categories a, channels b WHERE a.id = b.category_id";
		sql = "SELECT * FROM categories ORDER BY sequence";
		c	= UtilSQLite.executeQuery( sqldb, sql, false );
		
		main_list_items = new String[ c.getCount() ][ 2 ];
		for( int i = 0 ; i < c.getCount() ; i++ ){
			c.moveToNext();
			main_list_items[ i ][ CATEGORY_NAME ] = c.getString( c.getColumnIndex( "category_name" ) );
			main_list_items[ i ][ CATEGORY_ID ] = c.getString( c.getColumnIndex( "id" ) );
			// Log.e( TAG, String.format( "cat-name : %s, id : %s", main_list_items[ i ][ CATEGORY_NAME ], main_list_items[ i ][ CATEGORY_ID ] ) );
		}
		
	}
	
	private void initializeSharedPreferences(){
		spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_LAST_CHANNEL_HISTORY );
	}
	
	int LAST_MAIN_LIST_VALUE = 0;
	Parcelable state;
	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event ) {
		Log.i( TAG, "Key pressed : "+event.getKeyCode() );
		
		int code = event.getKeyCode();
		
		if( ( code == 21 ) && ( focus == FOCUSSED_RIGHT ) ){ // Left Pressed && FOCUSSED_RIGHT
			// list_main.setSelectionFromTop( index_main_list, 0 ); 
			// Hide the sub list
			//list_sub.setVisibility( View.INVISIBLE );
			animateSubListOut();
			showSubList();	
			// list_main.setSelection( LAST_MAIN_LIST_VALUE );
			// Log.d( TAG, "LAST_MAIN_LIST_VALUE : "+LAST_MAIN_LIST_VALUE );
			// Log.d( TAG, "index_main_list : "+index_main_list );
			// showSubList();	
			
			// Restore previous state (including selected item index and scroll position)
		    if(state != null) {
		        //Log.d(TAG, "trying to restore listview state..");
		        list_main.onRestoreInstanceState(state);
		        list_main.requestFocus();
		    }
		    return true;
		}
		else if( ( code == 22 ) && ( focus == FOCUSSED_LEFT ) ){ // RIGHT Pressed && FOCUSSED_LEFT
			// list_main.setSelectionFromTop( index_main_list, 0 ); 
			// Show the sub list
			showSubList();
			animateSubListIn();
			state = list_main.onSaveInstanceState();
			// return true;
			
		}
		else if( ( code == 20 ) && ( focus == FOCUSSED_RIGHT ) ){ // Down pressed && FOCUSSED_RIGHT -> Last element in the list has been reached
			list_sub.setSelected( true ); 
			list_sub.setSelection( 0 );
			
			return true;
		}
		/*else if( ( code == 20 ) && ( focus == FOCUSSED_LEFT ) ){ // Up pressed && FOCUSSED_RIGHT
			
		}*/
		/*else if( code == 4 ){ // Back pressed
			Intent in = new Intent( context, DTVPlayer.class );
			startActivity( in ); 
			finish();
			return true;
		}*/
		
		
		return super.onKeyDown( keyCode, event );
	}

	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event ) {
		//Log.i( TAG, "Key pressed : "+event.getKeyCode() );
		
		int code = event.getKeyCode();
		
		return super.onKeyUp(keyCode, event);
	}
	
	public void switchFocus( int switch_to ){
		if( switch_to == FOCUSSED_LEFT ){
			focus = 0;
			// list_sub.setVisibility( View.INVISIBLE );
		}
		else if( switch_to == FOCUSSED_RIGHT ){
			focus = 1;
			// list_sub.setVisibility( View.VISIBLE );
		}
		else
			focus = 0;
	}
	
	public void showSubList(){
		//list_sub.setVisibility( View.VISIBLE );
		String position = String.valueOf( list_main.getSelectedItemPosition() );
		//Log.e(TAG, "pos : "+position );
		
		if( position.equals( "-1" ) )
			position = "0";
		
		tv_category_name.setText( main_list_items[ Integer.parseInt( position ) ][ CATEGORY_NAME ] );
		
		String sql = "";
		if( main_list_items[ Integer.parseInt( position ) ][ CATEGORY_ID ].equals( "0" ) )
			sql = "Select * FROM channels ORDER BY id";
		else
			sql = "Select * FROM channels WHERE category_id ="+main_list_items[ Integer.parseInt( position ) ][ CATEGORY_ID ]+" ORDER BY sequence";
		
		//Log.i( TAG, "sql : "+sql );
		Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
		
		String[][] temp = new String[ c.getCount() ][ c.getColumnCount() ];
		
		if( position.equals( "0" ) ){
			sla  = new SubListAdapter( context, temp, false, R.layout.list_item_subitem );
		}
		else{
			sla  = new SubListAdapter( context, temp, true, R.layout.list_item_subitem );
		}
		
		for( int i = 0 ; i < c.getCount() ; i++ ){
			c.moveToNext();
			temp[ i ][ CHANNEL_ID ] = c.getString( c.getColumnIndex( "id" ) );
			temp[ i ][ CHANNEL_SEQUENCE ] = c.getString( c.getColumnIndex( "sequence" ) );
			temp[ i ][ CHANNEL_NAME ] = c.getString( c.getColumnIndex( "channel_name" ) );
			temp[ i ][ CHANNEL_URL ] = c.getString( c.getColumnIndex( "channel_url" ) );
			temp[ i ][ CHANNEL_ICON ] = c.getString( c.getColumnIndex( "icon" ) );
		}
		//}
		/*else{
			for( int i = 0 ; i < c.getCount() ; i++ ){
				c.moveToNext();
				temp[ i ][ CHANNEL_ID ] = "";
				temp[ i ][ CHANNEL_SEQUENCE ] = c.getString( c.getColumnIndex( "sequence" ) );
				temp[ i ][ CHANNEL_NAME ] = c.getString( c.getColumnIndex( "channel_name" ) );
				temp[ i ][ CHANNEL_URL ] = c.getString( c.getColumnIndex( "channel_url" ) );
				temp[ i ][ CHANNEL_ICON ] = c.getString( c.getColumnIndex( "icon" ) );
			}
		}*/
		
		
		
		list_sub.setAdapter( sla );
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		sqldb.close();
	}

	public void animateSubListIn(){
		// Animate Sub-List In
		ObjectAnimator transAnimation = ObjectAnimator.ofFloat( list_sub, "translationX", -200 );
		transAnimation.setDuration( 500 );//set duration
		transAnimation.start();//start animation
		
		ObjectAnimator transAnimation1 = ObjectAnimator.ofFloat( tv_category_name, "translationX", -200 );
		transAnimation1.setDuration( 500 );//set duration
		transAnimation1.start();//start animation
		
		// Animation anim_in = AnimationUtils.loadAnimation( context, R.anim.sub_list_in );
		// list_sub.startAnimation( anim_in );
		
		// Make Sub-list Blue background
		list_sub.setBackgroundResource( R.drawable.list_background_blue );
		list_sub.getBackground().setAlpha( 75 );
		
		// Make Main-list Alpha 0
		ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat( list_main, "alpha", 1.0f, 0.0f );
		alphaAnimation.setDuration( 500 );//set duration
		alphaAnimation.start();//start animation
		
		// list_main.setLayoutParams( new LayoutParams( 100, list_main.getHeight() ) );
		ObjectAnimator arrowAnimation = ObjectAnimator.ofFloat( iv_left_arrow, "alpha", 0.0f, 1.0f );
		arrowAnimation.setDuration( 500 );//set duration
		arrowAnimation.start();//start animation
		
		ObjectAnimator arrowAnimation1 = ObjectAnimator.ofFloat( iv_right_arrow, "alpha", 1.0f, 0.0f );
		arrowAnimation1.setDuration( 500 );//set duration
		arrowAnimation1.start();//start animation
	}
	
	public void animateSubListOut(){
		// Animate Sub-List In
		ObjectAnimator transAnimation = ObjectAnimator.ofFloat( list_sub, "translationX", 0 );
		transAnimation.setDuration( 600 );//set duration
		transAnimation.start();//start animation
		
		ObjectAnimator transAnimation1 = ObjectAnimator.ofFloat( tv_category_name, "translationX", 0 );
		transAnimation1.setDuration( 600 );//set duration
		transAnimation1.start();//start animation
		
		ObjectAnimator transAnimation2 = ObjectAnimator.ofFloat( iv_right_arrow, "translationX", 0 );
		transAnimation2.setDuration( 600 );//set duration
		transAnimation2.start();//start animation
		
		
		// Animation anim_in = AnimationUtils.loadAnimation( context, R.anim.sub_list_in );
		// list_sub.startAnimation( anim_in );
		
		// Make Sub-list Transparent background
		list_sub.setBackgroundResource( R.drawable.sub_list_background );
		list_sub.getBackground().setAlpha( 0 );
		
		// Make Main-list Alpha 1
		ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat( list_main, "alpha", 0.0f, 1.0f );
		alphaAnimation.setDuration( 500 );//set duration
		alphaAnimation.start();//start animation
		
		// Make Main List Blue Background
		list_main.setBackgroundResource( R.drawable.list_background_blue );
		list_main.getBackground().setAlpha( 75 );
		
		ObjectAnimator arrowAnimation = ObjectAnimator.ofFloat( iv_left_arrow, "alpha", 1.0f, 0.0f );
		arrowAnimation.setDuration( 500 );//set duration
		arrowAnimation.start();//start animation
		
		ObjectAnimator arrowAnimation1 = ObjectAnimator.ofFloat( iv_right_arrow, "alpha", 0.0f, 1.0f );
		arrowAnimation1.setDuration( 500 );//set duration
		arrowAnimation1.start();//start animation
		
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
			/*String channel_id = app_prefs.getCurrentChannelID();
			String sql = "SELECT * FROM channels WHERE id = "+channel_id;
			Cursor 	 c = UtilSQLite.executeQuery( sqldb, sql, false );
			if( c == null ){
				Toast.makeText( context, "TV Channels have not yet synchronized !", Toast.LENGTH_LONG ).show();
				return ;
			}
			if( c.getCount() == 0 ){
				Toast.makeText( context, "Channel 1 does not exist !", Toast.LENGTH_LONG ).show();
				return ;
			} 
			c.moveToNext();*/
			icon = app_prefs.getCurrentChannelIcon(); //c.getString( c.getColumnIndex( "icon" ) );
		}
		//LinearLayout ll = (LinearLayout) View.inflate( context, R.layout.tv_ch_info, null );
		TextView tv_channel_no1 = (TextView) findViewById( R.id.tv_channel_no1 );
		tv_channel_no1.setText( app_prefs.getCurrentChannelID() );
		
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
