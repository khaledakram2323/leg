package com.example.legcurvaturedetection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class Login : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var rememberMeCheckBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        emailEditText = view.findViewById(R.id.login_email_et)
        passwordEditText = view.findViewById(R.id.login_password_et)
        loginButton = view.findViewById(R.id.register_btn)
        rememberMeCheckBox = view.findViewById(R.id.rememberme_cb)

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Check if user is remembered
        val isRemembered = sharedPref.getBoolean("remember_me", false)
        val currentUser = auth.currentUser
        if (isRemembered && currentUser != null) {
            findNavController().navigate(R.id.action_login_to_home)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (rememberMeCheckBox.isChecked) {
                            sharedPref.edit().putBoolean("remember_me", true).apply()
                        } else {
                            sharedPref.edit().putBoolean("remember_me", false).apply()
                        }

                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        val error = task.exception?.message ?: "Login failed"
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Navigate to Signup
        view.findViewById<TextView>(R.id.go_to_signup).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signup)
        }
    }
}
