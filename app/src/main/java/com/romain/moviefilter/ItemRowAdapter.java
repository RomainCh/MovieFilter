package com.romain.moviefilter;

/**
 * Created by romain on 27/11/18.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ItemRowAdapter extends RecyclerView.Adapter<ItemRowAdapter.ViewHolder> {
    private JSONArray values;

    private final String defaultString = "Undefined";

    private DecimalFormat df = new DecimalFormat("#.00");


    // Provide a suitable constructor (depends on the kind of dataset)
    public ItemRowAdapter(JSONArray myDataset) {
        values = myDataset;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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

//    public void add(int position, String item) {
//        values.add(position, item);
//        notifyItemInserted(position);
//    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemRowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.row_recycleview, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        try {
            JSONObject jsonElement = values.getJSONObject(position);

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
                    Intent intent = new Intent(context, ItemDetailsActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("image_url", imageUrl);
                    intent.putExtra("synopsis", synopsis);
                    intent.putExtra("score", score);

                    ArrayList<String> genresArray    = getArrayListFromName(genres);
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

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    return nbEpisodes + " épisodes";
                } else {
                    return nbEpisodes + " épisode";
                }
            } catch (NumberFormatException nfe) {
                return "In Progress";
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.length();
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

}