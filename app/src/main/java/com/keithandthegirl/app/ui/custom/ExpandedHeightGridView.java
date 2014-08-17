package com.keithandthegirl.app.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by Jeff on 8/16/2014.
 * Copyright JeffInMadison.com 2014
 * <p/>
 * http://stackoverflow.com/questions/8481844/gridview-height-gets-cut
 */
public class ExpandedHeightGridView extends GridView {

    public ExpandedHeightGridView(Context context) {
        super(context);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK! TAKE THAT ANDROID!

        // Calculate entire height by providing a very large height hint.
        // View.MEASURED_SIZE_MASK represents the largest height possible.
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}