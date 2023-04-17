package com.example.taller2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.taller2.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import com.google.android.gms.location.LocationRequest

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    var inic: Boolean = false
    var miLat: Double = 0.0
    var miLon: Double = 0.0
    var distancia = 0.0
    var distanciaToast = 0.0
    var cambios = -1 //para que no cuente el de 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private val REQUEST_CODE = 101


    companion object {
        const val lowerLeftLatitude = 1.396967
        const val lowerLeftLongitude = -78.903968
        const val upperRightLatitude = 11.983639
        const val upperRightLongitude = -71.869905
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Solicitar permiso de escritura
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {

            //luz
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

            mLocationRequest = createLocationRequest()


            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    var ubicacion = locationResult.lastLocation
                    Log.i("ubicacion", "--------------$ubicacion---------")
                    if (ubicacion != null) {
                        showUserLocation()

                    }
                }
            }


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this@MapsActivity)

            lightSensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (inic == true) {
                        if (mMap != null) {
                            if (event.values[0] < 5000) {
                                Log.i("MAPS", "DARK MAP " + event.values[0])
                                mMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                        this@MapsActivity,
                                        R.raw.style_json
                                    )
                                )
                            } else {
                                Log.i("MAPS", "LIGHT MAP " + event.values[0])
                                mMap.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(
                                        this@MapsActivity,
                                        R.raw.style_json2
                                    )
                                )
                            }
                        } else {
                            Log.i("MAPS", "mMap is null")
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }

        }
    }

    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        var ubicAc = LatLng(it.latitude, it.longitude)
                        distancia = abs(calcularDistancia(miLat, miLon, it.latitude, it.longitude))



                        if (distancia >= 30) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicAc))
                            mMap.addMarker(
                                MarkerOptions().position(ubicAc)
                                    .title("UBICACION")
                                    .snippet("30 metros") //Texto de Información
                                    .alpha(0.5f)//Trasparencia
                            )
                            miLat = it.latitude
                            miLon = it.longitude

                            if (cambios > -1) {
                                guardarJson(this@MapsActivity, it.latitude, it.longitude)
                                println("********************************* JSONmetros---------------------------------------------------")

                            }
                            cambios++
                        }

                        println("La distancia entre las dos ubicaciones es de $distancia metros---------------------------------------------------")
                        println("*********************************$cambios CAMBIOS ---------------------------------------------------")

                    }
                }
        }

    }

    private fun guardarJson(context: Context, latitud: Double, longitud: Double) {
        // Solicitar permiso de escritura
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {
            val fechaHora = LocalDateTime.now()
            val formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val json = JSONObject()
            json.put("latitud", latitud)
            json.put("longitud", longitud)
            json.put("fecha_hora", fechaHora.format(formatoFechaHora))

            var jsonString = json.toString()


            print(jsonString + "//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////")
            val fileName = "registros.json"
            val file = File(context.filesDir, fileName)
            val escrib: Writer

            escrib = BufferedWriter(OutputStreamWriter(FileOutputStream(file, true)))

            if (!file.exists()) {
                file.createNewFile()
            }

            if (file.length() > 0) {
                escrib.write(json.toString())
            } else {
                escrib.write(json.toString())
            }

            escrib.close()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        showUserLocation()

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18F))
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json2))

        // Dibuja el rectángulo de la zona
        val lowerLeft = LatLng(lowerLeftLatitude, lowerLeftLongitude)
        val upperRight = LatLng(upperRightLatitude, upperRightLongitude)
        val rectOptions = PolygonOptions()
            .add(
                lowerLeft,
                LatLng(lowerLeft.latitude, upperRight.longitude),
                upperRight,
                LatLng(upperRight.latitude, lowerLeft.longitude)
            )
            .strokeWidth(5f)
            .strokeColor(Color.BLUE)
            .fillColor(Color.argb(40, 0, 0, 170))
        mMap.addPolygon(rectOptions)
        requestLocationFunction()


        inic = true
        val mGeocoder = Geocoder(baseContext)

        //Cuando se realice la busqueda
        val addressString = binding.texto.text.toString()
        if (addressString.isNotEmpty()) {
            try {
                val addresses = mGeocoder.getFromLocationName(addressString, 2)
                if (addresses != null && addresses.isNotEmpty()) {
                    val addressResult = addresses[0]
                    val position = LatLng(addressResult.latitude, addressResult.longitude)
                    if (mMap != null) {
                        //Agregar Marcador al mapa
                        mMap.addMarker(
                            MarkerOptions().position(position)
                                .title(addressResult.getAddressLine(0))
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15F))
                    } else {
                        Toast.makeText(
                            this@MapsActivity,
                            "Dirección no encontrada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this@MapsActivity, "La dirección está vacía", Toast.LENGTH_SHORT).show()
        }
        binding.texto.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = binding.texto.text.toString()
                if (!addressString.isEmpty()) {
                    try {
                        //val addresses = mGeocoder.getFromLocationName(addressString, 2)
                        val addresses: List<Address>? = mGeocoder.getFromLocationName(
                            addressString,
                            2,
                            lowerLeftLatitude,
                            lowerLeftLongitude,
                            upperRightLatitude,
                            upperRightLongitude
                        )
                        if (addresses != null && addresses.size > 0) {
                            val address = addresses[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            mMap.addMarker(
                                MarkerOptions().position(latLng).title(address.getAddressLine(0))
                            )
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                            val aprox: String = String.format("%.2f", distanciaToast) //2 decimales
                            Toast.makeText(
                                this@MapsActivity,
                                "La distancia entre las dos ubicaciones es de $aprox metros",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MapsActivity,
                                "Address not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@MapsActivity,
                            "Geocoding error: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "Please enter an address", Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        mMap.setOnMapLongClickListener { latLng ->
            if (latLng.latitude >= lowerLeftLatitude && latLng.latitude <= upperRightLatitude &&
                latLng.longitude >= lowerLeftLongitude && latLng.longitude <= upperRightLongitude
            ) {
                mMap.addMarker(
                    MarkerOptions().position(latLng).title(geoCoderSearchLatLang(latLng))
                )


                distanciaToast =
                    abs(calcularDistancia(miLat, miLon, latLng.latitude, latLng.longitude))
                val aprox: String = String.format("%.2f", distanciaToast) //2 decimales
                Toast.makeText(
                    this@MapsActivity,
                    "La distancia entre las dos ubicaciones es de $aprox metros",
                    Toast.LENGTH_SHORT
                ).show()

            } else
                Toast.makeText(this@MapsActivity, "Address out of bounds", Toast.LENGTH_SHORT)
                    .show()
        }

    }


    private fun requestLocationFunction() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        )
            return

        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            showUserLocation()
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
                setInterval(10000)
                setFastestInterval(5000)
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }
    }
        private fun geoCoderSearchLatLang(latLng: LatLng): String? {
            val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
            val addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            var address = ""

            if (addresses != null && addresses.size > 0) {
                val returnedAddress = addresses[0]
                address = "${returnedAddress.thoroughfare}, ${returnedAddress.locality}"
            }
            return address
        }

        override fun onResume() {
            super.onResume()

            sensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(lightSensorListener)
        }

        fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val radioTierra = 6371 // Radio de la Tierra en km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a =
                sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                    dLon / 2
                ) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val distanciaEnKM = radioTierra * c
            val distanciaEnMetros = distanciaEnKM * 1000

            return distanciaEnMetros
        }
    }
