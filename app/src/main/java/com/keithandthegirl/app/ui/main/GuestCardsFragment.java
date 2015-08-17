package com.keithandthegirl.app.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.DatabaseHelper;
import com.keithandthegirl.app.db.model.GuestConstants;
import com.keithandthegirl.app.ui.CursorRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GuestCardsFragment extends Fragment {

    private static final String RAW_GUESTS_QUERY =
            "SELECT distinct " +
                    "    g._id, g.realname, g.pictureurl, g.description, count( eg.showguestid) as count " +
                    "FROM " +
                    "    guest g left join episode_guests eg on g._id = eg.showguestid " +
                    "group by " +
                    "    eg.showguestid " +
                    "order by " +
                    "    eg.showid desc";

    @Bind(R.id.guestCardRecyclerView)
    RecyclerView mGuestCardRecyclerView;

    DatabaseHelper dbHelper;
    Cursor cursor;
    GuestCursorAdapter mAdapter;

    public static GuestCardsFragment newInstance() {
        GuestCardsFragment fragment = new GuestCardsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GuestCardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_cards, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new DatabaseHelper(getActivity());
        cursor = dbHelper.getReadableDatabase().rawQuery(RAW_GUESTS_QUERY, null);

        mAdapter = new GuestCursorAdapter(getActivity(), cursor);
        mGuestCardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGuestCardRecyclerView.setAdapter(mAdapter);
    }

    public class GuestCursorAdapter extends CursorRecyclerViewAdapter<GuestCursorAdapter.ViewHolder> {

        public GuestCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.guest_image)
            ImageView image;
            @Bind(R.id.guest_real_name)
            TextView realName;
            @Bind(R.id.guest_episodes)
            TextView episodes;
            @Bind(R.id.guest_description)
            TextView description;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item_guest, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            String pictureUrl = cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_PICTUREURL));
            if (null != pictureUrl && !"".equals(pictureUrl)) {
                viewHolder.image.setVisibility(View.VISIBLE);
                Picasso.with(getActivity()).load(pictureUrl).fit().centerCrop().into(viewHolder.image);
            } else {
                viewHolder.image.setVisibility(View.INVISIBLE);
            }
            viewHolder.description.setText(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_DESCRIPTION)));
            viewHolder.realName.setText(cursor.getString(cursor.getColumnIndex(GuestConstants.FIELD_REALNAME)));
            viewHolder.episodes.setText("Episodes: " + cursor.getString(cursor.getColumnIndex("count")));
        }
    }
}
