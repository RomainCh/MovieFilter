package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class MovieDetailsActivity extends Activity implements AsyncResponse {

    // Initialisation variables

    private String url;
    private int movie_id;

    private RetrieveJsonTask retrieveJson = new RetrieveJsonTask();
    private AsyncResponse asyncR;

    private Context context;


    private TextView    txtGenres;
    private TextView    txtTitle;
    private ImageView   bgImage;
    private TextView    txtDirector;
    private TextView    txtCasting;
    private TextView    txtDateRelease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        context = this;


        // Récupération des éléments d'affichage

        txtTitle = (TextView)  findViewById(R.id.rowTitle);
        bgImage  = (ImageView) findViewById(R.id.bgImage);

        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("title");
        txtTitle.setText(title);

        Picasso.get().load(bundle.getString("image_url")).into(bgImage);

        final String synopsis = bundle.getString("synopsis");

        // Bouton vers Synopsis

        Button buttonSynopsis = (Button) findViewById(R.id.buttonSynopsis);
        buttonSynopsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, DisplayTextActivity.class);
                intent.putExtra("synopsis", synopsis);
                context.startActivity(intent);
            }
        });


        // Contact API

        movie_id = bundle.getInt("id");
        url = "https://api.themoviedb.org/3/movie/"+ movie_id +"?api_key=fbaaf7792a566ae6c637bab86098f3d3&language=en-US&append_to_response=credits";

        AsyncResponse asyncR  = this;
        retrieveJson.delegate = asyncR;
        retrieveJson.execute(url);

        // Set navbar
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(bundle.getString("score"));
    }

    //####################################################################################################################################################
    //##### Surcharger la méthode pour le retour "home" afin de revenir à l'Activity précédente ##########################################################
    //####################################################################################################################################################

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void processFinish(JSONObject json) {

        // Récupération des éléments d'affichage

        txtGenres       = (TextView) findViewById(R.id.txtGenres);
        txtDirector     = (TextView) findViewById(R.id.txtDirector);
        txtCasting      = (TextView) findViewById(R.id.txtCasting);
        txtDateRelease  = (TextView) findViewById(R.id.txtDateRelease);

        // Récupération des données du JSON

        if (json!=null){
            try {

                JSONArray  genresArray   = json.getJSONArray("genres");
                String     genres   = getGenres(genresArray);
                txtGenres.setText(genres);

                JSONObject credits  = json.getJSONObject("credits");
                String     director = getDirector(credits);
                String     casting  = getCasting(credits);
                txtDirector.setText(director);
                txtCasting.setText(casting);

                String     dateRelease = json.getString("release_date");
                txtDateRelease.setText("Release date: " + dateRelease);

            } catch (JSONException e) {

                e.printStackTrace();

            }
        }
    }


    private String getGenres(JSONArray genresArray) {
        try {

            String genres = "Genre: ";

            for (int i = 0; i < genresArray.length(); i++){
                JSONObject genre = genresArray.getJSONObject(i);
                String genre_name = genre.getString("name");
                genres += genre_name + ((i!=genresArray.length()-1) ? " - " : "" );
            }

            return genres;

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return null;
    }

    public String getDirector(JSONObject credits){
        try {

            String director = "Director: ";

            JSONArray crew = credits.getJSONArray("crew");

            for (int i = 0; i < crew.length(); i++){
                JSONObject jsonObject = crew.getJSONObject(i);
                String job = jsonObject.getString("job");
                if (job.equals("Director")){
                    director += jsonObject.getString("name");
                    return director;
                }
            }

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return null;
    }


    private String getCasting(JSONObject credits) {
        try {

            String casting = "Casting: \n";

            JSONArray cast = credits.getJSONArray("cast");

            for (int i = 0; i < 4; i++){
                JSONObject jsonObject = cast.getJSONObject(i);
                String acteur = jsonObject.getString("name") + " (" + jsonObject.getString("character") + ") ";

                casting += acteur + ((i!=3) ? "\n" : "" );

            }

            return casting;

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return null;
    }
}