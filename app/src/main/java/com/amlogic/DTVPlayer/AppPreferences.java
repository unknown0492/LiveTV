package com.amlogic.DTVPlayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.excel.excelclasslibrary.UtilSharedPreferences;
import com.excel.excelclasslibrary.UtilShell;


public class AppPreferences {
	SharedPreferences spfs;
	private static boolean isBackFromTVList;
	
	public AppPreferences( Context context ){
		spfs = UtilSharedPreferences.createSharedPreference( context, "app_preferences" );
	}
	
	public SharedPreferences getSharedPreferences(){
		return spfs;
	}
	
	/*public static AppPreferences getInstance( Context context ){
		return new AppPreferences( context );
	}*/
	
	public void setCurrentChannelID( String channel_id ){
		UtilSharedPreferences.editSharedPreference( spfs, "channel_id", channel_id );
	}
	
	public void setCurrentChannelName( String channel_name ){
		UtilSharedPreferences.editSharedPreference( spfs, "channel_name", channel_name );
	}
	
	public void setCurrentChannelSequence( String channel_sequence ){
		UtilSharedPreferences.editSharedPreference( spfs, "channel_sequence", channel_sequence );
	}
	
	public void setCurrentChannelURL( String channel_url ){
		UtilSharedPreferences.editSharedPreference( spfs, "channel_url", channel_url );
	}
	
	public void setCurrentCategoryID( String category_id ){
		UtilSharedPreferences.editSharedPreference( spfs, "category_id", category_id );
	}
	
	public void setCurrentCategoryName( String category_name ){
		UtilSharedPreferences.editSharedPreference( spfs, "category_name", category_name );
	}
	
	public void setCurrentChannelIcon( String channel_icon ){
		UtilSharedPreferences.editSharedPreference( spfs, "channel_icon", channel_icon );
	}
	
	public void setCurrentCategorySequence( String category_sequence ){
		UtilSharedPreferences.editSharedPreference( spfs, "category_sequence", category_sequence );
	}
	
	public void setMD5( String md5 ){
		UtilSharedPreferences.editSharedPreference( spfs, "md5", md5 );
	}
	
	public void setisFirstTimeRunning( String is_first_time_running ){
		UtilSharedPreferences.editSharedPreference( spfs, "is_first_time_running", is_first_time_running );
	}
	
	public void setAllPreferences( String channel_id, String channel_name, String channel_sequence, String channel_url, String channel_icon, 
			String category_id, String category_name, String category_sequence ){
		
		setCurrentChannelID( channel_id );
		setCurrentChannelName( channel_name );
		setCurrentChannelSequence( channel_sequence );
		setCurrentChannelURL( channel_url );
		setCurrentChannelIcon( channel_icon );
		setCurrentCategoryID( category_id );
		setCurrentCategoryName( category_name );
		setCurrentCategorySequence( category_sequence );
	}
	
	public String getCurrentChannelID(){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "channel_id", "1" );
	}
	
	public String getCurrentChannelName(){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "channel_name", "-1" );
	}
	
	public String getCurrentChannelSequence( ){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "channel_sequence", "-1" );
	}
	
	public String getCurrentChannelURL( ){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "channel_url", "-1" );
	}
	
	public String getCurrentChannelIcon(){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "channel_icon", "-1" );
	}
	
	public String getCurrentCategoryID( ){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "category_id", "-1" );
	}
	
	public String getCurrentCategoryName( ){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "category_name", "-1" );
	}
	
	public String getCurrentCategorySequence( ){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "category_sequence", "-1" );
	}
	
	public String getMD5(){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "md5", "-1" );
	}
	
	public String getisFirstTimeRunning(){
		return (String) UtilSharedPreferences.getSharedPreference( spfs, "is_first_time_running", "1" );
	}

	public static boolean isBackFromTVList() {
		String is_back_from_tv_list = UtilShell.executeShellCommandWithOp( "getprop is_back_from_tv_list" );
		return is_back_from_tv_list.trim().equals( "1" )?true:false;
	}

	public static void setBackFromTVList( boolean isBackFromTVList ) {
		String value = isBackFromTVList?"1":"0";
		UtilShell.executeShellCommandWithOp( "setprop is_back_from_tv_list "+value );
	}
}
