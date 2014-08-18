package com.keithandthegirl.app.ui.episodesimpler.gallery;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.keithandthegirl.app.R;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

/**
 * Created by Jeff on 8/17/2014.
 * Copyright JeffInMadison.com 2014
 */
class ImageGalleryPagerAdapter extends PagerAdapter {

    private final String[] mUrlList;

    public ImageGalleryPagerAdapter(final String[] urlList) {
        mUrlList = urlList;
    }

    @Override
    public int getCount() {
        return mUrlList.length;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(container.getContext());
        ViewPager viewPager = (ViewPager)container;
        View view = layoutInflater.inflate(R.layout.view_pager_episode_gallery_image, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.galleryImageView);
        Picasso.with(container.getContext()).load(mUrlList[position]).into(imageView);
        viewPager.addView(view);
        return view;
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
