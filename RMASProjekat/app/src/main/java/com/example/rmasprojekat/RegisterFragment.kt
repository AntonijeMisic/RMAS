package com.example.rmasprojekat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import com.example.rmasprojekat.databinding.FragmentHomeBinding
import com.example.rmasprojekat.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegisterFragment : Fragment() {

    companion object{
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val CAMERA_REQUEST = 1003
    }
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var dataBaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private  var photoUri: Uri? = null
    private var  storage = Firebase.storage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(LayoutInflater.from(context), container, false)
        val database = Firebase.database("https://rmas-projekat-default-rtdb.europe-west1.firebasedatabase.app/")
        auth= FirebaseAuth.getInstance()
        dataBaseRef= database.reference
        storage = FirebaseStorage.getInstance()

        binding.apply {

            btnProfilna.setOnClickListener {
                checkCameraPermissions()
            }

            btnRegister.setOnClickListener {

                if (etIme.text.isNullOrEmpty() || etPrezime.text.isNullOrEmpty() || etEmail.text.isNullOrEmpty() || etUsername.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty() || etConfirm.text.isNullOrEmpty()) {
                    Toast.makeText(activity, "Neka polja nisu validna", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    if (etPassword.text.toString() == etConfirm.text.toString()) {
                        if(photoUri!=null)
                        {
                            RegisterUser(etIme, etPrezime, etUsername, etEmail, etPassword)
                        }
                        else
                        {
                            Toast.makeText(activity, "Morate da unesete sliku", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else {
                        Toast.makeText(activity, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        return binding.root
    }


    private fun RegisterUser(etIme: EditText, etPrezime: EditText, etUsername: EditText, etEmail: EditText, etPassword: EditText)
    {
        auth.createUserWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString()).addOnCompleteListener {
            if(it.isSuccessful)
            {
                val currUser=auth.currentUser
                if(currUser!=null && photoUri!=null)
                {
                    storage.getReference("userImages").child(System.currentTimeMillis().toString()).putFile(photoUri!!)
                        .addOnSuccessListener {
                            it.metadata!!.reference!!.downloadUrl
                                .addOnSuccessListener {
                                    val slikaUri = it
                                    //ja moram da sacuvam uri slike i da ga ubacim u storage i u bazu ovde
                                    dataBaseRef.child("Korisnici").addListenerForSingleValueEvent(object :ValueEventListener{

                                        override fun onDataChange(snapshot: DataSnapshot) {

                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("id").setValue(currUser.uid.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("ime").setValue(etIme.text.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("prezime").setValue(etPrezime.text.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("username").setValue(etUsername.text.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("email").setValue(etEmail.text.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("password").setValue(etPassword.text.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("slika").setValue(slikaUri.toString())
                                            dataBaseRef.child("Korisnici").child(currUser.uid).child("poeni").setValue(0)

                                            Toast.makeText(
                                                activity,
                                                "Uspesno ste se registrovali",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            view?.findNavController()
                                                ?.navigate(R.id.action_registerFragment_to_loginFragment)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(activity, "Greskaa", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                                .addOnFailureListener {
                                    Toast.makeText(activity, "Neuspesno postavljanje slike", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
                else
                    Toast.makeText(activity, "Greska pri registraciji", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(activity, "Greska pri registraciji", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkCameraPermissions(){
        if(ContextCompat.checkSelfPermission(activity!!,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            openCamera()
        }
        else
        {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }
    private fun openCamera()
    {
        if(ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent, CAMERA_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera()
            }
            else
                Toast.makeText(activity, "Greska", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == CAMERA_REQUEST && resultCode== Activity.RESULT_OK) {

            val photoBitmap = data?.extras?.get("data") as Bitmap
            photoUri = saveBitmapToFile(context!!, photoBitmap)
            binding.apply {
                tvSlikaDodataRegister.visibility=View.VISIBLE
                btnProfilna.isEnabled=false
            }

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



}