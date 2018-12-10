package com.romain.moviefilter;

import org.json.JSONObject;

/**
 * Created by romain on 27/11/18.
 */

public interface AsyncResponse {
    void processFinish(JSONObject json);
}
