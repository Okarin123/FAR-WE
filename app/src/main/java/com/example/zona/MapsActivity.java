package com.example.zona;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.text.CaseMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PERMISSION_CODE = 101;
    private boolean access = false;
    private ArrayList<LatLng> hotspot = new ArrayList<LatLng>();

    //Bluetooth variables
    private static final int REQUEST_ENABLE_BT = 123;
    private ArrayList<String> deviceList = new ArrayList<String>();
    private ListView listView;
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                deviceList.add (deviceName + ": " + deviceHardwareAddress);
                //Add notifications

                try {
                    Uri alarmSound =
                            RingtoneManager. getDefaultUri (RingtoneManager. TYPE_ALARM );
                    MediaPlayer mp = MediaPlayer. create (getApplicationContext(), alarmSound);
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String message = "Alert!" + "\n" + "Distance between you and your neighbour is less than 3 metre. \n" ;
                message += "DEVICE: " + deviceName + "\n" + "MAC: " + deviceHardwareAddress;
                Toast.makeText(context, message ,Toast.LENGTH_LONG).show();

//                ArrayAdapter<String> items = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, deviceList);
//                ListView listView = (ListView) findViewById(R.id.listView);
//                listView.setAdapter(items);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.access = true;
            }
        }
    }

    public void runTimePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_CODE);
        }
        else {
            this.access = true;
        }
    }

    private void discover() {
        final Handler handler = new Handler();
        final int delay = 10000; //10s

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                if (!adapter.isDiscovering())
                    adapter.startDiscovery();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void pushNotifications (String title, String message) {
        //Add notifications
        Toast.makeText(this, title+"\n"+message, Toast.LENGTH_LONG).show();
    }

    public void pushSafetyNotification (){

        Location curr = new Location("");
        curr.setLatitude(mMap.getMyLocation().getLatitude());
        curr.setLongitude(mMap.getMyLocation().getLongitude());

        double m = 1e9;
        boolean unsafe = false;

        for (int i=0; i<this.hotspot.size(); i++) {
            Location spot = new Location("");
            spot.setLongitude(this.hotspot.get(i).longitude);
            spot.setLatitude(this.hotspot.get(i).latitude);

            double distance = curr.distanceTo(spot);
            m = Math.min (distance, m);
            if (distance < 2000) {
                unsafe = true;
                this.pushNotifications("HOTSPOT nearby!", "Do not forget to maintain social distancing!");
                break;
            }
        }

        String message = "SAFE! \nclosest hotspot distance: " + String.valueOf(m) + " m"; 
        if (!unsafe) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LatLng p1 = new LatLng (13.1199,80.2342);
        LatLng p2 = new LatLng (13.0850,80.2101);
        LatLng p3 = new LatLng (13.1137,80.2954);
        LatLng p4 = new LatLng (13.315,80.1331);
        LatLng p5 = new LatLng (13.0405,80.2503);
        LatLng p6 = new LatLng (13.1344,80.2774);
        LatLng p7 = new LatLng (13.0126,80.1547);
//        LatLng p8 = new LatLng (13.007, 80.255);

        this.hotspot.add(p1);
        this.hotspot.add(p2);
        this.hotspot.add(p3);
        this.hotspot.add(p4);
        this.hotspot.add(p5);
        this.hotspot.add(p6);
        this.hotspot.add(p7);
//        this.hotspot.add(p8);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        this.discover();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        this.runTimePermission(); //Get location access
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        LatLng startPos = new LatLng(13.0825, 80.2755);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 10));

        LatLng p1 = new LatLng (13.1199,80.2342);
        LatLng p2 = new LatLng (13.0850,80.2101);
        LatLng p3 = new LatLng (13.1137,80.2954);
        LatLng p4 = new LatLng (13.315,80.1331);
        LatLng p5 = new LatLng (13.0405,80.2503);
        LatLng p6 = new LatLng (13.1344,80.2774);
        LatLng p7 = new LatLng (13.0126,80.1547);
        LatLng p8 = new LatLng (13.007, 80.255);

        googleMap.addMarker(new MarkerOptions().position(p1)
                .title("hot-spot 1"));
        googleMap.addMarker(new MarkerOptions().position(p2)
                .title("hot-spot 2"));
        googleMap.addMarker(new MarkerOptions().position(p3)
                .title("hot-spot 3"));
        googleMap.addMarker(new MarkerOptions().position(p4)
                .title("hot-spot 4"));
        googleMap.addMarker(new MarkerOptions().position(p5)
                .title("hot-spot 5"));
        googleMap.addMarker(new MarkerOptions().position(p6)
                .title("hot-spot 6"));
        googleMap.addMarker(new MarkerOptions().position(p7)
                .title("hot-spot 7"));
//        googleMap.addMarker(new MarkerOptions().position(p8)
//                .title("hot-spot 8"));

    }

    @Override
    public void onStatusChanged (String provider,
                                 int status,
                                 Bundle extras) {
        Toast.makeText(this, "Change in location services", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.runTimePermission();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Location services enabled!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged (Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Toast.makeText(this, String.valueOf(lat) + ", " + String.valueOf(lng), Toast.LENGTH_SHORT).show();

        this.pushSafetyNotification();
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        Toast.makeText(this, "Current location:\n" + lat + ", " + lng, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        this.pushSafetyNotification();
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
