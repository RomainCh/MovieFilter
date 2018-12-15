package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfilActivity extends Activity {

    private Context context;
    private Menu menu;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView recyclerView;

    private int[] btnsId;

    private JSONArray listLikesAnime = new JSONArray();
    private JSONArray listLikesMovie = new JSONArray();

    private ArrayList<JSONArray> listLikes = new ArrayList<>();

    private int cursor = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        context = this;

        btnsId = new int[]{R.id.btnFilterOnlyMovie, R.id.btnFilterOnlyAnime};

        ((Button) findViewById(btnsId[cursor])).getBackground().setAlpha(100);

        SharedPreferences sharedPreferences = getSharedPreferences(ListPropositionActivity.globalPreferenceName, MODE_PRIVATE);
        String likesAnime = sharedPreferences.getString("likesAnime", "0");
        String likesMovie = sharedPreferences.getString("likesMovie", "0");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        if (likesAnime != null) {
            try {
                listLikesAnime = new JSONArray(likesAnime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (likesMovie != null) {
            try {
                listLikesMovie = new JSONArray(likesMovie);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TextView txtNoResult = (TextView) findViewById(R.id.txtNoResult);

        if((listLikesAnime.length()==0)&&(listLikesMovie.length()==0)){
            recyclerView.setVisibility(View.GONE);
            txtNoResult.setText("No result found");
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            txtNoResult.setVisibility(View.GONE);

            if(listLikesAnime.length()==0){
                cursor = 0;
            }
            else{
                cursor = 1;
            }
        }

        listLikes.add(listLikesMovie);
        listLikes.add(listLikesAnime);

        mAdapter = new ItemRowAdapter(listLikesAnime, cursor);
        recyclerView.setAdapter(mAdapter);

        // Set navbar
        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.icon_heart);
        actionBar.setTitle(" Your favorites");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        MenuItem itemProfil = menu.findItem(R.id.action_showProfil);
//        MenuItem itemSearch = menu.findItem(R.id.action_search);
        itemProfil.setVisible(false);
//        itemSearch.setVisible(false);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                initSearchView();
                return true;
            case R.id.action_goHome:
                Intent intentHome = new Intent(context, MainActivity.class);
                context.startActivity(intentHome);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearchView() {
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setIconifiedByDefault(false);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchViewItem.expandActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                JSONArray listLikesFiltered = new JSONArray();

                for(int i=0;i<listLikes.get(cursor).length();i++) {
                    try {
                        JSONObject jsonElement = listLikes.get(cursor).getJSONObject(i);

                        if(jsonElement.getString("title").toLowerCase().contains(newText.toLowerCase())){
                            listLikesFiltered.put(jsonElement);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mAdapter = new ItemRowAdapter(listLikesFiltered, cursor);
                recyclerView.setAdapter(mAdapter);
                return true;
            }
        });
    }

    public void selectFilter(View v) {

        v.getBackground().setAlpha(100);

        for (int i = 0; i < btnsId.length; i++){
            Button btn = (Button) findViewById(btnsId[i]);
            if(btn.getId() != v.getId()) {
                btn.getBackground().setAlpha(255);
            }
            else{
                cursor = i;
            }
        }

        mAdapter = new ItemRowAdapter(listLikes.get(cursor), cursor);
        recyclerView.setAdapter(mAdapter);

    }

}
