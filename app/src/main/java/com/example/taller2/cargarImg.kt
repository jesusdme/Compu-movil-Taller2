package com.example.taller2

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class cargarImg : AppCompatActivity() {

    private val CODIGO_SELECCIONAR_IMAGEN = 1
    private val CODIGO_TOMAR_FOTO = 2

    private lateinit var foto: ImageView
    private lateinit var gal: Button
    private lateinit var cam: Button
    private lateinit var rutaImagen: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cargar_img)


        gal = findViewById<Button>(R.id.sel_img)
        cam = findViewById<Button>(R.id.camara)
        foto = findViewById<ImageView>(R.id.imageView)


        gal.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, CODIGO_SELECCIONAR_IMAGEN)
        }
        cam.setOnClickListener {
            //tomarFoto()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            rutaImagen = crearRutaImagen()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, rutaImagen)
            startActivityForResult(intent, CODIGO_TOMAR_FOTO)
        }
    }

   /* private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, CODIGO_SELECCIONAR_IMAGEN)
    }*/

    /*private fun tomarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CODIGO_TOMAR_FOTO)
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CODIGO_SELECCIONAR_IMAGEN -> {
                    val imagenSeleccionada: Uri? = data?.data
                    try {
                        // Cargar la imagen en el ImageView
                        foto.setImageURI(imagenSeleccionada)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                CODIGO_TOMAR_FOTO -> {
                    // Cargar la imagen en el ImageView
                    foto.setImageURI(rutaImagen)
                }
            }
        } else {
            Log.e("TAG", "Error al obtener la imagen")
            Toast.makeText(this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun crearRutaImagen(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nombreArchivoImagen = "JPEG_" + timeStamp + "_"
        val directorioAlmacenamiento: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imagen = File.createTempFile(
            nombreArchivoImagen,
            ".jpg",
            directorioAlmacenamiento
        )
        return FileProvider.getUriForFile(
            this,
            "com.example.Taller2.fileprovider",
            imagen
        )
    }

    private fun guardarImagenEnGaleria(bitmap: Bitmap) {
        val archivo = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "nombre_imagen.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, archivo)!!

        val stream = contentResolver.openOutputStream(uri)!!
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
    }
}