package com.example.rmasprojekat.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rmasprojekat.Models.Korisnik
import com.example.rmasprojekat.R
import com.squareup.picasso.Picasso

class UsersAdapter(private var lista: MutableList<Korisnik>): RecyclerView.Adapter<UsersViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rang_list_item, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val korisnik = lista[position]
        holder.tvIndeks.text = String.format("${position + 1}.")
        holder.tvImePrezime.text = korisnik.ime + " " + korisnik.prezime
        holder.tvKorisnikPoeni.text = korisnik.poeni.toString() + " " + "poena"
        Picasso.get().load(korisnik.slika).into(holder.imgUser)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

}
class UsersViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
    val tvIndeks = ItemView.findViewById<TextView>(R.id.tvIndeks)
    val tvImePrezime = ItemView.findViewById<TextView>(R.id.tvImePrezime)
    val tvKorisnikPoeni = ItemView.findViewById<TextView>(R.id.tvKorisnikPoeni)
    val imgUser  =ItemView.findViewById<ImageView>(R.id.imageUser)
}


