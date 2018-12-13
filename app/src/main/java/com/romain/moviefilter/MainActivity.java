package com.romain.moviefilter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {

    private boolean buttonType_state = true;
    private boolean buttonGenre_state = true;
    private String type = new String();
    private ArrayList<String> genres = new ArrayList<>();
    private int[] btnsId;

    private boolean validSearch = false;
    private boolean typeVideo = false;
    private Button buttonRequest;

    private int initButtonRequestAlpha = 10;
    private String colorButtonRequestDisabled = "#333333";
    private String buttonSelected = "#5cb55c";


    //####################################################################################################################################################
    //##### onCreate method ##############################################################################################################################
    //####################################################################################################################################################

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            btnsId = new int[]{R.id.typeFilms, R.id.typeSeries, R.id.typeAnimes};

            //#############################################################################
            //##### Dropdown Button Type ##################################################
            //#############################################################################

                final RelativeLayout layoutTypeContent = (RelativeLayout) findViewById(R.id.layoutTypeContent);

                final ImageButton dropdownButtonType = (ImageButton) findViewById(R.id.dropdownButtonType);
                dropdownButtonType.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (buttonType_state){
                            layoutTypeContent.setVisibility(view.VISIBLE);
                            dropdownButtonType.setImageResource(R.drawable.up_arrow);
                            buttonType_state = false;
                        } else {
                            layoutTypeContent.setVisibility(view.GONE);
                            dropdownButtonType.setImageResource(R.drawable.down_arrow);
                            buttonType_state = true;
                        }

                    }
                });

            //#############################################################################
            //##### Dropdown Genre Type ##################################################
            //#############################################################################

                final RelativeLayout layoutGenreContent = (RelativeLayout) findViewById(R.id.layoutGenreContent);

                final ImageButton dropdownButtonGenre = (ImageButton) findViewById(R.id.dropdownButtonGenre);
                dropdownButtonGenre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (buttonGenre_state){
                            layoutGenreContent.setVisibility(view.VISIBLE);
                            dropdownButtonGenre.setImageResource(R.drawable.up_arrow);
                            buttonGenre_state = false;
                        } else {
                            layoutGenreContent.setVisibility(view.GONE);
                            dropdownButtonGenre.setImageResource(R.drawable.down_arrow);
                            buttonGenre_state = true;
                        }

                    }
                });

            //#############################################################################
            //##### Button Movie Request ##################################################
            //#############################################################################

                buttonRequest = (Button) findViewById(R.id.buttonRequest);
                initButtonRequest();

                buttonRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(validSearch) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, ListItemActivity.class);
                            intent.putExtra("type", type);
                            intent.putStringArrayListExtra("genres", genres);
                            context.startActivity(intent);
                        }
                    }
                });

        }

        private void initButtonRequest() {
            buttonRequest.setTextColor(Color.parseColor(colorButtonRequestDisabled));
            buttonRequest.getBackground().setAlpha(initButtonRequestAlpha);
        }

    //####################################################################################################################################################
    //##### Infos à transférer ###########################################################################################################################
    //####################################################################################################################################################

        public void addType(View v){

            String txtButtonType = ((Button)v).getText().toString();

            if (type == txtButtonType) {
                type = "";
            } else {
                type = ((Button)v).getText().toString();
            }

            if (v.getBackground().getAlpha() == 255){
                v.getBackground().setAlpha(100);
                typeVideo = true;
            } else {
                v.getBackground().setAlpha(255);
                typeVideo = false;
            }

            for (int i = 0; i < btnsId.length; i++){
                Button btn = (Button) findViewById(btnsId[i]);
                if(btn.getId() != v.getId()) {
                    btn.getBackground().setAlpha(255);
                }
            }

            testButtonSearch();

            Toast.makeText(v.getContext(), type, Toast.LENGTH_LONG).show();

        }

        private void testButtonSearch() {
            validSearch = typeVideo&&(genres.size()>0);
            if (validSearch) {
                buttonRequest.setTextColor(Color.WHITE);
                buttonRequest.getBackground().setAlpha(255);
            }
            else{
                initButtonRequest();
            }
        }

        public void addGenreToList(View v){

            String txtButton = ((Button)v).getText().toString();
            updateArray(txtButton);
            if (v.getBackground().getAlpha() == 255){
                ((Button)v).setTextColor(Color.parseColor(buttonSelected));
                v.getBackground().setAlpha(64);
            } else {
                ((Button)v).setTextColor(Color.WHITE);
                v.getBackground().setAlpha(255);
            }

            testButtonSearch();
            Toast.makeText(v.getContext(), genres.toString(), Toast.LENGTH_LONG).show();

        }

        private void updateArray(String txtButton) {
            for(int i=0;i<genres.size();i++){
                if(genres.get(i).toString().equals(txtButton)){
                    genres.remove(genres.get(i));
                    return;
                }
            }
            genres.add(txtButton);
        }

}
