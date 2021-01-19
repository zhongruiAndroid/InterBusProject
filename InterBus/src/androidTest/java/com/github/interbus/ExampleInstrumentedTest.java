package com.github.interbus;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.github.interbus.test", appContext.getPackageName());
    }
    @Test
    public void sdasdfd() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{f: \"123\"}");
            final String fData = jsonObject.optString("f");
            System.out.println(fData+":==");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
