package com.keithandthegirl.app.ui.episode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.keithandthegirl.app.R;
import com.keithandthegirl.app.ui.gallery.ImageGalleryInfoHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
* Created by Jeff on 2/21/2015.
* Copyright Propeller Health 2015
*/
class EpisodeImageAdapter extends ArrayAdapter<ImageGalleryInfoHolder> {
    public EpisodeImageAdapter(final Context context, final List<ImageGalleryInfoHolder> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ImageGalleryInfoHolder imageHolder = getItem(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.gridview_item_image, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (imageHolder.isExplicit()) {
            Picasso.with(getContext()).load(R.drawable.img_explicit_warning).resize(150, 150).centerInside().into(viewHolder.imageView);
        } else {
            Picasso.with(getContext()).load(imageHolder.getImageUrl()).resize(150, 150).centerInside().into(viewHolder.imageView);
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
    }
}
