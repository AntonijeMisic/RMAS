package com.example.rmasprojekat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.rmasprojekat.Dialogs.AddEventDialog
import com.example.rmasprojekat.Dialogs.EventsTableDialog
import com.example.rmasprojekat.Dialogs.FilterDialog
import com.example.rmasprojekat.Dialogs.RadiusDialog
import com.example.rmasprojekat.Functionality.DogadjajiFunctionality
import com.example.rmasprojekat.Functionality.ElementsFunctionality
import com.example.rmasprojekat.Models.DogadjajViewModel
import com.example.rmasprojekat.databinding.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val CAMERA_REQUEST = 1003
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mgoogleMap: GoogleMap
    private lateinit var userLatLng: LatLng
    private  var photoUri: Uri? = null

    private lateinit var mapBinding: ActivityMapBinding
    private lateinit var addDialog: AddEventDialog

    private lateinit var dogadjajiViewModel: DogadjajViewModel
    private var elementsFunc: ElementsFunctionality = ElementsFunctionality()
    private lateinit var dogadjajiFunc: DogadjajiFunctionality


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapBinding = ActivityMapBinding.inflate(LayoutInflater.from(this))
        setContentView(mapBinding.root)


        checkLocationPermissions()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(this, "Molimo vas ukljucite lokaciju", Toast.LENGTH_SHORT).show()
        }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            createLocationRequest()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations){
                        userLatLng = LatLng(location.latitude, location.longitude)
                        //mgoogleMap.animateCamera(CameraUpdateFactory.newLatLng(userLatLng!!))
                    }
                }
            }
            dogadjajiViewModel = ViewModelProvider(this).get(DogadjajViewModel::class.java)
            dogadjajiViewModel.toastMessage.observe(this, Observer {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            })

            mapBinding.apply {


                btnAdd.setOnClickListener {
                    addDialog = AddEventDialog(this@MapActivity)
                    addDialog.open(R.layout.add_dialog)

                    addDialog.addEventBinding.apply {

                        val adapter = ArrayAdapter.createFromResource(this@MapActivity, R.array.tip, android.R.layout.simple_spinner_dropdown_item)
                        cbxTip.adapter = adapter
                        btnCancel.setOnClickListener {
                            addDialog.dismiss()
                        }
                        btnDodaj.setOnClickListener {
                            if(photoUri!=null)
                            {
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                {
                                    dogadjajiFunc.SaveNewEvent(addDialog, userLatLng, photoUri!!)
                                }
                                else
                                    Toast.makeText(this@MapActivity, "Upalite lokaciju!", Toast.LENGTH_SHORT).show()
                            }
                            else
                                Toast.makeText(this@MapActivity, "Molimo vas slikajte sliku!", Toast.LENGTH_SHORT).show()

                        }
                        btnDodajSliku.setOnClickListener {
                            checkCameraPermissions()
                        }
                    }
                }
                btnFilter.setOnClickListener {
                    val filterDialog = FilterDialog(this@MapActivity)
                    filterDialog.open(R.layout.filter_dialog)
                    val adapter = ArrayAdapter.createFromResource(this@MapActivity, R.array.tip, android.R.layout.simple_spinner_dropdown_item)

                    filterDialog.filterBinding.apply {

                        val danas = Date()
                        etDatumOd.setText(elementsFunc.dateToString(danas))
                        etDatumDo.setText(R.string.maxDatum)
                        spinnerTip.adapter=adapter

                        btnShowAll.setOnClickListener {
                            dogadjajiFunc.PrikaziEvente()
                            filterDialog.dismiss()
                        }

                        btnPretrazi.setOnClickListener {
                            //zovi funk pretrazi i prikazuje nam sve markere koji odgovaraju unetim pojmovima
                            dogadjajiFunc.filterEvents(spinnerTip.selectedItem.toString(), etDatumOd.text.toString(), etDatumDo.text.toString())
                            //filterEvents(spinnerTip.selectedItem.toString(), etDatumOd.text.toString(), etDatumDo.text.toString())
                        }
                    }

                }
                icProfile.setOnClickListener {

                    val intent = Intent(this@MapActivity, ProfileActivity::class.java)
                    startActivity(intent)
                }
                btnRadius.setOnClickListener {
                    val radiusDialog = RadiusDialog(this@MapActivity)
                    radiusDialog.open(R.layout.radius_dialog)
                    radiusDialog.radiusBinding.apply {
                        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                tvRadius.text = "${progress}m"
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            }

                        })
                        btnShowAllRadius.setOnClickListener {
                            seekBarRadius.progress=0
                            dogadjajiFunc.PrikaziEvente()
                            radiusDialog.dismiss()
                        }
                        btnPretraziRadius.setOnClickListener {
                            dogadjajiFunc.PrikaziEvente(null, null, null, null, seekBarRadius.progress, userLatLng)
                            //PrikaziEvente(null, null, null, null, seekBarRadius.progress, userLatLng)
                            radiusDialog.dismiss()
                        }
                    }
                }
                searchEvents.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        //zelim da proverim da li se newText nalazi u nazivu ili opisu nekog dogadjaja
                        dogadjajiFunc.PrikaziEvente(null, null, null, newText)
                        //PrikaziEvente(null, null, null, newText)
                        return true
                    }
                })

                btnTable.setOnClickListener {
                    val tableDialog = EventsTableDialog(this@MapActivity, dogadjajiViewModel, this@MapActivity, dogadjajiFunc)
                    tableDialog.open(R.layout.table_layout)
                }
            }
    }


    private fun initMap()
    {
        val mapFragment=supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    private fun moveCamera(latLng: LatLng, naziv: String, zoom: Float)
    {
        mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initMap()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE )
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera()
            }
            else
                Toast.makeText(this, "Greska", Toast.LENGTH_SHORT).show()
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                initMap()
            } else {
                Toast.makeText(this, "Dozvole nisu odobrene, aplikacija nece raditi kako treba", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserLocationOnMap() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLatLng = LatLng(location.latitude, location.longitude)

                    //mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))
                    moveCamera(userLatLng, "My Location", 18f)
                }
            }
            mgoogleMap.isMyLocationEnabled = true //da prikaze onu plavu tackicu
            mgoogleMap.uiSettings.isMyLocationButtonEnabled=false //ne prikazuje ikonicu za vracanje na nasu lokaciju


            mapBinding.icGps.setOnClickListener {
                showUserLocationOnMap()
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, "Mapa je spremna", Toast.LENGTH_SHORT).show()
        mgoogleMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showUserLocationOnMap()
            dogadjajiFunc  = DogadjajiFunctionality(this@MapActivity,dogadjajiViewModel, this@MapActivity, mgoogleMap )
            dogadjajiFunc.PrikaziEvente()
        }
        mgoogleMap.setOnMarkerClickListener {
            dogadjajiFunc.findExistingEvent(it.position, it)
            //findExistingEvent(it.position, it)
            true
        }

    }
    private fun checkCameraPermissions(){
        if(ContextCompat.checkSelfPermission(this!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            openCamera()
        }
        else
        {
            ActivityCompat.requestPermissions(this!!, arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun openCamera()
    {
        if(ContextCompat.checkSelfPermission(this!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent, CAMERA_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == CAMERA_REQUEST && resultCode== Activity.RESULT_OK) {

            val photoBitmap = data?.extras?.get("data") as Bitmap
            //nekako da gledam da sliku za dogadjaj vadim iz viewModela za prikazivanje
            //ja ovde ovu sliku nigde ne prikazujem tokom dodavanja vec tek nakon sto dodam dogadjaj
            photoUri = saveBitmapToFile(this, photoBitmap)
            addDialog.addEventBinding.tvSlikaDodata.visibility=View.VISIBLE
            addDialog.addEventBinding.btnDodajSliku.isEnabled=false
        }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

            // Dobijanje URI-ja putem FileProvider
            uri = FileProvider.getUriForFile(context, "com.example.rmasprojekat.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uri
    }
    override fun onResume() {
        super.onResume()
        //if (requestingLocationUpdates)
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}