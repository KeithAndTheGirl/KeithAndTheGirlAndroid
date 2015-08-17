package com.keithandthegirl.app.ui.main;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.sync.EventsDataFragment;
import com.keithandthegirl.app.ui.CursorRecyclerViewAdapter;
import com.keithandthegirl.app.utils.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventCardsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @Bind(R.id.eventCardRecyclerView)
    RecyclerView mEventCardRecyclerView;

    private EventCursorAdapter mAdapter;

    public EventCardsFragment() {
        // Required empty public constructor
    }

    public static EventCardsFragment newInstance() {
        EventCardsFragment fragment = new EventCardsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_cards, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = null;

        String selection = EventConstants.FIELD_ENDDATE + " > ?";

        DateTime now = new DateTime(DateTimeZone.UTC);
        String[] selectionArgs = new String[]{String.valueOf(now.getMillis())};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), EventConstants.CONTENT_URI, projection, selection, selectionArgs, EventConstants.FIELD_ENDDATE);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter = new EventCursorAdapter(getActivity(), cursor);
        mEventCardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventCardRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


    public class EventCursorAdapter extends CursorRecyclerViewAdapter<EventCursorAdapter.ViewHolder> {
        DateTimeFormatter mFormatter = DateTimeFormat.forPattern("MMM d, yyyy hh:mm aa").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));

        public EventCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.event_title)
            TextView title;
            @Bind(R.id.event_start_date)
            TextView startDate;
            @Bind(R.id.event_location)
            TextView location;
            @Bind(R.id.event_details)
            TextView details;
            @Bind(R.id.whenLayout)
            View whenLayout;
            @Bind(R.id.locationLayout)
            View locationLayout;
            @Bind(R.id.detailsLayout)
            View detailsLayout;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item_event, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            long start = cursor.getLong(cursor.getColumnIndex(EventConstants.FIELD_STARTDATE));
            String details = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_DETAILS)).trim();
            String title = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_TITLE));
            String location = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_LOCATION));

            viewHolder.title.setText(title.trim());

            if (start > 0) {
                viewHolder.startDate.setText(mFormatter.print(start));
                viewHolder.whenLayout.setVisibility(View.VISIBLE);;
            } else {
                viewHolder.whenLayout.setVisibility(View.GONE);
            }

            if (!StringUtils.isNullOrEmpty(location)) {
                viewHolder.location.setText(location);
                viewHolder.locationLayout.setVisibility(View.VISIBLE);
            } else {
                viewHolder.locationLayout.setVisibility(View.GONE);
            }

            if (!StringUtils.isNullOrEmpty(details)) {
                viewHolder.details.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolder.details.setText(Html.fromHtml(details));
                viewHolder.detailsLayout.setVisibility(View.VISIBLE);
            } else {
                viewHolder.details.setText("");
                viewHolder.detailsLayout.setVisibility(View.GONE);
            }
        }
    }
}
