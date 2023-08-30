package com.example.rmasprojekat.Models

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rmasprojekat.Functionality.ElementsFunctionality
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

data class Dogadjaj(

    public var id: String,
    public var naziv: String,
    public var opis: String,
    public var kreator: Korisnik,
    public var tip: Tip,
    public var datumOdrzavanja: String,
    public var latitude: Double,
    public var longitude: Double,
    public var slika: String
)
{
    constructor() : this("", "", "", Korisnik(), Tip(), "", 0.0, 0.0, "" )
}
class DogadjajViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val kolekcijaDogadjaja = db.collection("Dogadjaji")
    val database = Firebase.database("https://rmas-projekat-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private var elementsFunc: ElementsFunctionality = ElementsFunctionality()

    private val _dogadjaji = MutableLiveData<List<Dogadjaj>>()
    val dogadjaji: LiveData<List<Dogadjaj>> get() = _dogadjaji

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _pronadjen_dogadjaj = MutableLiveData<Dogadjaj>()
    val currentEvent: LiveData<Dogadjaj> get() = _pronadjen_dogadjaj

    fun showSimpleToast(message: String) {
        _toastMessage.value = message
    }


    fun getEvents(tip: String? = null, datumOd: Date? = null, datumDo: Date?= null, searchTerm: String? = null, radius: Int? =null, userLatLng: LatLng?=null) {

        kolekcijaDogadjaja.get().addOnSuccessListener { documents ->
            val listaDogadjaja = mutableListOf<Dogadjaj>()
            if (!documents.isEmpty || documents != null) {
                for (document in documents) {
                    val dogadjaj = document.toObject(Dogadjaj::class.java)

                    //ukoliko dogadjaj datum prosao izbrisi taj dogadjaj i ne prikazuj ga

                    when
                    {
                        (radius!=null) -> //ukoliko radius nije null to znaci da radimo pretragu po radiusu
                        {
                            if(userLatLng!=null)
                            {
                                val distanca = distanca(userLatLng, dogadjaj)
                                if(distanca<=radius)
                                    listaDogadjaja.add(dogadjaj)
                            }
                        }
                        (!searchTerm.isNullOrEmpty()) -> //ukoliko searchTerm nije null to znaci da raidmo search
                        {
                            if(dogadjaj.naziv.contains(searchTerm, ignoreCase = true)
                                || dogadjaj.opis.contains(searchTerm, ignoreCase = true)
                                ||dogadjaj.tip.tip.contains(searchTerm, ignoreCase = true)) {
                                listaDogadjaja.add(dogadjaj)
                            }
                        }
                        else -> {                       //ukoliko su i radius i search null onda radimo pretragu po parametrima
                            val datumOdrzavanja = elementsFunc.stringToDate(dogadjaj.datumOdrzavanja)
                            when {
                                (tip != null && datumOd != null && datumDo != null) -> {
                                    if (tip == dogadjaj.tip.tip && datumOdrzavanja >= datumOd && datumOdrzavanja <= datumDo)
                                        listaDogadjaja.add(dogadjaj)
                                }
                                (tip != null && datumOd == null && datumDo == null) -> {
                                    if (tip == dogadjaj.tip.tip)
                                        listaDogadjaja.add(dogadjaj)
                                }
                                else -> {
                                    listaDogadjaja.add(dogadjaj) //u ovom slucaju ako je sve zivo null to znaci da prikazujemo sve dogadjaje
                                }
                            }
                        }
                    }
                }
                _dogadjaji.value = listaDogadjaja
            } else {
                showSimpleToast("Dogadjaji su null")
                throw NullPointerException("Dogadjaji su null")
            }
        }
            .addOnFailureListener {
                showSimpleToast("Niste uspeli da uzmete dogadjaje")
                throw Exception("Niste uspeli da uzmete dogadjaje")
            }
    }

    fun distanca(userLatLng: LatLng, dogadjaj: Dogadjaj): Float
    {
        val userLocation = Location("provider")
        userLocation.latitude = userLatLng.latitude
        userLocation.longitude = userLatLng.longitude

        val eventLocation = Location("provider")
        eventLocation.latitude =dogadjaj.latitude
        eventLocation.longitude =dogadjaj.longitude

        val distance = userLocation.distanceTo(eventLocation)
        return distance
    }
    fun unesiNoviDogadjaj(
        naziv: String,
        opis: String,
        user: Korisnik,
        tipName: String,
        datumOdrzavanja: String,
        latitude: Double,
        longitude: Double,
        slika: String
    ) {
        val tipValue = elementsFunc.getTipVrednost(tipName)
        val tipColor = elementsFunc.getTipColor(tipName)
        val tip = Tip(tipName, tipValue, tipColor)
        val eventId = String.format("${naziv}_${latitude}_${longitude}") //custom neki id da bi imao laksu pretragu kasnije
        val noviPoeni = user.poeni+tipValue //kako dodajes nove evente tako ti se povecavaju poeni
        user.poeni = noviPoeni
        val event: Dogadjaj = Dogadjaj(eventId, naziv, opis, user, tip, datumOdrzavanja, latitude, longitude, slika)

        kolekcijaDogadjaja.document(eventId).set(event).addOnSuccessListener {

            database.child("Korisnici").child(user.id).child("poeni")
                .setValue(noviPoeni)

            showSimpleToast("Uspesno ste dodali novi dogadjaj")
        }
            .addOnFailureListener {
                showSimpleToast("Greska u dodavanju novog dogadjaja")
                throw Exception("Greska u dodavanju novog dogadjaja")
            }
    }

    fun izmeniDogadjaj(id: String, naziv: String, opis: String, datum: String, tipName: String) {

        val tipValue = elementsFunc.getTipVrednost(tipName)
        val tipColor = elementsFunc.getTipColor(tipName)
        val tip = Tip(tipName, tipValue, tipColor)
        val updatedData = hashMapOf(
            "naziv" to naziv,
            "opis" to opis,
            "datumOdrzavanja" to datum,
            "tip" to hashMapOf(
                "tip" to tip.tip,
                "vrednost" to tip.vrednost,
                "color" to tip.color
            ),
        ) as HashMap<String, Any>



        kolekcijaDogadjaja.document(id).update(updatedData).addOnCompleteListener {
            if (it.isSuccessful)
                showSimpleToast("Uspesno ste izmenili dogadjaj")
            else {
                showSimpleToast("Niste uspesno izmenili dogadjaj")
                throw Exception("Niste uspesno izmenili dogadjaj")
            }
        }
            .addOnFailureListener {
                showSimpleToast("Niste uspesno izmenili dogadjaj, mozda ne postoji")
                throw Exception("Niste uspesno izmenili dogadjaj, mozda ne postoji")
            }
    }

    fun izbrisiDogadjaj(event: Dogadjaj) {

        val storage  = FirebaseStorage.getInstance()
        storage.getReferenceFromUrl(event.slika).delete().addOnSuccessListener {
            kolekcijaDogadjaja.document(event.id).delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    showSimpleToast("Uspesno ste izbrisali marker")
                } else {
                    showSimpleToast("Niste izbrisali marker")
                    throw Exception("Niste izbrisali marker")
                }
            }
                .addOnFailureListener {
                    showSimpleToast("Niste izbrisali marker, mozda ne postoji")
                    throw Exception("Niste izbrisali marker, mozda ne postoji")
                }
        }

    }
}