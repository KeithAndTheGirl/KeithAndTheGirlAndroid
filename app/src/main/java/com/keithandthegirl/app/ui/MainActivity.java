package com.keithandthegirl.app.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithandthegirl.app.MainApplication;
import com.keithandthegirl.app.R;
import com.keithandthegirl.app.db.KatgProvider;
import com.keithandthegirl.app.db.model.Endpoint;
import com.keithandthegirl.app.db.model.Episode;
import com.keithandthegirl.app.db.model.Live;
import com.keithandthegirl.app.db.model.Show;


public class MainActivity extends Activity implements ShowsFragment.OnShowSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    Account mAccount;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.d( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        if( savedInstanceState == null ) {
            getFragmentManager().beginTransaction()
                    .add( R.id.container, new ShowsFragment() )  // WorkFragment()
                    .commit();
        }

        mAccount = MainApplication.CreateSyncAccount( this );

        ContentResolver.setSyncAutomatically( mAccount, KatgProvider.AUTHORITY, true );

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
        settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

        Log.v( TAG, "onCreate : requesting sync" );
        ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, settingsBundle );

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if( id == R.id.action_settings ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onShowSelected( long showNameId ) {
        Log.d( TAG, "onShowSelected : enter" );

        EpisodesFragment episodesFrag = (EpisodesFragment) getFragmentManager().findFragmentById( R.id.frag );

        if( null != episodesFrag ) {

            episodesFrag.updateEpisodesView( showNameId );

        } else {

            // Create fragment and give it an argument for the selected article
            EpisodesFragment newFragment = new EpisodesFragment();
            Bundle args = new Bundle();
            args.putLong(EpisodesFragment.SHOW_NAME_ID_KEY, showNameId);
            newFragment.setArguments( args );

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace( R.id.container, newFragment );
            transaction.addToBackStack( null );

            // Commit the transaction
            transaction.commit();
        }

        Log.d( TAG, "onShowSelected : exit" );
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        Account mAccount;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

            View rootView = inflater.inflate( R.layout.fragment_main, container, false );

            return rootView;
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState ) {
            Log.d( TAG, "onActivityCreated : enter" );
            super.onActivityCreated( savedInstanceState );

            View view = getActivity().findViewById( R.id.frag );

            Log.v( TAG, "PlaceholderFragment.onActivityCreated : creating account" );
            mAccount = MainApplication.CreateSyncAccount( getActivity() );

            try {

                Cursor cursor = getActivity().getContentResolver().query( Endpoint.CONTENT_URI, null, null, null, null );
                while( cursor.moveToNext() ) {

                    String url = cursor.getString( cursor.getColumnIndex( Endpoint.FIELD_URL ) );
                    Log.i( TAG, "onActivityCreated url=" + url );

                    TextView t = new TextView( getActivity() );
                    t.setText( url );
                    ( (LinearLayout) view ).addView(t);
                }
                cursor.close();

                cursor = getActivity().getContentResolver().query( Show.CONTENT_URI, null, null, null, Show.FIELD_SORTORDER );
                while( cursor.moveToNext() ) {

                    String name = cursor.getString( cursor.getColumnIndex( Show.FIELD_NAME ) );
                    Log.i( TAG, "onActivityCreated name=" + name );

                    TextView t = new TextView( getActivity() );
                    t.setText( name );
                    ( (LinearLayout) view ).addView( t );
                }
                cursor.close();

                cursor = getActivity().getContentResolver().query( ContentUris.withAppendedId( Live.CONTENT_URI, 1 ), null, null, null, null );
                while( cursor.moveToNext() ) {

                    boolean broadcasting = cursor.getInt( cursor.getColumnIndex( Live.FIELD_BROADCASTING ) ) == 1 ? true : false;
                    Log.i( TAG, "onActivityCreated broadcasting=" + broadcasting );

                    TextView t = new TextView( getActivity() );
                    t.setText( "Broadcasting: " + broadcasting );
                    ( (LinearLayout) view ).addView( t );
                }
                cursor.close();

                cursor = getActivity().getContentResolver().query( Episode.CONTENT_URI, null, null, null, Episode.FIELD_NUMBER + " DESC" );
                while( cursor.moveToNext() ) {

                    String title = cursor.getString( cursor.getColumnIndex( Episode.FIELD_TITLE ) );
                    Log.i( TAG, "onActivityCreated title=" + title );

                    TextView t = new TextView( getActivity() );
                    t.setText( title );
                    ( (LinearLayout) view ).addView( t );
                }
                cursor.close();

            } catch( Exception e ) {
                Log.e( TAG, "onActivityCreated : error", e );
            }

            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
            settingsBundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

            Log.v( TAG, "onActivityCreated : requesting sync" );
            ContentResolver.requestSync( mAccount, KatgProvider.AUTHORITY, settingsBundle );

            Log.v( TAG, "onActivityCreated : exit" );
        }

    }

}
