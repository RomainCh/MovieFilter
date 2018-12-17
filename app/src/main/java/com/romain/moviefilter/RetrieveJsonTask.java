package com.romain.moviefilter;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by romain on 27/11/18.
 */

public class RetrieveJsonTask extends AsyncTask<String, Void, JSONArray> {

    public AsyncResponse delegate = null;
    public String apiKeyJson = null;

    //######################################################################################################
    //##### Première étape: on récupére le json associé à l'URL de l'API donné #############################
    //######################################################################################################

    protected JSONArray doInBackground(String... urls) {

        JSONArray json = new JSONArray();

        try {
            if (apiKeyJson.equals("results")) {
                for (int i = 0; i < urls.length; i++) {
                    JSONObject jsonObjectTemp = new JSONObject(IOUtils.toString(new URL(urls[i]), Charset.forName("UTF-8")));
                    JSONArray jsonArrayTemp = jsonObjectTemp.getJSONArray(apiKeyJson);
                    for (int j = 0; j < jsonArrayTemp.length(); j++) {
                        json.put(jsonArrayTemp.get(j));
                    }
                    Log.i("(Info) ", "->" + i + json.length());
                }
            } else {
                JSONObject jsonTemp = new JSONObject(IOUtils.toString(new URL(urls[0]), Charset.forName("UTF-8")));
                json = (JSONArray) jsonTemp.get(apiKeyJson);
                Log.i("(Info) ", "->" + json.length());
            }

        } catch (IOException e) {
            Log.i("(Error)", "-> "+e);
        } catch (JSONException e) {
            Log.i("(Error)", "->"+e);
        }

        if(json.length()==0){
            return null;
        }

        Log.i("JSON-MDR", json.toString());
        return json;
    }

    //######################################################################################################
    //##### Deuxième étape:                                                                    #############
    //##### - On récupère le json retourné par la méthode précédente                           #############
    //##### - On appelle la méthode processFinish de l'activité parente de l'AsyncTask         #############
    //######################################################################################################

    protected void onPostExecute(JSONArray json) {

        if(json!=null) {
            Log.i("(Info)", "Json is not null");
        }

        delegate.processFinish(json);
    }

    public void setApiKeyJson(String apiKeyJson){
        this.apiKeyJson = apiKeyJson;
    }
}