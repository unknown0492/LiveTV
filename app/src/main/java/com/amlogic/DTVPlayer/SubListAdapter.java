package com.amlogic.DTVPlayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SubListAdapter extends BaseAdapter {

	Context context;
	String[][] sub_list_items;
	int resId;
	boolean is_channel_nos_needed;
	
	public SubListAdapter( Context context, String[][] sub_list_items, boolean is_channel_nos_needed, int resId ){
		this.context = context;
		this.sub_list_items= sub_list_items;
		this.resId = resId;
		this.is_channel_nos_needed = is_channel_nos_needed;
	}
	
	@Override
	public int getCount() {
		return sub_list_items.length;
	}

	@Override
	public Object getItem( int position ) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView( final int position, View convertView, ViewGroup parent ) {
		LinearLayout ll = (LinearLayout) View.inflate( context, resId, null );
		
		TextView tv_channel_name = (TextView) ll.findViewById( R.id.tv_channel_name );
		tv_channel_name.setText( sub_list_items[ position ][ TvList.CHANNEL_NAME ] );
		
		TextView tv_channel_number = (TextView) ll.findViewById( R.id.tv_channel_number );
		tv_channel_number.setText( sub_list_items[ position ][ TvList.CHANNEL_ID ] + "" );
		
		TextView tv_channel_url = (TextView) ll.findViewById( R.id.tv_channel_url );
		tv_channel_url.setText( sub_list_items[ position ][ TvList.CHANNEL_URL ] );
		
		TextView tv_channel_id = (TextView) ll.findViewById( R.id.tv_channel_id );
		tv_channel_id.setText( sub_list_items[ position ][ TvList.CHANNEL_ID ] );
		
		if( is_channel_nos_needed ){
			tv_channel_number.setVisibility( View.GONE );
		}
		// Log.i( null, ","+sub_list_items[ position ][ TvList.CHANNEL_ICON ]+"," );
		
		ImageView iv_channel_icon = (ImageView) ll.findViewById( R.id.iv_channel_icon );  
		int resID = context.getResources().getIdentifier( sub_list_items[ position ][ TvList.CHANNEL_ICON ], "drawable",  context.getPackageName() );
		iv_channel_icon.setBackgroundResource( resID );
		
		/*ll.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i( null, "clicked "+position );
				
			}
		});*/
		
		return ll;
	}

}
