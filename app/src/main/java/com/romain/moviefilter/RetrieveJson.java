package com.romain.moviefilter;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by romain on 27/11/18.
 */

public class RetrieveJson extends AsyncTask<String, Void, JSONObject> {

    public AsyncResponse delegate = null;

    protected JSONObject doInBackground(String... urls) {
        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL(urls[0]), Charset.forName("UTF-8")));
        } catch (IOException e) {
            //System.out.println(": Error with HTTP Request -> "+e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    protected void onPostExecute(JSONObject json) {
        try {
            Log.i("(info)", ""+(json==null)+"  "+json.getString("item_count"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        delegate.processFinish(json);
    }
}
