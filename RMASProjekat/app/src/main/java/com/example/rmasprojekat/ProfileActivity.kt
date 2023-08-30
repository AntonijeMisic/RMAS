package com.example.rmasprojekat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.widget.Toast
import com.example.rmasprojekat.Models.Dogadjaj
import com.example.rmasprojekat.Models.Korisnik
import com.example.rmasprojekat.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val db= FirebaseFirestore.getInstance()
    private var auth= FirebaseAuth.getInstance()
    val database = Firebase.database("https://rmas-projekat-default-rtdb.europe-west1.firebasedatabase.app/").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)


        binding.apply {

            database.child("Korisnici").child(auth.currentUser!!.uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val user = it.getValue(Korisnik::class.java)!!

                    initElements(user)
                    cbxShowPass.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked)
                            etPasswordProfile.transformationMethod =
                                HideReturnsTransformationMethod.getInstance()
                        else
                            etPasswordProfile.transformationMethod =
                                PasswordTransformationMethod.getInstance()
                    }
                    btnSaveProfile.setOnClickListener {

                        if (etImeProfile.text.isNullOrEmpty() || etPrezimeProfile.text.isNullOrEmpty() || etEmailProfile.text.isNullOrEmpty() || etUsernameProfile.text.isNullOrEmpty() || etPasswordProfile.text.isNullOrEmpty()) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Neki od polja nisu validna",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            database.child("Korisnici").child(user.id).child("ime")
                                .setValue(etImeProfile.text.toString())
                            database.child("Korisnici").child(user.id).child("prezime")
                                .setValue(etPrezimeProfile.text.toString())
                            database.child("Korisnici").child(user.id).child("username")
                                .setValue(etUsernameProfile.text.toString())
                            database.child("Korisnici").child(user.id).child("email")
                                .setValue(etEmailProfile.text.toString())
                            database.child("Korisnici").child(user.id).child("password")
                                .setValue(etPasswordProfile.text.toString())

                            val k = Korisnik(
                                user.id,
                                etImeProfile.text.toString(),
                                etPrezimeProfile.text.toString(),
                                etUsernameProfile.text.toString(),
                                etEmailProfile.text.toString(),
                                etPasswordProfile.text.toString(),
                                user.slika,
                                user.poeni
                            )


                            db.collection("Dogadjaji")
                                .whereEqualTo("kreator.id", user.id)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        val dogadjaj = document.toObject(Dogadjaj::class.java)
                                        dogadjaj.kreator = k
                                        db.collection("Dogadjaji").document(dogadjaj.id)
                                            .set(dogadjaj)
                                    }
                                }

                            //mozda dialog za da li ste sigurni
                            Toast.makeText(
                                this@ProfileActivity,
                                "Izmene su sacuvane",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    btnRangLista.setOnClickListener {
                        //otvaram activity sa rang listom svih korisnika
                        val intent = Intent(this@ProfileActivity, RangListActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

        private fun initElements(k: Korisnik) {
            binding.apply {
                etImeProfile.setText(k.ime)
                etPrezimeProfile.setText(k.prezime)
                etUsernameProfile.setText(k.username)
                etEmailProfile.setText(k.email)
                etPasswordProfile.setText(k.password)
                tvPoeni.text=k.poeni.toString()
                Picasso.get().load(k.slika).into(imageProfile)
            }
        }
}