
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
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
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

public class ListItemActivity extends Activity implements AsyncResponse{

    // RecycleView components
        private RecyclerView recyclerView;
        private RecyclerView.Adapter mAdapter;
        private RecyclerView.LayoutManager layoutManager;

    // AsyncTask and AsyncResponse
    private RetrieveJsonTask retrieveJsonTask = new RetrieveJsonTask();
    private AsyncResponse asyncR;

    private String url;

    private ArrayList<String> genresChoosen;

    private Paint p = new Paint();

    private Context context;

    public static String globalPreferenceName = "com.romain.moviefilter";

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
                    retrieveJsonTask.delegate = asyncR;
                    retrieveJsonTask.execute(url);

            // Set navbar
            ActionBar actionBar = getActionBar();
            actionBar.setLogo(R.drawable.icon_book);
            actionBar.setTitle(" Your propositions");
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                // TODO
                return true;
            case R.id.action_showProfil:
                Intent intent = new Intent(context, ProfilActivity.class);
                context.startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //####################################################################################################################################################
    //##### override the implemented method from asyncTask ###############################################################################################
    //####################################################################################################################################################

        @Override
        public void processFinish(JSONObject body) {

            //#############################################################################
            //##### Si le json venant de l'API n'est pas null #############################
            //#############################################################################

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
                        initSwipe();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            //#############################################################################
            //##### Si le json venant de l'API est null ###################################
            //#############################################################################

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


                    SharedPreferences sharedPreferences = getSharedPreferences(ListItemActivity.globalPreferenceName, MODE_PRIVATE);
                    String likes = sharedPreferences.getString("likes", "0");
                    String disLikes = sharedPreferences.getString("disLikes", "0");


                    JSONArray listLikes    = new JSONArray();
                    JSONArray listDisLikes = new JSONArray();

                    if (likes != null) {
                        try {
                            listLikes = new JSONArray(likes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (disLikes != null) {
                        try {
                            listDisLikes = new JSONArray(disLikes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    SharedPreferences.Editor editor = getSharedPreferences(globalPreferenceName, MODE_PRIVATE).edit();

                    // TODO: More generic approache

                    if (direction == ItemTouchHelper.LEFT) {
                        Toast.makeText(context, "Love", Toast.LENGTH_LONG).show();
                        try {
                            JSONObject jsonItem = ((ItemRowAdapter) mAdapter).updateLikes(position);

                            // TODO: check if in JSONArray
                            listLikes.put(jsonItem);

                            ((ItemRowAdapter) mAdapter).removeItem(position);
                            editor.putString("likes", listLikes.toString());
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "Don't want to see", Toast.LENGTH_LONG).show();
                        try {
                            JSONObject jsonItem = ((ItemRowAdapter) mAdapter).updateLikes(position);

                            listDisLikes.put(jsonItem);

                            ((ItemRowAdapter) mAdapter).removeItem(position);
                            editor.putString("disLikes", listDisLikes.toString());
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


                    Log.i("kkkkk", "--------------"+actionState);

                    Bitmap icon;
                    if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){


                        Log.i("kkkkk", "lmlmlllm");

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
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_heart);
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

}
