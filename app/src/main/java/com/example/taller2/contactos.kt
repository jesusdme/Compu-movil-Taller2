package com.example.taller2

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.Manifest
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CursorAdapter

class contactos : AppCompatActivity() {
    private val CONTACT_ID_INDEX=0
    private val DISPLAY_NAME_INDEX=1

    var mProjection: Array<String>? =null
    var mCursor: Cursor? = null
    var mContactsAdapter:ContactsAdapter? = null
    var mlista: ListView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        mlista = findViewById<ListView>(R.id.list)
        mProjection= arrayOf(ContactsContract.Profile._ID,ContactsContract.Profile.DISPLAY_NAME_PRIMARY )
        mContactsAdapter= ContactsAdapter(this,null,0)
        mCursor= contentResolver.query(ContactsContract.Contacts.CONTENT_URI,mProjection,null,null,null)

        initView()
        mlista?.adapter = mContactsAdapter
    }

    fun initView() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            mCursor=contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,mProjection,null,null,null
            )
            mContactsAdapter?.changeCursor(mCursor)
        }
    }

    class ContactsAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
        private val CONTACT_ID_INDEX = 0
        private val DISPLAY_NAME_INDEX = 1

        override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
            return LayoutInflater.from(context)
                .inflate(R.layout.linear, parent, false)
        }

        override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
            val tvIdContacto = view?.findViewById<TextView>(R.id.idContacto)
            val tvNombre = view?.findViewById<TextView>(R.id.nombre)
            val idnum = cursor?.getInt(CONTACT_ID_INDEX)
            val nombre = cursor?.getString(DISPLAY_NAME_INDEX)
            tvIdContacto?.text = idnum?.toString()
            tvNombre?.text = nombre
        }
    }
}