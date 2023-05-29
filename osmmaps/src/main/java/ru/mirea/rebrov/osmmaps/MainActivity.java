package ru.mirea.rebrov.osmmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import ru.mirea.rebrov.osmmaps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 200;
    private MapView mapView = null;
    private ActivityMainBinding binding;
    private MyLocationNewOverlay locationNewOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.mapView;
        mapView.setZoomRounding(true);
        mapView.setMultiTouchControls(true);
//        IMapController mapController = mapView.getController();
//        mapController.setZoom(15.0);
//        GeoPoint startPoint = new GeoPoint(55.794229, 37.700772);
//        mapController.setCenter(startPoint);
        int cOARSE_LOCATION = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fINE_LOCATION = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (cOARSE_LOCATION == PackageManager.PERMISSION_GRANTED || fINE_LOCATION == PackageManager.PERMISSION_GRANTED) {
            locationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mapView);
            locationNewOverlay.enableMyLocation();
            mapView.getOverlays().add(this.locationNewOverlay);
            locationNewOverlay.runOnFirstFix(new Runnable() {
                public void run() {

                    try {
                        double latitude = locationNewOverlay.getMyLocation().getLatitude();
                        double longitude = locationNewOverlay.getMyLocation().getLongitude();
                        Log.d("coord", String.valueOf(latitude));
                        Log.d("coord", String.valueOf(longitude));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                IMapController mapController = mapView.getController();
                                mapController.setZoom(15.0);
                                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                                mapController.setCenter(startPoint);
                            }
                        });
                    }
                    catch (Exception e) {}
                }
            });

        } else {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }
        CompassOverlay compassOverlay = new CompassOverlay(getApplicationContext(), new
                InternalCompassOrientationProvider(getApplicationContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
        final Context context = this.getApplicationContext();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        Marker kreml = new Marker(mapView);
        kreml.setPosition(new GeoPoint(55.750019, 37.617328));
        kreml.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker kreml, MapView mapView) {
                Toast.makeText(getApplicationContext(),"center of Moscow",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mapView.getOverlays().add(kreml);
        kreml.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.osm_ic_follow_me_on, null));
        kreml.setTitle("Title");

        Marker luzhniki = new Marker(mapView);
        luzhniki.setPosition(new GeoPoint(55.715762, 37.553958));
        luzhniki.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker luzhniki, MapView mapView) {
                Toast.makeText(getApplicationContext(),"sport here",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mapView.getOverlays().add(luzhniki);
        luzhniki.setIcon(ResourcesCompat.getDrawable(getResources(), org.osmdroid.library.R.drawable.osm_ic_follow_me_on, null));
        luzhniki.setTitle("Title");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView != null)
        {
            mapView.onResume();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        if (mapView != null)
        {
            mapView.onPause();
        }
    }
}