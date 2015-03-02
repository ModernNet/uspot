package com.modernet.uspot;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class Get {

    private static final String TAG = "GETApi";
    private final Check check;
    private final Activity activity;
    private String app_id;
    private String TokenApp;
    private String iCityAPIUrl = "http://icity-gw.icityproject.com:8080/api/requests";
    private String responseXML;

    public Get (Activity activity) {
        Log.d(TAG,"Creation");
        app_id = activity.getString(R.string.app_id);
        TokenApp = activity.getString(R.string.TokenApp);
        check = new Check(activity);
        this.activity = activity;
    }

    private String getRequestUrl() {
        try {
            StringBuilder urlBuilder = new StringBuilder(iCityAPIUrl);
            urlBuilder.append("?");
            urlBuilder.append(URLEncoder.encode("jurisdiction_id", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8") + "&");
            urlBuilder.append(URLEncoder.encode("apikey","UTF-8") + "=" + URLEncoder.encode(TokenApp, "UTF-8"));
            return urlBuilder.toString();
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void getResponse() {
        if(!check.isInternetAvailable()){
            Toast.makeText(activity,R.string.internet_not_available,Toast.LENGTH_LONG).show();
            Log.e(TAG,"No network connection available");
            return;
        }else {
            try {
                responseXML = connection(getRequestUrl());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void freeRAM() {
        responseXML = null;        
    }

    private String[] parseDescription(String textContent) {
        return textContent.split(";");
    }
    
    public ArrayList<InterestPoint> getCategoryDataset (String category) {
        Log.i(TAG,"Parsing "+category);

        if(responseXML==null)
            getResponse();

        // Xpath Builder
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression_1 = "/service_requests/request[address_id=" + app_id + " and address = '" + category + "']/description/text()";
        String expression_2 = "/service_requests/request[address_id=" + app_id + " and address = '" + category + "']/address/text()";
        String expression_3 = "/service_requests/request[address_id=" + app_id + " and address = '" + category + "']/lat/text()";
        String expression_4 = "/service_requests/request[address_id=" + app_id + " and address = '" + category + "']/long/text()";

        // Get Nodes
        try {
            InputSource inputSource = new InputSource(new StringReader(responseXML));
            NodeList description_nodes = null;
            description_nodes = (NodeList) xpath.evaluate(expression_1, inputSource, XPathConstants.NODESET);
            inputSource = new InputSource(new StringReader(responseXML));
            NodeList category_nodes = (NodeList) xpath.evaluate(expression_2, inputSource, XPathConstants.NODESET);
            inputSource = new InputSource(new StringReader(responseXML));
            NodeList lat_nodes = (NodeList) xpath.evaluate(expression_3, inputSource, XPathConstants.NODESET);
            inputSource = new InputSource(new StringReader(responseXML));
            NodeList long_nodes = (NodeList) xpath.evaluate(expression_4, inputSource, XPathConstants.NODESET);

            // Getting last known location
            LocationManager locManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            Location location = new Location(LocationManager.NETWORK_PROVIDER);
            boolean loc_en = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (loc_en)
                location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location==null) {
                location = new Location(LocationManager.NETWORK_PROVIDER);
            }
            //Log.d(TAG, "location = ("+String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude())+")");

            ArrayList<InterestPoint> response = new ArrayList<InterestPoint>();

            for (int i = 0, n = description_nodes.getLength(); i < n; i++) {
                InterestPoint interestPoint = new InterestPoint();
                String[] nam_desc_adap = parseDescription(description_nodes.item(i).getTextContent());
                interestPoint.name= nam_desc_adap[0];
                interestPoint.description = nam_desc_adap[1];
                interestPoint.adapted = nam_desc_adap[2].equals("y");
                interestPoint.category = category_nodes.item(i).getTextContent();
                interestPoint.lat = Double.parseDouble(lat_nodes.item(i).getTextContent());
                interestPoint.lon = Double.parseDouble(long_nodes.item(i).getTextContent());

                float[] results = new float[1];
                Location.distanceBetween(
                        location.getLatitude(),
                        location.getLongitude(),
                        interestPoint.lat,
                        interestPoint.lon,
                        results
                );
                interestPoint.distance = results[0];
                if(!loc_en) interestPoint.distance = -1;

                //Discrimina POIs llunyans... Xq Quim? Distancista!!
                if(interestPoint.distance<500000)
                    response.add(interestPoint);
            }

            // Sorting by distance
            Collections.sort(response, new Comparator<InterestPoint>() {
                @Override
                public int compare(InterestPoint lhs, InterestPoint rhs) {
                    if (lhs.distance == rhs.distance) return 0;
                    return lhs.distance < rhs.distance ? -1 : 1;
                }
            });

            return response;
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String connection (String urls) {
        try {
            //Start Connection
            URL url = new URL(urls);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Log.d(TAG, conn.getResponseMessage());

            //Get Buffer
            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            //Get Response
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            //Disconnect and close Buffer
            rd.close();
            conn.disconnect();

            //Build String
            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
}
