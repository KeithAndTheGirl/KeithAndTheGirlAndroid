package com.keithandthegirl.app.utils;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class StringUtilsTest extends TestCase {

    public void testGetCommaSeparatedString() throws Exception {
        List<String> stringList = new ArrayList<String>();
        assertEquals(StringUtils.EMPTY_STRING, StringUtils.getCommaSeparatedString(stringList));
        assertEquals(StringUtils.EMPTY_STRING, StringUtils.getCommaSeparatedString(stringList, true));

        stringList.add("");
        assertEquals(StringUtils.EMPTY_STRING, StringUtils.getCommaSeparatedString(stringList));
        assertEquals(StringUtils.EMPTY_STRING, StringUtils.getCommaSeparatedString(stringList, true));

        stringList.add("a");
        stringList.add("b");
        String test = StringUtils.getCommaSeparatedString(stringList);
        assertEquals("a,b", test);
        test = StringUtils.getCommaSeparatedString(stringList, true);
        assertEquals("a, b", test);

        stringList.add("ace bad boy");
        test = StringUtils.getCommaSeparatedString(stringList);
        assertEquals("a,b,ace bad boy", test);
        test = StringUtils.getCommaSeparatedString(stringList, true);
        assertEquals("a, b, ace bad boy", test);

        stringList.add(StringUtils.EMPTY_STRING);
        test = StringUtils.getCommaSeparatedString(stringList);
        assertEquals("a,b,ace bad boy", test);
        test = StringUtils.getCommaSeparatedString(stringList, true);
        assertEquals("a, b, ace bad boy", test);

    }
}