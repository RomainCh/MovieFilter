
package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientBuilder;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;

public class ListPropositionActivity extends Activity implements AsyncResponse{

    // RecycleView components
        private RecyclerView recyclerView;
        private RecyclerView.Adapter mAdapter;
        private RecyclerView.LayoutManager layoutManager;

    // AsyncTask and AsyncResponse
    private RetrieveJsonTask retrieveJsonTask = new RetrieveJsonTask();
    private AsyncResponse asyncR;

    private String url;
    private String[] urls = new String[5];

    private ArrayList<String> genresChoosen;

    private Paint p = new Paint();

    private Context context;

    public static String globalPreferenceName = "com.romain.moviefilter";
    private Menu menu;

    private JSONArray resultsFinal = new JSONArray();
    private String apiKeyJson = null;
    private int typeProcessed;

    //####################################################################################################################################################
    //##### onCreate method ##############################################################################################################################
    //####################################################################################################################################################

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_list);

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);

            context = this;

            // Process the genres

            genresChoosen = getIntent().getStringArrayListExtra("genres");

            Toast.makeText(this, genresChoosen.toString(), Toast.LENGTH_LONG).show();

            JSONObject jsonGenres = null;
            JSONObject jsonGenresMovie = null;
            try {
                String jsonString = getGenres(R.raw.genres_animes);
                String jsonStringMovie = getGenres(R.raw.genres_movies);
                jsonGenres = new JSONObject(jsonString);
                jsonGenresMovie = new JSONObject(jsonStringMovie);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Bundle bundle = getIntent().getExtras();
            String type = bundle.getString("type");

            if(type.equals("Movie")){
                // Contact API
                apiKeyJson = "results";
                typeProcessed = 0;

                int genreId = 0;
                int page;

                try {
                    genreId = jsonGenresMovie.getInt(genresChoosen.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 1; i < 6; i++) {
                    page = i;
                    //url = "https://api.themoviedb.org/3/movie/top_rated?api_key=fbaaf7792a566ae6c637bab86098f3d3&language=en-US&page=1";
                    url = "https://api.themoviedb.org/3/discover/movie?" +
                            "api_key=fbaaf7792a566ae6c637bab86098f3d3" +
                            "&language=en-US" +
                            "&sort_by=vote_average.desc" +
                            "&vote_count.gte=1000" +
                            "&with_genres=" + genreId +
                            "&include_adult=false" +
                            "&include_video=false" +
                            "&page=" + page;
                    urls[i-1] = url;
                }
            }
            else{
                // Contact API
                apiKeyJson = "anime";
                typeProcessed = 1;
                int genreId = 0;

                try {
                    genreId = jsonGenres.getInt(genresChoosen.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int page = 1;

                urls[0] = String.format("https://api.jikan.moe/v3/genre/%s/%d/%d", "anime", genreId, page);
            }

            AsyncResponse asyncR  = this;
            retrieveJsonTask.delegate = asyncR;

            retrieveJsonTask.setApiKeyJson(apiKeyJson);
            retrieveJsonTask.execute(urls);

            // Set navbar
            ActionBar actionBar = getActionBar();
            actionBar.setLogo(R.drawable.icon_book);
            actionBar.setTitle(" Your propositions");
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

    //#############################################################################
    //##### Gestion du menu de la toolbar #########################################
    //#############################################################################

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_main_actions, menu);
            MenuItem itemHome = menu.findItem(R.id.action_goHome);
            itemHome.setVisible(false);
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
                case R.id.action_showProfil:
                    Intent intentProfil = new Intent(context, ProfilActivity.class);
                    context.startActivity(intentProfil);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }
    
    //####################################################################################################################################################
    //##### override the implemented method from asyncTask ###############################################################################################
    //####################################################################################################################################################

        @Override
        public void processFinish(JSONArray body) {

            //#############################################################################
            //##### Si le json venant de l'API n'est pas null #############################
            //#############################################################################

                if(body!=null) {

                    if(body.length()!=0) {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        layoutManager = new LinearLayoutManager(this);
                        recyclerView.setLayoutManager(layoutManager);

//                        resultsFinal = new JSONArray();
//
//                        boolean checkItem;
//
//                        for(int i=0;i<body.length();i++) {
//
//                            checkItem = false;
//
//                            try {
//                                JSONObject jsonElement = body.getJSONObject(i);
//
//                                JSONArray genres = jsonElement.getJSONArray("genres");
//
//                                Log.i("(Warning)", genresChoosen.toString());
//
//                                for(int k=0;k<genres.length();k++) {
//                                    if (genresChoosen.contains(genres.getJSONObject(k).getString("name"))) {
//                                        checkItem = true;
//                                        Log.i("(Warning)", jsonElement.getString("title")+" have the genre "+genresChoosen.toString());
//                                    }
//                                }
//
//                                if(checkItem){
//                                    resultsFinal.put(jsonElement);
//                                }
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }

                        resultsFinal = body;

                        if(resultsFinal.length()>0) {

                            mAdapter = new ItemRowAdapter(resultsFinal, typeProcessed);
                            recyclerView.setAdapter(mAdapter);
                            initSwipe();
                        }
                        else{
                            noResults();
                        }
                    }
                    else{
                        noResults();
                    }

                }

            //#############################################################################
            //##### Si le json venant de l'API est null ###################################
            //#############################################################################

                else {

                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    TextView txtError = (TextView) findViewById(R.id.errorTxtLoading);
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


    @Override
    public void processFinish(JSONObject json) {
        Log.i("(Error)", "Not the right method summoned");
    }

    //####################################################################################################################################################
    //##### initSwipe method (Pour activer le glissement sur les lignes de la RecycleView) ###############################################################
    //####################################################################################################################################################

        private void initSwipe() {

            // attaching the touch helper to recycler view
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();


                    SharedPreferences sharedPreferences = getSharedPreferences(ListPropositionActivity.globalPreferenceName, MODE_PRIVATE);
                    String likesAnime = sharedPreferences.getString("likesAnime", "[]");
                    String likesMovie = sharedPreferences.getString("likesMovie", "[]");

                    JSONArray listLikesAnime = new JSONArray();
                    JSONArray listLikesMovie = new JSONArray();

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

                    JSONObject jsonItem = null;
                    try {
                        jsonItem = ((ItemRowAdapter) mAdapter).updateLikes(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(typeProcessed==1) {
                        // likesAnime
                        for(int i=0;i<likesAnime.length();i++){
                            try {
                                if(listLikesAnime.getJSONObject(i).getInt("mal_id")==jsonItem.getInt("mal_id")){
                                    ((ItemRowAdapter) mAdapter).removeItem(position);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        // likesMovie
                        for(int i=0;i<likesMovie.length();i++){
                            try {
                                if(listLikesMovie.getJSONObject(i).getInt("id")==jsonItem.getInt("id")){
                                    ((ItemRowAdapter) mAdapter).removeItem(position);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    SharedPreferences.Editor editor = getSharedPreferences(globalPreferenceName, MODE_PRIVATE).edit();

                    // TODO: More generic approache

                    if (direction == ItemTouchHelper.RIGHT) {
                        Toast.makeText(context, "Love", Toast.LENGTH_LONG).show();

                        // TODO: check if in JSONArray and Unlike a anime/movie
                        if(typeProcessed==1) {
                            listLikesAnime.put(jsonItem);
                            editor.putString("likesAnime", listLikesAnime.toString());
                        }
                        else{
                            listLikesMovie.put(jsonItem);
                            editor.putString("likesMovie", listLikesMovie.toString());
                        }

                        ((ItemRowAdapter) mAdapter).removeItem(position);
                        editor.commit();

                    } else {
                        Toast.makeText(context, "Don't want to see", Toast.LENGTH_LONG).show();
                        ((ItemRowAdapter) mAdapter).removeItem(position);
                    }
                }


                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                    Bitmap icon;
                    if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                        View itemView = viewHolder.itemView;
                        float height = (float) itemView.getBottom() - (float) itemView.getTop();
                        float width = height / 3;

                        if(dX > 0){
                            p.setColor(Color.parseColor("#388E3C"));
                            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                            c.drawRect(background,p);
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_heart);
                            RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                            c.drawBitmap(icon,null,icon_dest,p);
                        } else if (dX < 0) {
                            p.setColor(Color.parseColor("#D32F2F"));
                            RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                            c.drawRect(background,p);
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                            RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                            c.drawBitmap(icon,null,icon_dest,p);
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

    //####################################################################################################################################################
    //##### Utils methods ################################################################################################################################
    //####################################################################################################################################################

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

        private void noResults() {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            if(isConnected) {

                TextView txtError = (TextView) findViewById(R.id.errorTxtLoading);
                final RelativeLayout layoutError = (RelativeLayout) findViewById(R.id.errorLoadingLayout);
                txtError.setText("No results found");

                Button retryBtn = (Button) findViewById(R.id.retryButton);
                retryBtn.getBackground().setAlpha(64);

                layoutError.setVisibility(View.VISIBLE);

                retryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        layoutError.setVisibility(View.GONE);
                        finish();
                        Intent intentHome = new Intent(context, MainActivity.class);
                        context.startActivity(intentHome);
                    }
                });
            }
        }

    //#############################################################################
    //##### Méthode initiant la SearchView ########################################
    //#############################################################################

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

                    JSONArray listFiltered = new JSONArray();

                    for(int i=0;i<resultsFinal.length();i++) {
                        try {
                            JSONObject jsonElement = resultsFinal.getJSONObject(i);

                            if(jsonElement.getString("title").toLowerCase().contains(newText.toLowerCase())){
                                listFiltered.put(jsonElement);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    mAdapter = new ItemRowAdapter(listFiltered, typeProcessed);
                    recyclerView.setAdapter(mAdapter);
                    return true;
                }
            });
        }


}
