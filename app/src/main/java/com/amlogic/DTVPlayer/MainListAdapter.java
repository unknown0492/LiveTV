package com.amlogic.DTVPlayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainListAdapter extends BaseAdapter {

	Context context;
	String[][] main_list_items;
	int resId;
	
	public MainListAdapter( Context context, String[][] main_list_items, int resId ){
		this.context = context;
		this.main_list_items = main_list_items;
		this.resId = resId;
	}
	
	@Override
	public int getCount() {
		return main_list_items.length;
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
	public View getView( int position, View convertView, ViewGroup parent ) {
		// Log.i( null, "Total items : "+getCount() );
		LinearLayout ll = null;
		if ( convertView == null ) {
            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
            ll = (LinearLayout) inflator.inflate( resId, null );
        } else {
            ll = (LinearLayout) convertView;
        }
		
		// LinearLayout ll = (LinearLayout) View.inflate( context, resId, null );
		ImageView iv_category_icon = (ImageView) ll.findViewById( R.id.iv_category_icon );
		iv_category_icon.setVisibility( View.INVISIBLE );
		
		TextView tv_channel_category_id = (TextView) ll.findViewById( R.id.tv_channel_category_id );
		tv_channel_category_id.setText( main_list_items[ position ][ TvList.CATEGORY_ID ] );
		
		TextView tv_channel_category = (TextView) ll.findViewById( R.id.tv_channel_category );
		tv_channel_category.setText( main_list_items[ position ][ TvList.CATEGORY_NAME ] );
		
		//if( main_list_items[ position ][ TvList.CATEGORY_ID ].equals( "0" ) ){
		if( tv_channel_category_id.getText().toString().trim().equals( "0" ) ){
			// Log.e( null, String.format( "cat-name : %s, id : %s, position : %s", main_list_items[ position ][ TvList.CATEGORY_NAME ], main_list_items[ position ][ TvList.CATEGORY_ID ], position ) );
			iv_category_icon.setBackgroundResource( R.drawable.all_channels_icon );
			iv_category_icon.setVisibility( View.VISIBLE );
			
		}
		 
		 
		/*LinearLayout ActiveItem = (LinearLayout) ll;
		if ( position == selectedItem ){
			int top = (ActiveItem == null) ? 0 : ActiveItem.getTop();
			((ListView) parent).setSelectionFromTop(position, top);
		}
		*/
		return ll;
	}

	private int selectedItem;

    public void setSelectedItem(int position) {
        selectedItem = position;
    }
}
