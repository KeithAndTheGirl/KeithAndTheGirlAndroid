package com.keithandthegirl.app.ui.main;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.model.EventConstants;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.feeback.FeedbackService;
import com.keithandthegirl.app.sync.BroadcastingDataFragment;
import com.keithandthegirl.app.sync.ShowsDataFragment;
import com.keithandthegirl.app.utils.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LiveFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {
    private static final String TAG = LiveFragment.class.getSimpleName();
    private static final int VIEW_COUNTDOWN = 0;
    private static final int VIEW_LIVE = 1;

    private static final String BROADCASTING_DATA_FRAGMENT_TAG = BroadcastingDataFragment.class.getCanonicalName();

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

    @Bind(R.id.countDownTextView)
    TextView mCountDownTextView;
    @Bind(R.id.liveFragmentTitleTextView)
    TextView mLiveFragmentTitleTextView;
    @Bind(R.id.liveViewSwitcher)
    ViewSwitcher mLiveViewSwitcher;
    @Bind(R.id.nameEditText)
    EditText mNameEditText;
    @Bind(R.id.locationEditText)
    EditText mLocationEditText;
    @Bind(R.id.commentEditText)
    EditText mCommentEditText;
    @Bind(R.id.submitButton)
    Button mSubmitButton;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_live, container, false);
        ButterKnife.bind(this, view);

        BroadcastingDataFragment broadcastingDataFragment = (BroadcastingDataFragment) getChildFragmentManager().findFragmentByTag( BROADCASTING_DATA_FRAGMENT_TAG );
        if( null == broadcastingDataFragment ) {

            broadcastingDataFragment = (BroadcastingDataFragment) instantiate( getActivity(), BroadcastingDataFragment.class.getName() );
            broadcastingDataFragment.setRetainInstance( true );

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.add( broadcastingDataFragment, BROADCASTING_DATA_FRAGMENT_TAG );
            transaction.commit();

        }

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLocationEditText.addTextChangedListener(this);
        mNameEditText.addTextChangedListener(this);
        mCommentEditText.addTextChangedListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        doLiveCheck();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @OnClick(R.id.submitButton)
    public void submit(View view) {
        // todo could possibly spin in progress to indicate pushing to comment service
        mSubmitButton.setEnabled(false);
        mSubmitButton.setText("Submitting...");
        String name = mNameEditText.getText().toString();
        String location = mLocationEditText.getText().toString();
        String comment = mCommentEditText.getText().toString();
        FeedbackService.getInstance().sendFeedback(name, location, comment, new Callback<FeedbackService.FeedbackResult>() {
            @Override
            public void success(final FeedbackService.FeedbackResult feedbackResult, final Response response) {
                Toast.makeText(getActivity(), "Feedback was sent!", Toast.LENGTH_LONG).show();
                afterPost();
            }

            @Override
            public void failure(final RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to send your feedback. :(", Toast.LENGTH_SHORT).show();
                afterPost();
            }

            void afterPost() {
                mSubmitButton.setEnabled(true);
                mSubmitButton.setText("Submit");
                mCommentEditText.setText(StringUtils.EMPTY_STRING);
            }
        });
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) { }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (StringUtils.isNullOrEmpty(mNameEditText.getText()) ||
                StringUtils.isNullOrEmpty(mLocationEditText.getText()) ||
                StringUtils.isNullOrEmpty(mCommentEditText.getText())) {
            mSubmitButton.setEnabled(false);
        } else {
            mSubmitButton.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(final Editable s) {  }


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
            resultString = getResources().getQuantityString(R.plurals.timerSeconds, seconds, seconds);
            doLiveCheck();
        } else if (delta < HOUR_MILLIS) {
            int minutes = (int) (delta / MINUTE_MILLIS);
            int seconds = (int) ((delta % MINUTE_MILLIS) / SECONDS_MILLIS);
            if (seconds > 0) {
                String secondsString = getResources().getQuantityString(R.plurals.timerSeconds, seconds, seconds);
                String minutesString = getResources().getQuantityString(R.plurals.timerMinutes, minutes, minutes);
                resultString = String.format("%s %s", minutesString, secondsString);
            } else {
                resultString = getResources().getQuantityString(R.plurals.timerMinutes, minutes, minutes);;
            }
        } else if (delta < DAY_MILLIS) {
            int hours = (int) (delta / HOUR_MILLIS);
            int minutes = (int) ((delta % HOUR_MILLIS) / MINUTE_MILLIS);
            int seconds = (int) ((delta % MINUTE_MILLIS) / SECONDS_MILLIS);
            String hoursString = getResources().getQuantityString(R.plurals.timerHours, hours, hours);
            String minutesString = getResources().getQuantityString(R.plurals.timerMinutes, minutes, minutes);
            String secondsString = getResources().getQuantityString(R.plurals.timerSeconds, seconds, seconds);

            resultString = String.format("%s %s %s", hoursString, minutesString, secondsString);
        } else if (delta < WEEK_MILLIS) {
            int days = (int) (delta / DAY_MILLIS);
            int hours = (int) ((delta % DAY_MILLIS) / HOUR_MILLIS);
            if (hours > 0) {
                String daysString = getResources().getQuantityString(R.plurals.timerDays, days, days);
                String hoursString = getResources().getQuantityString(R.plurals.timerHours, hours, hours);

                resultString = String.format("%s %s", daysString, hoursString);
            } else {
                resultString = getResources().getQuantityString(R.plurals.timerDays, days, days);;
            }
        } else if (delta < MONTH_MILLIS) {
            int weeks = (int) (delta / WEEK_MILLIS);
            int days = (int) ((delta % WEEK_MILLIS) / DAY_MILLIS);
            if (days > 0) {
                String weeksString = getResources().getQuantityString(R.plurals.timerWeeks, weeks, weeks);
                String daysString = getResources().getQuantityString(R.plurals.timerDays, days, days);

                resultString = String.format("%s %s", weeksString, daysString);
            } else {
                resultString = getResources().getQuantityString(R.plurals.timerWeeks, weeks, weeks);
            }
        } else if (delta < YEAR_MILLIS) {
            int months = (int) (delta / MONTH_MILLIS);
            int weeks = (int) ((delta % MONTH_MILLIS) / WEEK_MILLIS);
            if (weeks > 0) {
                String weeksString = getResources().getQuantityString(R.plurals.timerWeeks, weeks, weeks);
                String monthsString = getResources().getQuantityString(R.plurals.timerMonths, months, months);
                resultString = String.format("%s %s", monthsString, weeksString);
            } else {
                resultString = getResources().getQuantityString(R.plurals.timerMonths, months, months);
            }
        } else {
            android.text.format.DateUtils.getRelativeTimeSpanString(
                    releaseDateLong,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.FORMAT_SHOW_YEAR,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE);
            doLiveCheck();
        }
        return resultString;
    }

    private void doLiveCheck() {
        FeedbackService.getInstance().getBroadcastingLive(new Callback<Live>() {
            @Override
            public void success(final Live live, final Response response) {
                if (live.isBroadcasting()) {
                    // update UI
                    mLiveViewSwitcher.setDisplayedChild(VIEW_LIVE);
                    mLiveFragmentTitleTextView.setText(R.string.live_title_broadcasting);

                    // stop timer if running
                    if (updateHandler != null) {
                        updateHandler.removeCallbacks(updateRunnable);
                    }
                } else {
                    // update UI
                    mLiveViewSwitcher.setDisplayedChild(VIEW_COUNTDOWN);
                    mLiveFragmentTitleTextView.setText(R.string.live_title_not_broadcasting);

                    // start cursor loader for next show time
                    getLoaderManager().initLoader(0, null, LiveFragment.this);
                }
            }

            @Override
            public void failure(final RetrofitError error) {
                Log.w(TAG, error.getMessage());
                // todo this is the same call as success not broadcasting, refactor to one method
                // update UI
                mLiveViewSwitcher.setDisplayedChild(VIEW_COUNTDOWN);
                mLiveFragmentTitleTextView.setText(R.string.live_title_not_broadcasting);

                // start cursor loader for next show time
                getLoaderManager().initLoader(0, null, LiveFragment.this);
            }
        });
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
            updateHandler.postDelayed(updateRunnable, 1000);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) { }
}
