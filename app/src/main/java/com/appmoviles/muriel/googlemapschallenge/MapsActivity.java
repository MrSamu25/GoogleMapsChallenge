package com.appmoviles.muriel.googlemapschallenge;


import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DialogoMarker.ComunicacionDialogo {

    private static final int REQUEST_CODE = 11;

    private GoogleMap mMap;

    private LatLng ubicacionNueva;

    private LocationManager manager;

    //Hace referencia a la lista de marcadores que están en el mapa, a excepción del marcador de la posición del usuario
    private List<MarkerOptions> markerList;

    private Marker marcadorMiPosicion;

    private static final double RADIO_TIERRA = 6371; // En kilómetros

    private TextView tv_lugar_cercano;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //MANAGER DE CONEXIÓN
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Se inicializa la lista
        markerList = new ArrayList<MarkerOptions>();

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (marcadorMiPosicion != null) {
                    //Borra mi posición antigua
                    marcadorMiPosicion.remove();

                }

                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.title("Usted se encuentra aquí");
                //markerOptions.snippet("Debe decir la dirección en la que me encuentro");

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                markerOptions.position(latLng);

                //Con esto se puede poner un ícono pero no se le puede definir el tamaño
                //BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.current_position);

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("current_position_2", 130, 130)));


                marcadorMiPosicion = mMap.addMarker(markerOptions);

                //Cada 5 segundo muestra la posición actual
                //Toast.makeText(getApplicationContext(), "Nueva posición >>>> LAT: " + latLng.latitude + "LONG: " + latLng.longitude, Toast.LENGTH_LONG).show();

                Toast.makeText(MapsActivity.this, "Se actualizó", Toast.LENGTH_LONG).show();

                establecerLugarCercano();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });


        // Marcador predeterminado Colombia
        LatLng colombia = new LatLng(10.965363, -74.809851);
        MarkerOptions marcadorInicial = new MarkerOptions();
        marcadorInicial.title("Barranquilla");
        marcadorInicial.snippet("La ciudad es conocida por su gran Carnaval");
        marcadorInicial.position(colombia);
        mMap.addMarker(marcadorInicial);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(colombia));

        //Se añade a la lista de marcadores
        markerList.add(marcadorInicial);


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                mostrarDialogo();

                ubicacionNueva = latLng;

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //Si hago click en mi mismo marcador
                if (marker.getTitle().equals(marcadorMiPosicion.getTitle())) {
                    marcadorMiPosicion.setSnippet("Usted se encuentra en " + obtenerDireccion());

                } else {
                    double distancia = distanciaCoord(marcadorMiPosicion.getPosition().latitude, marcadorMiPosicion.getPosition().longitude, marker.getPosition().latitude, marker.getPosition().longitude);
                    marker.setSnippet("Se encuentra a " + distancia + " km del lugar");

                }


                //No sirve con return true
                return false;
            }
        });


    }


    public void establecerLugarCercano() {

        //LAT Y LON DE MI POSICIÓN
        double Lat1 = marcadorMiPosicion.getPosition().latitude;
        double Lon1 = marcadorMiPosicion.getPosition().longitude;

        MarkerOptions marcadorMasCercano = new MarkerOptions();
        double distanciaMasCercana = Double.MAX_VALUE;


        for (int i = 0; i < markerList.size(); i++) {

            //LAT Y LON DE LA POSICIÓN DE OTROS MARCADORES
            double Lat2 = markerList.get(i).getPosition().latitude;
            double Lon2 = markerList.get(i).getPosition().longitude;

            double r = distanciaCoord(Lat1, Lon1, Lat2, Lon2);

            if (r < distanciaMasCercana) {
                distanciaMasCercana = r;
                marcadorMasCercano = markerList.get(i);
            }

        }

        tv_lugar_cercano = findViewById(R.id.tv_lugar_cercano);

        if(distanciaMasCercana <= 0.100  ){

            tv_lugar_cercano.setText("Usted está en: " + marcadorMasCercano.getTitle() + ", a una distancia de " + distanciaMasCercana + " km");
        }
        else{

            tv_lugar_cercano.setText("El lugar más cercano es: " + marcadorMasCercano.getTitle() + ", a una distancia de " + distanciaMasCercana + " km");
        }



    }

    private String obtenerDireccion() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String direccion = "";
        List<Address> addresses = null;
        try {
            // My position
            LatLng posicion = marcadorMiPosicion.getPosition();
            addresses = geocoder.getFromLocation(posicion.latitude, posicion.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = addresses.get(0);
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            direccion += address.getAddressLine(i) + "\n";
        }
        return direccion;
    }


    public double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = RADIO_TIERRA * va2;

        return Math.round(distancia * 1000d) / 1000d;
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    public void mostrarDialogo() {

        DialogFragment newFragment = new DialogoMarker();
        newFragment.show(getSupportFragmentManager(), "missiles");

    }

    @Override
    public void crearMarcador(String nombre) {

        MarkerOptions marcadorNuevo = new MarkerOptions();
        marcadorNuevo.title(nombre);
        double d = distanciaCoord(marcadorMiPosicion.getPosition().latitude, marcadorMiPosicion.getPosition().longitude, ubicacionNueva.latitude, ubicacionNueva.longitude);
        marcadorNuevo.snippet("Se encuentra a una distancia de " + d + " km del lugar");
        marcadorNuevo.position(ubicacionNueva);

        //Se añade al mapa
        mMap.addMarker(marcadorNuevo);

        //Se añade a la lista de marcadores
        markerList.add(marcadorNuevo);

        establecerLugarCercano();

    }
}
