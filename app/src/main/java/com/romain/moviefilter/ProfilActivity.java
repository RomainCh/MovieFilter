package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfilActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        SharedPreferences sharedPreferences = getSharedPreferences(ListItemActivity.globalPreferenceName, MODE_PRIVATE);
        String likes = sharedPreferences.getString("likes", "0");
        String disLikes = sharedPreferences.getString("disLikes", "0");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        JSONArray listLikes    = new JSONArray();
        JSONArray listDisLikes = new JSONArray();

        if (likes != null) {
            try {
                listLikes = new JSONArray(likes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TextView txtNoResult = (TextView) findViewById(R.id.txtNoResult);

        if(listLikes.length()==0){
            recyclerView.setVisibility(View.GONE);
            txtNoResult.setText("No result found");
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            txtNoResult.setVisibility(View.GONE);
        }

        RecyclerView.Adapter mAdapter = new ItemRowAdapter(listLikes);
        recyclerView.setAdapter(mAdapter);

        // Set navbar
        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.icon_book);
        actionBar.setTitle(" Your favorites");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

    }
}
