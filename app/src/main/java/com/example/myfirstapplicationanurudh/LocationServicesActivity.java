package com.example.myfirstapplicationanurudh;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;

public class LocationServicesActivity extends AppCompatActivity {

    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    private double latitude;
    public double longitude;
    public String address;
    private static final int REQUEST_PERMISSION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location_services);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequestLocationCallback();

        if (checkPermissions()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }
    }

    public void createLocationRequestLocationCallback() {
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                    showLocationAddress();
                }
            }
        };
    }

    public void showLocationAddress() {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    if (!addresses.isEmpty()) {
                        address = "Address: ";
                        Address addr = addresses.get(0);
                        for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                            address += addr.getAddressLine(i) + "\n";
                        }
                    } else {
                        address = "Address: Unknown";
                    }
                    updateUI();
                }

                @Override
                public void onError(String errorMessage) {
                    address = "Error: " + errorMessage;
                    updateUI();
                }
            });
        } catch (Exception e) {
            address = "Service not available";
            e.printStackTrace();
            updateUI();
        }
    }

    private void updateUI() {
        runOnUiThread(() -> {
            TextView tvlat = findViewById(R.id.textViewLatitude);
            TextView tvlng = findViewById(R.id.textViewLongitude);
            TextView tvaddr = findViewById(R.id.textViewAddress);
            if (tvlat != null) tvlat.setText("Latitude: " + latitude);
            if (tvlng != null) tvlng.setText("Longitude: " + longitude);
            if (tvaddr != null) tvaddr.setText(address);
        });
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            startLocationUpdates();
        }
    }
}
