package com.romain.moviefilter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class FulltextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulltext);

        TextView txtSynopsis  = (TextView) findViewById(R.id.rowSynopsis);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtSynopsis.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

        Button returnButton = (Button) findViewById(R.id.returnButton);

        Bundle bundle = getIntent().getExtras();
        txtSynopsis.setText(bundle.getString("synopsis"));
        getActionBar().hide();

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
