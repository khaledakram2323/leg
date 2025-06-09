package com.example.legcurvaturedetection

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.legcurvaturedetection.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.profilePic)
            saveProfileImageUri(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadFirebaseUser()
        loadProfileImage()
        loadStoredUserData()

        binding.logout.setOnClickListener { performLogout() }
        binding.settings.setOnClickListener { openAppSettings() }
        binding.profilePic.setOnClickListener { imagePicker.launch("image/*") }

        binding.editIcon.setOnClickListener {
            showEditProfileDialog()
        }

        return binding.root
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_profile_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.editName)
        val emailInput = dialogView.findViewById<EditText>(R.id.editEmail)
        val ageInput = dialogView.findViewById<EditText>(R.id.editAge)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        nameInput.setText(binding.userNameEdit.text.toString())
        emailInput.setText(binding.userEmailEdit.text.toString())
        ageInput.setText(binding.age.text.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val updatedName = nameInput.text.toString().trim()
            val updatedEmail = emailInput.text.toString().trim()
            val updatedAge = ageInput.text.toString().trim()

            binding.userNameEdit.setText(updatedName)
            binding.userEmailEdit.setText(updatedEmail)
            binding.age.setText(updatedAge)
            binding.userNameText.text = updatedName

            saveUserDataToFirestore(updatedName, updatedEmail, updatedAge)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadFirebaseUser() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val name = user.displayName
            val email = user.email

            if (!name.isNullOrEmpty()) {
                binding.userNameEdit.setText(name)
                binding.userNameText.text = name
                saveUserName(name)
            } else {
                binding.userNameText.text = "No Name"
            }

            binding.userEmailEdit.setText(email ?: "No Email")
            email?.let { saveUserEmail(it) }

        } else {
            binding.userNameText.text = "No Name"
            binding.userEmailEdit.setText("No Email")
        }

        loadUserDataFromFirestore()
    }

    private fun saveUserDataToFirestore(name: String, email: String, age: String) {
        val user = auth.currentUser
        user?.let {
            val userId = user.uid
            val userRef = firestore.collection("users").document(userId)

            val data = mapOf(
                "name" to name,
                "email" to email,
                "age" to age
            )

            userRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    saveUserName(name)
                    saveUserEmail(email)
                    saveUserAge(age)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun loadUserDataFromFirestore() {
        val user = auth.currentUser
        user?.let {
            val userId = user.uid
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.getString("name")?.let {
                            binding.userNameEdit.setText(it)
                            binding.userNameText.text = it
                            saveUserName(it)
                        }
                        document.getString("email")?.let {
                            binding.userEmailEdit.setText(it)
                            saveUserEmail(it)
                        }
                        document.getString("age")?.let {
                            binding.age.setText(it)
                            saveUserAge(it)
                        }
                    }
                }
        }
    }

    private fun saveUserName(name: String) {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("userName", name).apply()
    }

    private fun saveUserEmail(email: String) {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("userEmail", email).apply()
    }

    private fun saveUserAge(age: String) {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("userAge", age).apply()
    }

    private fun loadStoredUserData() {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.getString("userName", null)?.let {
            binding.userNameEdit.setText(it)
            binding.userNameText.text = it
        }
        prefs.getString("userEmail", null)?.let {
            binding.userEmailEdit.setText(it)
        }
        prefs.getString("userAge", null)?.let {
            binding.age.setText(it)
        }
    }

    private fun saveProfileImageUri(uri: String) {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("profileImageUri", uri).apply()
    }

    private fun loadProfileImage() {
        val prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        prefs.getString("profileImageUri", null)?.let {
            Glide.with(this)
                .load(Uri.parse(it))
                .circleCrop()
                .into(binding.profilePic)
        }
    }

    private fun performLogout() {
        val loginPrefs = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        loginPrefs.edit().clear().apply()
        auth.signOut()
        findNavController().navigate(R.id.action_profile_to_login)
        requireActivity().finish()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
