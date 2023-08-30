package com.example.rmasprojekat.Adapters

import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.example.rmasprojekat.Dialogs.InfoDialog
import com.example.rmasprojekat.Functionality.DogadjajiFunctionality
import com.example.rmasprojekat.Functionality.ElementsFunctionality
import com.example.rmasprojekat.Models.Dogadjaj
import com.example.rmasprojekat.Models.DogadjajViewModel
import com.example.rmasprojekat.R
import com.example.rmasprojekat.databinding.InfoDialogBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecycleViewAdapter(private var lista: List<Dogadjaj>, private var context: Context, private var dogadjajiFunctionality: DogadjajiFunctionality): RecyclerView.Adapter<EventViewHolder>() {

    private  lateinit var infoDialogView: View
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dogadjajiFunc: DogadjajiFunctionality = dogadjajiFunctionality

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = lista[position]
        holder.nazivText.text = event.naziv
        val niz = event.datumOdrzavanja.split(" ")
        holder.datumText.text = niz[0]
        holder.vremeText.text=niz[1]
        holder.organizatorText.text = event.kreator.ime + " " + event.kreator.prezime
        Picasso.get().load(event.slika).into(holder.slika)

        holder.itemView.setOnClickListener {
            val lat = LatLng(event.latitude, event.longitude)
            val marker = dogadjajiFunc.markerList.firstOrNull() { it.position==lat }
            val infoDialog = InfoDialog(context)
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
                if(event.kreator.id!= auth.currentUser?.uid)
                    llDugmici.isVisible=false
                else
                {
                    llDugmici.isVisible=true
                    btnIzmeni.setOnClickListener {
                        if(marker!=null)
                        {
                            dogadjajiFunc.izmeniDogadjaj(event, infoDialog, marker)
                        }
                    }
                    btnIzbrisi.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}
class EventViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView)
{
    val nazivText = ItemView.findViewById<TextView>(R.id.tvNazivTabela)
    val datumText = ItemView.findViewById<TextView>(R.id.tvDatumTabela)
    val organizatorText = ItemView.findViewById<TextView>(R.id.tvOrganizatorTabela)
    val vremeText = ItemView.findViewById<TextView>(R.id.tvSatiTabela)
    val slika = ItemView.findViewById<ImageView>(R.id.slikaDogadjaja)


}