
package com.keithandthegirl.app.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.sync.BroadcastingDataFragment;
import com.keithandthegirl.app.sync.EventsDataFragment;
import com.keithandthegirl.app.utils.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.Bind;

/**
 * Created by dmfrey on 3/21/14.
 * TODO this would look nice as a recyclerView/cardView
 * TODO adding an action to the card for adding a reminder could be a nice feature. but maybe just a push notification would be better
 */
public class EventsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = EventsFragment.class.getSimpleName();
    private static final String EVENTS_DATA_FRAGMENT_TAG = EventsDataFragment.class.getName();

    EventCursorAdapter mAdapter;

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventsDataFragment eventsDataFragment = (EventsDataFragment) getChildFragmentManager().findFragmentByTag( EVENTS_DATA_FRAGMENT_TAG );
        if( null == eventsDataFragment ) {

            eventsDataFragment = (EventsDataFragment) instantiate( getActivity(), EventsDataFragment.class.getName() );
            eventsDataFragment.setRetainInstance( true );

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.add( eventsDataFragment, EVENTS_DATA_FRAGMENT_TAG );
            transaction.commit();

        }

        return super.onCreateView(inflater, container, savedInstanceState);
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
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new EventCursorAdapter(getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    private class EventCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        DateTimeFormatter mFormatter = DateTimeFormat.forPattern("MMM d, yyyy hh:mm aa").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));

        public EventCursorAdapter(Context context) {
            super(context, null, false);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.listview_item_event, parent, false);

            ViewHolder refHolder = new ViewHolder(view);
            view.setTag(refHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder mHolder = (ViewHolder) view.getTag();

            long start = cursor.getLong(cursor.getColumnIndex(EventConstants.FIELD_STARTDATE));
            String details = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_DETAILS)).trim();
            String title = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_TITLE));
            String location = cursor.getString(cursor.getColumnIndex(EventConstants.FIELD_LOCATION));

            mHolder.title.setText(title.trim());

            if (start > 0) {
                mHolder.startDate.setText(mFormatter.print(start));
                mHolder.whenLayout.setVisibility(View.VISIBLE);;
            } else {
                mHolder.whenLayout.setVisibility(View.GONE);
            }

            if (!StringUtils.isNullOrEmpty(location)) {
                mHolder.location.setText(location);
                mHolder.locationLayout.setVisibility(View.VISIBLE);
            } else {
                mHolder.locationLayout.setVisibility(View.GONE);
            }

            if (!StringUtils.isNullOrEmpty(details)) {

                mHolder.details.setMovementMethod(LinkMovementMethod.getInstance());
                mHolder.details.setText(Html.fromHtml(details));
                mHolder.detailsLayout.setVisibility(View.VISIBLE);
            } else {
                mHolder.details.setText("");
                mHolder.detailsLayout.setVisibility(View.GONE);
            }
        }
    }

    static class ViewHolder {
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
            ButterKnife.bind(this, view);
        }
    }
}