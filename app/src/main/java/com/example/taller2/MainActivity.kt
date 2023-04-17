package com.example.taller2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.location.LocationRequest
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CAMERA = 1 // Identificador para la solicitud de permiso de la cámara
    private val REQUEST_IMAGE_CAPTURE = 2 // Identificador para la solicitud de captura de imagen
    private val REQUEST_WRITE_EXTERNAL_STORAGE =3 // Identificador para guardar la imagen
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 4
    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var photoImageView: ImageView // Referencia al ImageView donde se mostrará la foto capturada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val foto=findViewById<ImageView>(R.id.cam)
        val map=findViewById<ImageView>(R.id.mapa)
        val cont=findViewById<ImageView>(R.id.contacto)

        map.setOnClickListener(){
            //si no tiene permiso preguntar
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_ACCESS_LOCATION
                )

            } else //si tiene permiso usar la ubicacion
            {
                val Intent = Intent(this, MapsActivity::class.java)
                startActivity(Intent)
            }
        }

        cont.setOnClickListener(){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                // aun no hay permiso preguntar
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
            }
            else
            {
                val Intent = Intent(this, contactos::class.java)
                startActivity(Intent)
            }
        }

        foto.setOnClickListener {
            // Solicitar permiso para usar la cámara si aún no se ha concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
            } else {
                // El permiso ya ha sido concedido
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_STORAGE)
                } else {
                    // El permiso ya ha sido concedido
                    val Intent = Intent(this, cargarImg::class.java)
                    startActivity(Intent)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // If the request is cancelled, the grantResults array will be empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied. Handle the denied state.
                Toast.makeText(this, "Permiso denegado. La aplicación no puede acceder a los contactos sin el permiso correspondiente.", Toast.LENGTH_LONG).show()
            }
            return
        }


        // Verifica si el permiso se ha concedido y muestra un mensaje de tostada correspondiente
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                // El permiso ha sido concedido, así que se puede escribir en el almacenamiento externo
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }



}