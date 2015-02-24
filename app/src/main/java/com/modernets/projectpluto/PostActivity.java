package com.modernets.projectpluto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

/* TO_DO:
 * -Make it look Fancy
 *Ampliacions a considerar:
 * -Adreça amb autocompletat
 * -Funció de crispetes instantanees
 */

public class PostActivity extends ActionBarActivity
{

    private static final String TAG = "PostActivity";
    private FusedLocationService fusedLocationService;
    private Check checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init classes
        checker = new Check(this);
        fusedLocationService = new FusedLocationService(this);

        //Layout set up
        setContentView(R.layout.activity_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpSwitcher();
        setUpSpinner();
    }

    private void setUpSpinner()
    {
        Spinner categories = (Spinner) findViewById(R.id.categories);
        ArrayAdapter <CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adapter);
    }

    private void setUpSwitcher()
    {
        //Set Listener for switch
        Switch auto_geo = (Switch) findViewById(R.id.auto_locate);
        auto_geo.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG,"AutoLocateSwitcher used");
                if(buttonView.isChecked()) {

                    checker.checkLocation();
                    setAutoLocation(true);
                } else {
                    setAutoLocation(false);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG,"Resuming activity");
        //Check if location was turned off during the pause
        Switch auto_loc = (Switch) findViewById(R.id.auto_locate);
        if(!checker.isLocationEnabled() && auto_loc.isChecked()){
            setAutoLocation(false);
        }
    }

    public void onSubmit(View view)
    {
        Log.i(TAG,"Submit button pressed");
        //Check if we are online
        if (!checker.isInternetAvailable()) {
            Toast.makeText(
                    this,
                    R.string.internet_not_available,
                    Toast.LENGTH_SHORT
            ).show();
            Log.e(TAG,"Missing internet connection");
            return;
        }

        //Oh it seems we are, let's start
        final Post post = new Post(this);
        //Name
        String name = ((EditText) findViewById(R.id.name))
                .getText()
                .toString();
        post.setName(name);
        //Description
        String description = ((EditText) findViewById(R.id.description))
                .getText()
                .toString();
        post.setDescription(description);
        //Location
        setPostLocation(post);
        //Categories
        Spinner cat_select = (Spinner) findViewById(R.id.categories);
        String[] categories = getResources().getStringArray(R.array.categories);
        post.setCategory(categories[cat_select.getSelectedItemPosition()]);
        //Is it Adapted
        CheckBox mob_red = (CheckBox) findViewById(R.id.mob_red);
        post.setIfAdapted(mob_red.isChecked());
        /*//MediaUrl
        String media_url = ((EditText) findViewById(R.id.media_url))
                .getText()
                .toString();
        post.setImageUrl(media_url);*/
        //Execute Post

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmation_title);
        builder.setMessage(R.string.confirmation_message);
        builder.setPositiveButton(R.string.confirmation_positive,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(post.execute()) finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }

    private void setPostLocation(Post post)
    {
        //Location
        Switch geo_enable = (Switch) findViewById(R.id.auto_locate);
        //Auto_Location Enabled
        if(geo_enable.isChecked() && checker.isLocationEnabled()) {
            Log.d(TAG,"Auto_Location Post");
            Location autoLocation = fusedLocationService.getLocation();
            post.setLocation(
                    autoLocation.getLatitude(),
                    autoLocation.getLongitude()
            );
        //EasterEgg
        } else  if (geo_enable.isChecked()){
            Toast.makeText(
                    this,
                    R.string.ee_trick,
                    Toast.LENGTH_LONG
            ).show();
            setAutoLocation(false);
        //Auto_Location Disabled
        } else {
            Log.d(TAG,"Not Auto_Location Post");
            try {
                String Address = ((EditText)findViewById(R.id.adress))
                        .getText()
                        .toString();
                if(!Address.isEmpty()) {
                    Geocoder geocoder = new Geocoder(this);
                    List<Address> addresses;
                    addresses = geocoder.getFromLocationName(Address, 1);
                    if (addresses.size() > 0) {
                        post.setLocation(
                                addresses.get(0).getLatitude(),
                                addresses.get(0).getLongitude()
                        );
                    } else {
                        Toast.makeText(
                                this,
                                R.string.error_adress,
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.e(TAG, "Error parsing Address to coordinates");
                    }
                } else
                    Log.d(TAG,"Adress empty");
            } catch (Exception e) {
                Log.e(TAG,"Error parsin Adress to coordinates");
                e.printStackTrace();
            }
        }
    }

    private void setAutoLocation (boolean enable)
    {
        Log.d(TAG,"setAutoLocation to "+String.valueOf(enable));
        EditText address = (EditText) findViewById(R.id.adress);
        Switch auto_geo = (Switch) findViewById(R.id.auto_locate);
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //Enable Auto_Location
        if (enable && locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && checker.isGooglePlayServicesAvailable()) {
            Log.i(TAG,"Auto_location enabled");
            address.setHint(R.string.auto_location);
            address.setText("");
            address.setEnabled(false);
        }
        //Disable Auto_Location
        else {
            Log.i(TAG,"Auto_location disabled");
            auto_geo.setChecked(false);
            address.setHint(R.string.address);
            address.setEnabled(true);
            if(!checker.isGooglePlayServicesAvailable()) {
                Toast.makeText(
                        this,
                        R.string.GooglePlayEnable,
                        Toast.LENGTH_SHORT
                ).show();
                Log.e(TAG,"Missing Google Play Services");
            }
        }
    }

    /* Not recreate MainActivity on Up Button

    http://stackoverflow.com/questions/22182888/actionbar-up-button-destroys-parent-activity-back-does-not
    */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
