package com.keithandthegirl.app.ui.utils;

import java.util.Iterator;
import java.util.List;

/**
 * Created by dmfrey on 7/19/15.
 */
public class Utils {

    public static String concatList( List<String> sList, String separator ) {
        Iterator<String> iter = sList.iterator();
        StringBuilder sb = new StringBuilder();

        while( iter.hasNext() ){
            sb.append( iter.next() ).append( iter.hasNext() ? separator : "" );
        }
        return sb.toString();
    }

}
