package ru.mirea.rebrov.yandexdriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;
import java.util.ArrayList;
import java.util.List;
import ru.mirea.rebrov.yandexdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener
{
    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_PERMISSION = 200;
    private Point ROUTE_START_LOCATION;
    private final Point ROUTE_END_LOCATION = new Point(55.765119, 37.619388);
    private Point SCREEN_CENTER;
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private LocationManager locationManager;
    private LocationListener myLocationListener;
    private Point myLocation;
    private boolean updated = true;
    private static final double DESIRED_ACCURACY = 0;
    private static final long MINIMAL_TIME = 1000;
    private static final double MINIMAL_DISTANCE = 1;
    private static final boolean USE_IN_BACKGROUND = false;
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0x00FFBBBB, 0xFF0000FF};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.mapview;
        mapView.getMap().setRotateGesturesEnabled(false);
        int cOARSE_LOCATION = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fINE_LOCATION = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (cOARSE_LOCATION == PackageManager.PERMISSION_GRANTED || fINE_LOCATION == PackageManager.PERMISSION_GRANTED)
        {
            locationManager = MapKitFactory.getInstance().createLocationManager();
            myLocationListener = new LocationListener()
            {
                @Override
                public void onLocationUpdated(Location location)
                {
                    if(updated)
                    {
                        myLocation = location.getPosition(); //this user point
                        ROUTE_START_LOCATION = new Point(myLocation.getLatitude(), myLocation.getLongitude());
                        SCREEN_CENTER = new Point((ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2, (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2);
                        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 8, 0, 0));
                        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
                        mapObjects = mapView.getMap().getMapObjects().addCollection();
                        submitRequest();
                        updated = false;
                    }
                    Log.w("my location", "my location - " + myLocation.getLatitude() + "," + myLocation.getLongitude());
                }

                @Override
                public void onLocationStatusUpdated(LocationStatus locationStatus)
                {
                    if (locationStatus == LocationStatus.NOT_AVAILABLE)
                    {
                        System.out.println("sdncvoadsjv");
                    }
                }
            };

        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }

        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(ROUTE_END_LOCATION);
        marker.addTapListener(new MapObjectTapListener()
        {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point
                    point)
            {
                Toast.makeText(getApplication(),"Moscow center here!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
    private void submitRequest()
    {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(2);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions,
                vehicleOptions, this);
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list)
    {
        int color;
        for (int i = 0; i < list.size(); i++)
        {
            color = colors[i];
            mapObjects.addPolyline(list.get(i).getGeometry()).setStrokeColor(color);
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error)
    {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError)
        {
            errorMessage = getString(R.string.remote_error_message);
        }
        else if (error instanceof NetworkError)
        {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        subscribeToLocationUpdate();
    }

    @Override
    protected void onStop()
    {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        locationManager.unsubscribe(myLocationListener);

        super.onStop();
    }

    private void subscribeToLocationUpdate()
    {
        if (locationManager != null && myLocationListener != null)
        {
            locationManager.subscribeForLocationUpdates(DESIRED_ACCURACY, MINIMAL_TIME, MINIMAL_DISTANCE, USE_IN_BACKGROUND, FilteringMode.OFF, myLocationListener);
        }
    }
}