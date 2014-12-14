package com.keithandthegirl.app.feeback;

import junit.framework.TestCase;

public class FeedbackServiceTest extends TestCase {

    public void testSendFeedback() throws Exception {
        FeedbackService.FeedbackResult result = FeedbackService.getInstance().sendFeedback("user", "location", "comment");

        assertFalse(result.getError());
    }
}