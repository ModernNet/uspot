package com.modernet.uspot;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;

public class SplashScreen extends Activity {

    private ArrayList<ArrayList<InterestPoint>> mDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    private class loadData extends AsyncTask <Void,Void,Void> {


        @Override
        protected Void doInBackground(Void... params) {
            Get get = new Get(SplashScreen.this);
            Check checker = new Check(SplashScreen.this);
            String[] categories = getResources().getStringArray(R.array.categories);
            mDataset = new ArrayList<ArrayList<InterestPoint>>();
            if (checker.isInternetAvailable()) {
                for (int i = 0; i < categories.length; ++i) {
                    mDataset.add(get.getCategoryDataset(categories[i]));
                }
                get.freeRAM();
            } else {
                for(int i=0; i<categories.length; ++i)
                    mDataset.add(new ArrayList<InterestPoint>());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent main = new Intent(SplashScreen.this,MainActivity.class);
            main.putExtra("mDataset",mDataset);
            startActivity(main);
        }
    }


}
