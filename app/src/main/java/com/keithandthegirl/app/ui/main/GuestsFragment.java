package com.keithandthegirl.app.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.Bind;

public class GuestsFragment extends ListFragment {
    private static final String TAG = GuestsFragment.class.getSimpleName();

    private static final String RAW_GUESTS_QUERY =
            "SELECT distinct " +
            "    g._id, g.realname, g.pictureurl, g.description, count( eg.showguestid) as count " +
            "FROM " +
            "    guest g left join episode_guests eg on g._id = eg.showguestid " +
            "group by " +
            "    eg.showguestid " +
            "order by " +
            "    eg.showid desc";

    DatabaseHelper dbHelper;
    Cursor cursor;
    GuestCursorAdapter mAdapter;


    public static GuestsFragment newInstance() {
        GuestsFragment fragment = new GuestsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GuestsFragment() { }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new DatabaseHelper( getActivity() );
        cursor = dbHelper.getReadableDatabase().rawQuery( RAW_GUESTS_QUERY, null );

        mAdapter = new GuestCursorAdapter( getActivity(), cursor );
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    private class GuestCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public GuestCursorAdapter( Context context, Cursor cursor ) {
            super( context, cursor, false );

            mInflater = LayoutInflater.from( context );
        }

        @Override
        public View newView( Context context, Cursor cursor, ViewGroup parent ) {
            View view = mInflater.inflate( R.layout.listview_item_guest, parent, false );

            ViewHolder refHolder = new ViewHolder(view);
            view.setTag( refHolder );

            return view;
        }

        @Override
        public void bindView( View view, Context context, Cursor cursor ) {
            ViewHolder mHolder = (ViewHolder) view.getTag();

            String pictureUrl = cursor.getString( cursor.getColumnIndex( GuestConstants.FIELD_PICTUREURL ) );
            if( null != pictureUrl && !"".equals( pictureUrl ) ) {
                mHolder.image.setVisibility( View.VISIBLE );
                Picasso.with(getActivity()).load(pictureUrl).fit().centerCrop().into(mHolder.image);
            } else {
                mHolder.image.setVisibility( View.INVISIBLE );
            }
            mHolder.description.setText(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_DESCRIPTION)));
            mHolder.realName.setText( cursor.getString( cursor.getColumnIndex( GuestConstants.FIELD_REALNAME ) ) );
            mHolder.episodes.setText( "Episodes: " + cursor.getString( cursor.getColumnIndex( "count" ) ) );
        }
    }

    static class ViewHolder {
        @Bind(R.id.guest_image)
        ImageView image;
        @Bind(R.id.guest_real_name)
        TextView realName;
        @Bind(R.id.guest_episodes)
        TextView episodes;
        @Bind(R.id.guest_description)
        TextView description;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
