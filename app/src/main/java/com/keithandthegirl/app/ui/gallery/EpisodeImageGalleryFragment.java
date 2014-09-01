package com.keithandthegirl.app.ui.gallery;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithandthegirl.app.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EpisodeImageGalleryFragment.EpisodeImageGalleryFragmentListener} interface
 * to handle interaction events.
 * Use the {@link EpisodeImageGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EpisodeImageGalleryFragment extends Fragment {
    private static final String ARG_IMAGE_POSITION = "ARG_IMAGE_POSITION";
    private static final String ARG_URL_LIST = "ARG_URL_LIST";

    public static final String STACK_NAME = EpisodeImageGalleryFragment.class.getName();

    private int mPosition;
    private String[] mUrlList;

    private EpisodeImageGalleryFragmentListener mListener;
    private ViewPager mViewPager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position initial position.
     * @param urlList url list.
     * @return A new instance of fragment EpisodeImageGalleryFragment.
     */
    public static EpisodeImageGalleryFragment newInstance(int position, String[]  urlList) {
        EpisodeImageGalleryFragment fragment = new EpisodeImageGalleryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_POSITION, position);
        args.putStringArray(ARG_URL_LIST, urlList);
        fragment.setArguments(args);
        return fragment;
    }
    public EpisodeImageGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(ARG_IMAGE_POSITION);
            mUrlList = getArguments().getStringArray(ARG_URL_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_episode_image_gallery, container, false);
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
        mViewPager.setAdapter(new ImageGalleryPagerAdapter(mUrlList));
        mViewPager.setCurrentItem(mPosition);
        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof EpisodeImageGalleryFragmentListener) {
            mListener = (EpisodeImageGalleryFragmentListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface EpisodeImageGalleryFragmentListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
