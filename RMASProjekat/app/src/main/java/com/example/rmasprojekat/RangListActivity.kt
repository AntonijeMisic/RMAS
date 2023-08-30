package com.example.rmasprojekat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rmasprojekat.Adapters.UsersAdapter
import com.example.rmasprojekat.Models.Korisnik
import com.example.rmasprojekat.databinding.ActivityRangListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RangListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRangListBinding
    val database = Firebase.database("https://rmas-projekat-default-rtdb.europe-west1.firebasedatabase.app/").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRangListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        getUsers()//moze sa viewModel za korisnika posto moram da observe svaku promenu koja se desi

    }

    private fun getUsers()
    {
        database.child("Korisnici").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val korisnici: MutableList<Korisnik> = mutableListOf()
                for (korisnikSnapshot in snapshot.children) {
                    val korisnik = korisnikSnapshot.getValue(Korisnik::class.java)
                    if (korisnik != null) {
                        korisnici.add(korisnik)
                    }
                }
                //moram da sortiram korisnike pre nego da ih stavim u adapter
                korisnici.sortByDescending { it.poeni }

                val rvRangList = findViewById<RecyclerView>(R.id.rvRangList)
                rvRangList.layoutManager = LinearLayoutManager(this@RangListActivity)
                val adapter = UsersAdapter(korisnici)
                rvRangList.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}