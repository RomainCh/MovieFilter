
package com.romain.moviefilter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;

public class ListItemActivity extends Activity implements AsyncResponse{

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private RetrieveJson retrieveJson = new RetrieveJson();

    private String url;
    private AsyncResponse asyncR;

    private ArrayList<String> genresChoosen;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        context = this;

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        genresChoosen = getIntent().getStringArrayListExtra("genres");

        Toast.makeText(this, genresChoosen.toString(), Toast.LENGTH_LONG).show();

            JSONObject jsonGenres = null;
            try {
                String jsonString = getGenres(R.raw.genres_animes);
                jsonGenres = new JSONObject(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        // Contact API
            String type = "anime";
            int genreId = 0;
            try {
                genreId = jsonGenres.getInt(genresChoosen.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            int page = 1;

            url = String.format("https://api.jikan.moe/v3/genre/%s/%d/%d", type, genreId, page);

            AsyncResponse asyncR  = this;
            retrieveJson.delegate = asyncR;
            retrieveJson.execute(url);
    }

    //this override the implemented method from asyncTask
    @Override
    public void processFinish(JSONObject body) {

        recyclerView.setHasFixedSize(true);

        if(body!=null) {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            try {
                JSONArray results = (JSONArray) body.get("anime");

                JSONArray resultsFinal = new JSONArray();

                for(int i=0;i<results.length();i++){
                    JSONArray elementGenres = results.getJSONObject(i).getJSONArray("genres");

                    int checkComptRow = 0;
                    for(int k=0;k<elementGenres.length();k++) {
                        for(int l=0;l<genresChoosen.size();l++) {
                            Log.i("jjjjjj", ""+genresChoosen.get(l)+"=="+elementGenres.getJSONObject(k).getString("name"));
                            if(genresChoosen.get(l).equals(elementGenres.getJSONObject(k).getString("name"))) {
                                checkComptRow++;
                            }
                        }
                    }

                    Log.i("jjjjjj", ""+checkComptRow);

                    if(checkComptRow==genresChoosen.size()){
                        resultsFinal.put(results.getJSONObject(i));
                    }

                }

                mAdapter = new ItemRowAdapter(resultsFinal);
                recyclerView.setAdapter(mAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            TextView txtError    = (TextView) findViewById(R.id.errorTxtLoading);
            final RelativeLayout layoutError = (RelativeLayout) findViewById(R.id.errorLoadingLayout);
            txtError.setText("No internet");

            Button retryBtn = (Button) findViewById(R.id.retryButton);
            retryBtn.getBackground().setAlpha(64);

            layoutError.setVisibility(View.VISIBLE);

            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    layoutError.setVisibility(View.GONE);
                    finish();
                    startActivity(getIntent());
                }
            });

        }
    }

    private String getGenres(int idRawFile) throws IOException {
        InputStream is = getResources().openRawResource(idRawFile);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        return writer.toString();
    }

}
