package com.example.rmasprojekat.Functionality

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.example.rmasprojekat.Adapters.RecycleViewAdapter
import com.example.rmasprojekat.Dialogs.AddEventDialog
import com.example.rmasprojekat.Dialogs.InfoDialog
import com.example.rmasprojekat.Models.Dogadjaj
import com.example.rmasprojekat.Models.DogadjajViewModel
import com.example.rmasprojekat.Models.Korisnik
import com.example.rmasprojekat.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class DogadjajiFunctionality(context: Context, dogadjajViewModel: DogadjajViewModel, owner: LifecycleOwner, mgoogleMap: GoogleMap) {

    private var elementsFunc: ElementsFunctionality = ElementsFunctionality()
    private var context: Context = context
    private var  storage = FirebaseStorage.getInstance()
    val db= FirebaseFirestore.getInstance()
    private var dogadjajiViewModel: DogadjajViewModel =dogadjajViewModel
    private lateinit var dogadjajiAdapter: RecycleViewAdapter
    public var markerList = mutableListOf<Marker>()
    private var owner = owner
    private var  mgoogleMap: GoogleMap = mgoogleMap
    val databaseUsers = Firebase.database("https://rmas-projekat-default-rtdb.europe-west1.firebasedatabase.app/").reference
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    public fun filterEvents(tip: String?, datumOd: String?, datumDo: String?)
    {
        val now= Date()
        val nowString = elementsFunc.dateToString(now)
        val nowDate = elementsFunc.stringToDate(nowString)
        val maks = elementsFunc.stringToDate("01-01-2024")

        //tip ne moze da bude null
        when {
            (datumOd.isNullOrEmpty() && datumDo.isNullOrEmpty()) -> { //kada su oba polja prazna
                PrikaziEvente(tip, nowDate, maks)
            }
            (datumOd.isNullOrEmpty() && !datumDo.isNullOrEmpty()) -> { //kada je polje za datumOd prazno racunaj od danasnjeg dana
                val datumDoDate: Date? = elementsFunc.stringToDate(datumDo)
                if (datumDoDate!!.after(maks))
                    Toast.makeText(context, "Neispravan konacni datum", Toast.LENGTH_SHORT)
                else {
                    PrikaziEvente(tip, nowDate, datumDoDate)
                }
            }
            (!datumOd.isNullOrEmpty() && datumDo.isNullOrEmpty()) -> { //kada je polje za datumDo prazno racunaj do maks datuma
                val datumOdDate: Date? = elementsFunc.stringToDate(datumOd)
                if (datumOdDate!!.before(nowDate))
                    Toast.makeText(context, "Neispravan pocetni datum", Toast.LENGTH_SHORT)
                else {
                    PrikaziEvente(tip, datumOdDate, maks)
                }
            }
            else -> { //kada imamo sva polja
                val datumOdDate: Date? = elementsFunc.stringToDate(datumOd)
                val datumDoDate: Date? = elementsFunc.stringToDate(datumDo)
                when {
                    (datumOdDate!!.before(nowDate)) -> Toast.makeText(
                        context,
                        "Neispravan pocetni datum",
                        Toast.LENGTH_SHORT
                    ).show()
                    (datumDoDate!!.after(maks)) -> Toast.makeText(
                        context,
                        "Neispravan konacni datum",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> PrikaziEvente(tip, datumOdDate, datumDoDate)
                }
            }
        }
    }
    public fun SaveNewEvent(dialog: AddEventDialog, userLatLng: LatLng, photoUri: Uri) {

        dialog.addEventBinding.apply {

            val userId = auth.currentUser?.uid as String

            //sve ovo moram da isproveravam
            if(etNazivAdd.text.isNullOrEmpty() || etOpisAdd.text.isNullOrEmpty() || etDatum.text.isNullOrEmpty() || etVreme.text.isNullOrEmpty() )
            {
                Toast.makeText(context, "Neki od unosa nisu ispravni", Toast.LENGTH_SHORT).show()
                return
            }


            val tip = cbxTip.selectedItem.toString()
            val ime = etNazivAdd.text.toString()
            val opisce = etOpisAdd.text.toString()
            val datumce = etDatum.text.toString()
            val vremce = etVreme.text.toString()

            if(!elementsFunc.isDateValidFormat(datumce) || !elementsFunc.isTimeValidFormat(vremce))
            {
                Toast.makeText(context, "Unet datum i vreme nisu u ispravnom formatu", Toast.LENGTH_SHORT).show()
                return
            }
            val now= Date()
            val maks = elementsFunc.stringToDate("01-01-2024")

            val vremeOdrzavanja = String.format("${datumce} ${vremce}")
            val vremeOdrzavanjaDate: Date =  elementsFunc.stringToDate(vremeOdrzavanja)
            if(vremeOdrzavanjaDate.before(now))
            {
                Toast.makeText(context, "Unet datum je vec prosao!", Toast.LENGTH_SHORT).show()
                return
            }
            if(vremeOdrzavanjaDate.after(maks))
            {
                Toast.makeText(context, "Unet datum prevazilazi maksimalni datum", Toast.LENGTH_SHORT).show()
                return
            }

            databaseUsers.child("Korisnici").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            val korisnik = snapshot.getValue(Korisnik::class.java)
                            if (korisnik != null) {
                                try {
                                    unesiDogadjajUBazu(ime, opisce, korisnik, tip, vremeOdrzavanja, userLatLng.latitude, userLatLng.longitude, photoUri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                } finally {
                                    etNazivAdd.text.clear()
                                    etOpisAdd.text?.clear()
                                    etDatum.text?.clear()
                                    etVreme.text?.clear()
                                    dialog.dismiss()
                                    val lat = LatLng(userLatLng.latitude, userLatLng.longitude)
                                    val typeColor = elementsFunc.getTipColor(tip)
                                    postaviMarker(lat, typeColor)
                                }
                            } else
                                Toast.makeText(
                                    context,
                                    "Korisnik je null",
                                    Toast.LENGTH_SHORT
                                ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Neuspesno dobijanje korisnika",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "canceled", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    private fun unesiDogadjajUBazu(naziv: String, opis: String, user: Korisnik, tip: String, vremeOdrzavanja: String, latitude: Double, longitude: Double, photoUri: Uri?) {
        try {
                storage.getReference("eventImages").child(System.currentTimeMillis().toString())
                    .putFile(photoUri!!)
                    .addOnSuccessListener {
                        it.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {
                                val slikaUri = it
                                dogadjajiViewModel.unesiNoviDogadjaj(
                                    naziv,
                                    opis,
                                    user,
                                    tip,
                                    vremeOdrzavanja,
                                    latitude,
                                    longitude,
                                    slikaUri.toString()
                                )
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Neuspesno dodavanje novog dogadjaja zbog slike",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Neuspesno dodavanje dogadjaja ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    public fun postaviMarker(latLng: LatLng, color: String)
    {
        val markerColor = when (color) {
            "crvena" -> BitmapDescriptorFactory.HUE_RED
            "plava" -> BitmapDescriptorFactory.HUE_BLUE
            "zelena" -> BitmapDescriptorFactory.HUE_GREEN
            "ljubicasta" -> BitmapDescriptorFactory.HUE_VIOLET
            "roze"-> BitmapDescriptorFactory.HUE_ROSE
            else -> BitmapDescriptorFactory.HUE_RED // Default boja
        }
        val options = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(markerColor))

        val marker = mgoogleMap.addMarker(options)
        markerList.add(marker!!)
    }
    public fun PrikaziEvente(tip: String? = null, datumOd: Date? = null, datumDo: Date?= null, searchTerm: String? =null, radius: Int? = null, latLon: LatLng?=null)
    {
        dogadjajiViewModel.getEvents(tip, datumOd, datumDo, searchTerm, radius, latLon)
        dogadjajiViewModel.dogadjaji.observe(owner, androidx.lifecycle.Observer {
            if(it!=null)
            {
                dogadjajiAdapter = RecycleViewAdapter(it, context, this)
                if(markerList.size>0)
                {
                    for(marker in markerList)//ako imam nesto u listi markera onda ovo uradi ako ne onda stavljaj markere samo
                        marker.remove()
                }
                for(event in it)
                {
                    postaviMarker(LatLng(event.latitude, event.longitude), event.tip.color)
                }
            }
        })
    }
    public fun findExistingEvent(lat: LatLng, marker: Marker)
    {
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null)
        {
            db.collection("Dogadjaji")
                .whereEqualTo("latitude", lat.latitude)
                .whereEqualTo("longitude", lat.longitude)
                .limit(1).get().addOnSuccessListener {
                    val document = it.documents[0]
                    val event = document.toObject(Dogadjaj::class.java)
                    if(event!=null)
                    {
                        val infoDialog= InfoDialog(context)
                        infoDialog.open(R.layout.info_dialog)
                        infoDialog.infobinding.apply {

                            //postavljanje elemenata iz baze
                            tvNaziv.text= event.naziv
                            tvOpis.text=event.opis
                            tvTip.text = event.tip.tip
                            tvAutor.text = event.kreator.ime + " " + event.kreator.prezime
                            val stringic=event.datumOdrzavanja
                            val niz = stringic.split(" ")
                            tvDatumOdrzavanja.text=niz[0]
                            tvVremeOdrzavanja.text=niz[1]
                            Picasso.get().load(event.slika).into(slikaDogadjaja)

                            //ukoliko je taj marker postavio trenutni user on ima mogucnost izmene i brisanja
                            if(event.kreator.id!= auth.currentUser?.uid)
                                llDugmici.isVisible=false
                            else
                            {
                                llDugmici.isVisible=true
                                btnIzmeni.setOnClickListener {
                                    izmeniDogadjaj(event, infoDialog, marker)
                                }
                                //brisanje markera
                                btnIzbrisi.setOnClickListener {
                                    izbrisiDogadjaj(event, marker)
                                    infoDialog.dismiss()
                                }
                            }
                        }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(context, "Nije pronadjen dogadjaj", Toast.LENGTH_SHORT).show()
                }
        }
    }
    public fun izbrisiDogadjaj(event: Dogadjaj, marker: Marker)
    {
        try {
            dogadjajiViewModel.izbrisiDogadjaj(event)
        }
        catch (e: Exception)
        {
            Toast.makeText(context, "Neuspesno brisanje markera ${e.message}", Toast.LENGTH_SHORT).show()
        }
        finally {
            marker.remove() //sklanjam marker sa mape
        }
    }
    public fun izmeniDogadjaj(event: Dogadjaj, dialog: InfoDialog, marker: Marker)
    {
        dialog.infobinding.apply {
            val adapter = ArrayAdapter.createFromResource(context,
                R.array.tip, android.R.layout.simple_spinner_dropdown_item)
            cbxTipInfo.adapter = adapter

            elementsFunc.convertTextViewtoEditView(tvNaziv, etNazivInfo, tvNaziv.text.toString())
            elementsFunc.convertTextViewtoEditView(tvOpis, etOpisInfo, tvOpis.text.toString())
            elementsFunc.convertTextViewtoEditView(tvDatumOdrzavanja, etDatumInfo, tvDatumOdrzavanja.text.toString())
            elementsFunc.convertTextViewtoEditView(tvVremeOdrzavanja, etVremeInfo, tvVremeOdrzavanja.text.toString())
            tvTip.visibility= View.GONE
            cbxTipInfo.visibility= View.VISIBLE

            val tipovi= context.resources.getStringArray(R.array.tip)
            val indeks = tipovi.indexOf(tvTip.text.toString())
            cbxTipInfo.setSelection(indeks)



            btnIzmeni.visibility = View.GONE
            btnSacuvajIzmene.visibility = View.VISIBLE

            btnSacuvajIzmene.setOnClickListener {

                if (etNazivInfo.text.isNullOrEmpty() || etOpisInfo.text.isNullOrEmpty() || etDatumInfo.text.isNullOrEmpty() || etVremeInfo.text.isNullOrEmpty()) {
                    Toast.makeText(context, "Polja nisu validna", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!elementsFunc.isDateValidFormat(etDatumInfo.text.toString()) || !elementsFunc.isTimeValidFormat(etVremeInfo.text.toString()))
                {
                    Toast.makeText(context, "Datum i vreme nisu u ispravnom formatu", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val date = String.format("${etDatumInfo.text} ${etVremeInfo.text}")
                val vremeOdrzavanjaDate = elementsFunc.stringToDate(date)
                val now= Date()
                val maks = elementsFunc.stringToDate("01-01-2024")
                if(vremeOdrzavanjaDate.before(now))
                {
                    Toast.makeText(context, "Unet datum je vec prosao!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(vremeOdrzavanjaDate.after(maks))
                {
                    Toast.makeText(context, "Unet datum prevazilazi maksimalni datum", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                try {
                    dogadjajiViewModel.izmeniDogadjaj(
                        event.id,
                        etNazivInfo.text.toString(),
                        etOpisInfo.text.toString(),
                        date,
                        cbxTipInfo.selectedItem.toString()
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Neuspesno azuriranje dogadjaja ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    elementsFunc.convertEditTextToTextView(tvNaziv, etNazivInfo, etNazivInfo.text.toString())
                    elementsFunc.convertEditTextToTextView(tvOpis, etOpisInfo, etOpisInfo.text.toString())
                    elementsFunc.convertEditTextToTextView(tvDatumOdrzavanja, etDatumInfo, etDatumInfo.text.toString())
                    elementsFunc.convertEditTextToTextView(tvVremeOdrzavanja, etVremeInfo, etVremeInfo.text.toString())
                    tvTip.visibility = View.VISIBLE
                    cbxTipInfo.visibility = View.GONE
                    tvTip.text = cbxTipInfo.selectedItem.toString()

                    btnIzmeni.visibility = View.VISIBLE
                    btnSacuvajIzmene.visibility = View.GONE

                    markerList.remove(marker) //da bi promeni boju markera ukoliko menjam tip
                    marker.remove()
                    val tipColor = elementsFunc.getTipColor(cbxTipInfo.selectedItem.toString())
                    postaviMarker(LatLng(event.latitude, event.longitude), tipColor)
                }
            }
        }
    }
}