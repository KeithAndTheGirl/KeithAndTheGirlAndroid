package com.keithandthegirl.app.ui.navigationdrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keithandthegirl.app.R;

import java.util.List;

/**
 * Created by Jeff on 11/16/2014.
 */
public class NavigationItemAdapter extends ArrayAdapter<NavigationItem> {
    private static final String TAG = NavigationItemAdapter.class.getSimpleName();

    public NavigationItemAdapter(Context context, List<NavigationItem> objects) {
        super(context, -1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavigationItem item = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_navigation_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.itemIcon = (ImageView) convertView.findViewById(R.id.itemIcon);
            viewHolder.itemLabel = (TextView) convertView.findViewById(R.id.itemLabel);
            viewHolder.itemVip = (TextView) convertView.findViewById(R.id.itemVip);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.itemIcon.setImageResource(item.getIcon());
        viewHolder.itemLabel.setText(item.getLabel());
        viewHolder.itemVip.setVisibility(item.isVip() ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    private class ViewHolder {
        ImageView itemIcon;
        TextView itemLabel;
        TextView itemVip;
    }
}
