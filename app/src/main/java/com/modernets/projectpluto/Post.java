package com.modernets.projectpluto;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/** TO_DO:
 * -Acabar de documentar
 * -Image
 * -Incloure la part de mobilitat
 *Ampliacions a considerar:
 * -Incloure botó per fer atac DDoS a esecretaria (Ups!)
 */
public class Post {

    Activity app;
    private String TAG = "PostAPI";

    //Variables
    public double lat=0;
    public double lon=0;
    public String description;
    public String name;
    public String imageUrl="noURL";
    public boolean adapted;

    //Configuració
    private String appId;
    private int jId = 1;      //JusrisdictionID
    private String servCode = "001";
    private String TokenApp;
    private String iCityAPIUrl = "http://icity-gw.icityproject.com:8080/api/requests";
    private String category = "Monument";

    public Post (Activity currentActivity){
        app = currentActivity;
        appId = app.getString(R.string.app_id);
        TokenApp = app.getString(R.string.TokenApp);
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public void setName (String name) {this.name = name;}

    public void setImageUrl (String url) {
        imageUrl=url;
    }

    public void setLocation (double lat, double lon) { this.lat = lat; this.lon = lon;}

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIfAdapted (boolean adapted) { this.adapted = adapted; }

    public boolean execute () {
        if(notComplete()){
            Log.d(TAG,"Missing parameters");
            Toast.makeText(
                    app,
                    R.string.err_form_not_completed,
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
        try {
            HttpConnection http = new HttpConnection();
            http.execute(getRequestUrl());
            http.get();
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean notComplete() {
        return  description ==null || description.equals("") ||
                category==null || category.equals("") ||
                name ==null || name.equals("") ||
                lat==0 || lon == 0;
    }

    //CODING THE REQUEST URL
    private String getRequestUrl() {
        try {
            String isAdapted;
            if(adapted) {
                isAdapted = "y";
            } else {
                isAdapted = "n";
            }
            StringBuilder paramsBody = new StringBuilder();
            paramsBody.append(URLEncoder.encode("jurisdiction_id", "UTF-8") + "=" +
                    URLEncoder.encode(String.valueOf(jId), "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("address_id", "UTF-8") + "=" +
                    URLEncoder.encode(appId, "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("service_code", "UTF-8") + "=" +
                    URLEncoder.encode(servCode, "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("lat", "UTF-8") + "=" +
                    URLEncoder.encode(String.valueOf(lat), "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("long", "UTF-8") + "=" +
                    URLEncoder.encode(String.valueOf(lon), "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("apikey", "UTF-8") + "=" +
                    URLEncoder.encode(TokenApp, "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("description", "UTF-8") + "=" +
                    URLEncoder.encode(name+";"+ description+";"+isAdapted, "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("address_string", "UTF-8") +
                    "=" + URLEncoder.encode(category, "UTF-8") + "&");
            paramsBody.append(URLEncoder.encode("media_url", "UTF-8") +
                    "=" + URLEncoder.encode(imageUrl, "UTF-8"));

            return paramsBody.toString();
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private class HttpConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder urlBuilder = new StringBuilder(iCityAPIUrl);
            try {
                //URL->CONNECTION
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded ");
                byte[] body = params[0].getBytes();
                conn.setFixedLengthStreamingMode(body.length);
                conn.setDoOutput(true);


                OutputStream out = conn.getOutputStream();
                out.write(body);

                Log.d(TAG, conn.getResponseMessage());
                BufferedReader rd;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();
                return sb.toString();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"POST successful");
            Toast.makeText(app, R.string.succ_sent, Toast.LENGTH_SHORT).show();
        }
    }
}
