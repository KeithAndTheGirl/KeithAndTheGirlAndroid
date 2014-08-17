package com.keithandthegirl.app.ui.episodesimpler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.keithandthegirl.app.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Jeff on 8/16/2014.
 * Copyright JeffInMadison.com 2014
 */
public class EpisodeGuestImageAdapter extends ArrayAdapter<String> {
    public EpisodeGuestImageAdapter(final Context context, final List<String> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        String guestImageUrl = getItem(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.gridview_item_guest_image, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Picasso.with(getContext()).load(guestImageUrl).into(viewHolder.imageView);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
    }
}
