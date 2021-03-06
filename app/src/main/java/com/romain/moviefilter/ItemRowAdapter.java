package com.romain.moviefilter;

/**
 * Created by romain on 27/11/18.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ItemRowAdapter extends RecyclerView.Adapter<ItemRowAdapter.ViewHolder> {
    private JSONArray values;

    private final String defaultString = "Undefined";

    private DecimalFormat df = new DecimalFormat("#.00");

    private  int typeProcessed;

    //####################################################################################################################################################
    //##### On récupére les données passées en argument (JSONArray) ######################################################################################
    //####################################################################################################################################################

        public ItemRowAdapter(JSONArray myDataset, int typeProcessed) {
            values = myDataset;
            this.typeProcessed = typeProcessed;
        }

    //####################################################################################################################################################
    //##### Création de références pour chaque item du JSONArray sur lequel on itérera pour créer les lignes du RecyleView ###############################
    //####################################################################################################################################################

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView txtTitle;
            public TextView txtScore;
            public TextView txtEpisodes;
            public ImageView txtThumbNail;
            public View layout;

            public ViewHolder(View v) {
                super(v);
                layout = v;
                txtTitle     = (TextView) v.findViewById(R.id.rowTitle);
                txtScore     = (TextView) v.findViewById(R.id.rowScore);
                txtEpisodes  = (TextView) v.findViewById(R.id.rowEpisodes);
                txtThumbNail = (ImageView) v.findViewById(R.id.thumbnail);
            }
        }

    //####################################################################################################################################################
    //##### On créé une nouvelle View en utilisant le layout d'une ligne pour la RecyleView ##############################################################
    //####################################################################################################################################################

        @Override
        public ItemRowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.row_recycleview, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

    //####################################################################################################################################################
    //##### On remplace le contenu de la ligne courante par les éléments qui nous intéressent ############################################################
    //####################################################################################################################################################

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            try {
                JSONObject jsonElement = values.getJSONObject(position);

                if(typeProcessed==1){
                    initRowAnime(holder, jsonElement);
                }
                else{
                    initRowMovie(holder, jsonElement);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    private void initRowAnime(ViewHolder holder, JSONObject jsonElement) throws JSONException {

        final String title         = jsonElement.getString("title");
        final String imageUrl      = jsonElement.getString("image_url");
        final String synopsis      = jsonElement.getString("synopsis");
        final String score         = jsonElement.getString("score");
        final JSONArray genres     = jsonElement.getJSONArray("genres");
        final JSONArray producers  = jsonElement.getJSONArray("producers");

        holder.txtTitle.setText(title);
        holder.layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

                Intent intent = null;

                intent = new Intent(context, AnimeDetailsActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("image_url", imageUrl);
                intent.putExtra("synopsis", synopsis);
                intent.putExtra("score", score);

                ArrayList<String> genresArray = getArrayListFromName(genres);
                intent.putStringArrayListExtra("genres", genresArray);

                ArrayList<String> producersArray = getArrayListFromName(producers);
                intent.putStringArrayListExtra("producers", producersArray);

                context.startActivity(intent);
            }
        });

        holder.txtScore.setText(df.format(Double.parseDouble(score)));

        String nbEpisodes = "Error";
        nbEpisodes = rightText(jsonElement.getString("episodes"));

        holder.txtEpisodes.setText(nbEpisodes);

        Picasso.get().load(imageUrl).into(holder.txtThumbNail);
    }

    private void initRowMovie(ViewHolder holder, JSONObject jsonElement) throws JSONException {
        final String title         = jsonElement.getString("title");
        final String imageUrl      = "https://image.tmdb.org/t/p/w500"+jsonElement.getString("poster_path");
        final String score         = jsonElement.getString("vote_average");
        final String releaseDate   = rightFormatDate(jsonElement.getString("release_date"));
        final String synopsis      = jsonElement.getString("overview");
        final int id               = jsonElement.getInt("id");

        // full image url :https://image.tmdb.org/t/p/w500

        holder.txtTitle.setText(title);
        holder.layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("image_url", imageUrl);
                intent.putExtra("synopsis", synopsis);
                intent.putExtra("score", score);

                context.startActivity(intent);
            }
        });

        holder.txtScore.setText(score);//df.format(Double.parseDouble(score)));

        holder.txtEpisodes.setText(releaseDate);

        Picasso.get().load(imageUrl).into(holder.txtThumbNail);
    }


    //####################################################################################################################################################
    //##### Méthodes d'usages pour la RecycleView ########################################################################################################
    //####################################################################################################################################################

        public void removeItem(int position) {
            values.remove(position);
            notifyItemRemoved(position);
        }


        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return values.length();
        }


    //####################################################################################################################################################
    //##### Utils methods ################################################################################################################################
    //####################################################################################################################################################

        public JSONObject updateLikes(int position) throws JSONException {
            return values.getJSONObject(position);
        }

        private String rightText(String nbEpisodes) {
            if(nbEpisodes==null || nbEpisodes.isEmpty()){
                System.out.println((nbEpisodes==null));
                return "In Progress";
            }
            else {
                try {
                    int nbEpisodesInt = Integer.parseInt(nbEpisodes);
                    if (nbEpisodesInt > 1) {
                        return nbEpisodes + " episodes";
                    } else {
                        return nbEpisodes + " episode";
                    }
                } catch (NumberFormatException nfe) {
                    return "In Progress";
                }
            }
        }

        public ArrayList<String> getArrayListFromName(JSONArray genres) {
            final ArrayList<String> genresArray = new ArrayList<>();

            for(int i=0;i<genres.length();i++){
                try {
                    genresArray.add(genres.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return genresArray;
        }

        private String rightFormatDate(String releaseDate) {
            String[] dateParts = releaseDate.split("-");
            return String.format("%s/%s/%s", dateParts[2], dateParts[1], dateParts[0]);
        }

}