package com.example.rmasprojekat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.rmasprojekat.databinding.FragmentHomeBinding
import com.example.rmasprojekat.databinding.FragmentLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.security.AuthProvider


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(LayoutInflater.from(context), container, false)
        auth = FirebaseAuth.getInstance()

        binding.apply {
            btnSubmit.setOnClickListener {

                if (!etUsername.text.isNullOrEmpty()) {
                    if (!etPassword.text.isNullOrEmpty()) {
                        loginUser(etUsername.text.toString(), etPassword.text.toString())

                    } else {
                        Toast.makeText(activity, "Unesite lozinka", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Unesite username", Toast.LENGTH_SHORT).show()
                }

            }

            tvRegistrujteSe.setOnClickListener {
                it.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
        return binding.root
    }

    private fun loginUser(email: String, password: String)
    {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful)
            {
                Toast.makeText(activity, "Uspesno prijavljivanje", Toast.LENGTH_SHORT).show()
                view?.findNavController()?.navigate(R.id.action_loginFragment_to_mapActivity)
            }
            else Toast.makeText(activity, "Neuspesno prijavljivanje", Toast.LENGTH_SHORT).show()
        }
    }


}