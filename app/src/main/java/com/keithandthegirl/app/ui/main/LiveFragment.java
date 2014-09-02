package com.keithandthegirl.app.ui.main;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.utils.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

public class LiveFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = LiveFragment.class.getSimpleName();

    private long mNextStart;

    private Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            String countDownText = getRemainingTimeFromDateAsHtmlString(mNextStart);
            mCountDownTextView.setText(Html.fromHtml(countDownText));
            updateHandler.postDelayed(this, 1000);
        }
    };

    private TextView mCountDownTextView;

    public static LiveFragment newInstance() {
        LiveFragment fragment = new LiveFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public LiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        mCountDownTextView = (TextView)view.findViewById(R.id.countDownTextView);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHandler.postDelayed(updateRunnable, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private String getRemainingTimeFromDateAsHtmlString(long releaseDateLong) {
        String resultString = StringUtils.EMPTY_STRING;

        long YEAR_MILLIS    = 1000L * 60 * 60 * 24 * 365;
        long MONTH_MILLIS   = 1000L * 60 * 60 * 24 * 30 ;
        long WEEK_MILLIS    = 1000L * 60 * 60 * 24 * 7;
        long DAY_MILLIS     = 1000L * 60 * 60 * 24;
        long HOUR_MILLIS    = 1000L * 60 * 60;
        long MINUTE_MILLIS  = 1000L * 60;
        long SECONDS_MILLIS = 1000;

        Date now = new Date();
        long delta = releaseDateLong - now.getTime();

        if (delta < MINUTE_MILLIS) {
            int seconds = (int) (delta / SECONDS_MILLIS);
            resultString = String.format("<big>%d</big> seconds", seconds);
        } else if (delta < HOUR_MILLIS) {
            int minutes = (int) (delta / MINUTE_MILLIS);
            int seconds = (int) ((delta % MINUTE_MILLIS) / SECONDS_MILLIS);
            if (seconds > 0) {
                resultString = String.format("<big>%d</big> minutes <big>%d</big> seconds", minutes, seconds);
            } else {
                resultString = String.format("<big>%d</big> minutes", minutes);
            }
        } else if (delta < DAY_MILLIS) {
            int hours = (int) (delta / HOUR_MILLIS);
            int minutes = (int) ((delta % HOUR_MILLIS) / MINUTE_MILLIS);
            int seconds = (int) ((delta % MINUTE_MILLIS) / SECONDS_MILLIS);
                resultString = String.format("<big>%d</big> hours <big>%d</big> minutes <big>%d</big> seconds", hours, minutes, seconds);
        } else if (delta < WEEK_MILLIS) {
            int days = (int) (delta / DAY_MILLIS);
            int hours = (int) ((delta % DAY_MILLIS) / HOUR_MILLIS);
            if (hours > 0) {
                resultString = String.format("<big>%d</big> days <big>%d</big> hours", days, hours);
            } else {
                resultString = String.format("<big>%d</big> days", days);
            }
        } else if (delta < MONTH_MILLIS) {
            int weeks = (int) (delta / WEEK_MILLIS);
            int days = (int) ((delta % WEEK_MILLIS) / DAY_MILLIS);
            if (days > 0) {
                resultString = String.format("<big>%d</big> weeks <big>%d</big> days", weeks, days);
            } else {
                resultString = String.format("<big>%d</big> weeks", weeks);
            }
        } else if (delta < YEAR_MILLIS) {
            int months = (int) (delta / MONTH_MILLIS);
            int weeks = (int) ((delta % MONTH_MILLIS) / WEEK_MILLIS);
            if (weeks > 0) {
                resultString = String.format("<big>%d</big> months <big>%d</big> weeks", months, weeks);
            } else {
                resultString = String.format("<big>%d</big> months", months);
            }
        } else {
            android.text.format.DateUtils.getRelativeTimeSpanString(
                    releaseDateLong,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.FORMAT_SHOW_YEAR,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE);
        }
        return resultString;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        String[] projection = null;
        String selection = EventConstants.FIELD_STARTDATE + " > ?";
        DateTime now = new DateTime( DateTimeZone.UTC );
        String[] selectionArgs = new String[] { String.valueOf( now.getMillis() ) };

        CursorLoader cursorLoader = new CursorLoader( getActivity(), EventConstants.CONTENT_URI, projection, selection, selectionArgs, EventConstants.FIELD_ENDDATE);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (cursor.moveToFirst()) {
            mNextStart = cursor.getLong( cursor.getColumnIndex( EventConstants.FIELD_STARTDATE ) );
            String countDownText = getRemainingTimeFromDateAsHtmlString(mNextStart);
            mCountDownTextView.setText(Html.fromHtml(countDownText));
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) { }
}
