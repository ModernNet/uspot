package com.modernet.uspot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class SplashScreen extends Activity {

    private Bundle mDataset;
    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        categories = getResources().getStringArray(R.array.categories);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_Bar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(
                        Color.WHITE,
                        PorterDuff.Mode.SRC_IN
                );
    }

    private void checkAndDownload() {
        Check checker = new Check(this);
        if(!checker.isLocationEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.location_request_title);
            builder.setMessage(R.string.location_request_message_start);
            builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
                    startActivity(settings);
                }
            });
            builder.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new loadData().execute();
                }
            });
            builder.create().show();
        } else {
            new loadData().execute();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndDownload();
    }

    private class loadData extends AsyncTask <Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {
            Get get = new Get(SplashScreen.this);
            Check checker = new Check(SplashScreen.this);
            mDataset = new Bundle();
            if (checker.isInternetAvailable()) {
                get.getResponse();
                for (int i = 0; i < categories.length; ++i) {
                    final int finalI = i;
                    SplashScreen.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView load = (TextView) findViewById(R.id.loading_text);
                            load.setText(getString(R.string.parsing)+" "+categories[finalI]+"...");
                        }
                    });
                    mDataset.putParcelableArrayList(categories[i],get.getCategoryDataset(categories[i]));
                }
                get.freeRAM();
            } else {
                for(int i=0; i<categories.length; ++i){
                    mDataset.putParcelableArrayList(categories[i],new ArrayList<InterestPoint>());
                }
                    
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent main = new Intent(SplashScreen.this,MainActivity.class);
            main.putExtra("mDataset",mDataset);
            startActivity(main);
            finish();
        }
    }


}
