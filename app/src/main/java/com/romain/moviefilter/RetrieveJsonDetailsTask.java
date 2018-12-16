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

public class RetrieveJsonDetailsTask extends AsyncTask<String, Void, JSONObject> {

    public AsyncResponse delegate = null;

    //######################################################################################################
    //##### Première étape: on récupére le json associé à l'URL de l'API donné #############################
    //######################################################################################################

    protected JSONObject doInBackground(String... urls) {
        JSONObject json = null;
        try {
            json = new JSONObject(IOUtils.toString(new URL(urls[0]), Charset.forName("UTF-8")));
            Log.i("(Info) ", "->"+json.length());
        } catch (IOException e) {
            Log.i("(Error)", "-> "+e);
        } catch (JSONException e) {
            Log.i("(Error)", "->"+e);
        }
        return json;
    }

    //######################################################################################################
    //##### Deuxième étape:                                                                    #############
    //##### - On récupère le json retourné par la méthode précédente                           #############
    //##### - On appelle la méthode processFinish de l'activité parente de l'AsyncTask         #############
    //######################################################################################################

    protected void onPostExecute(JSONObject json) {
        try {
            if(json!=null) {
                Log.i("(Info)", "" + (json == null) + "  " + json.getString("item_count"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        delegate.processFinish(json);
    }
}