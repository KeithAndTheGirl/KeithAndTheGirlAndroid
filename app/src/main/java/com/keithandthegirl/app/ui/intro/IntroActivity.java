package com.keithandthegirl.app.ui.intro;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.keithandthegirl.app.R;

public class IntroActivity extends Activity {

    private ViewPager mViewPager;
    private IntroPagerAdapter mIntroPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        int[] introImages = new int[] { R.drawable.about_chemda, R.drawable.about_keith, R.drawable.about_podcasting_guide};
        mIntroPagerAdapter = new IntroPagerAdapter(introImages);
        mViewPager.setAdapter(mIntroPagerAdapter);
    }

    private class IntroPagerAdapter extends PagerAdapter {
        private final int[] mIntroImageResIdList;

        public IntroPagerAdapter(int[] introImageResIdList) {
            mIntroImageResIdList = introImageResIdList;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            ImageView imageView = new ImageView(IntroActivity.this);
            imageView.setImageResource(mIntroImageResIdList[position]);
            ViewGroup.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(layoutParams);
            return imageView;
        }

        @Override
        public int getCount() {
            return mIntroImageResIdList.length;
        }

        @Override
        public boolean isViewFromObject(final View view, final Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            ViewPager viewPager = (ViewPager)container;
            viewPager.removeView((View) object);
        }
    }
}
