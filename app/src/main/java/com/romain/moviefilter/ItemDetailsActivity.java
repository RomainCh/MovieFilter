package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by romain on 03/12/18.
 */

public class ItemDetailsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView txtTitle = (TextView) findViewById(R.id.rowTitle);
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
//        TextView txtSynopsis = (TextView) findViewById(R.id.rowSynopsis);
        Button buttonSynopsis   = (Button) findViewById(R.id.buttonSynopsis);


        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("title");

        txtTitle.setText(title);
        Picasso.get().load(bundle.getString("image_url")).into(bgImage);

        final String synopsis = bundle.getString("synopsis");
        final ArrayList<String> genres    = getIntent().getStringArrayListExtra("genres");
        final ArrayList<String> producers = getIntent().getStringArrayListExtra("producers");

        buttonSynopsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, FulltextActivity.class);
                intent.putExtra("synopsis", synopsis);
                context.startActivity(intent);
            }
        });

        // Genres
            TextView txtGenres  = (TextView) findViewById(R.id.txtGenres);

            String stringGenres = "";
            for(int i=0;i<genres.size();i++) {
                stringGenres+=genres.get(i)+((i<genres.size()-1) ? " - ":"");
            }

            txtGenres.setText(stringGenres);

        Toast.makeText(this, "You choose\n <"+title+">", Toast.LENGTH_LONG).show();


        TableRow lProducers = (TableRow) findViewById(R.id.listProducers);

        for(int i=0;i<producers.size();i++) {
            Button btn = new Button(this);
            btn.setText(producers.get(i));
            btn.setTextSize(12);
            btn.getBackground().setAlpha(64);
            //btn.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            lProducers.addView(btn);
        }

        // Set navbar
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(bundle.getString("score"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
