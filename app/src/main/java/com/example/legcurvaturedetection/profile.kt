package com.example.legcurvaturedetection

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.legcurvaturedetection.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is authenticated
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profile_to_login)
            return
        }

        // Load user data
        loadUserDataFromFirestore()

        // Set up click listeners
        binding.logout.setOnClickListener { performLogout() }
        binding.settings.setOnClickListener { openAppSettings() }
        binding.profilePic.setOnClickListener { imagePicker.launch("image/*") }
        binding.editIcon.setOnClickListener { showEditProfileDialog() }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_profile_dialog, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.editName)
        val emailInput = dialogView.findViewById<EditText>(R.id.editEmail)
        val ageInput = dialogView.findViewById<EditText>(R.id.editAge)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Pre-fill dialog with current data
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

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate age
            if (updatedAge.isNotEmpty() && (updatedAge.toIntOrNull() == null || updatedAge.toInt() <= 0)) {
                Toast.makeText(requireContext(), "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update UI
            binding.userNameEdit.setText(updatedName)
            binding.userNameText.text = updatedName
            binding.userEmailEdit.setText(updatedEmail)
            binding.age.setText(updatedAge)

            // Save to Firestore
            saveUserDataToFirestore(updatedName, updatedEmail, updatedAge)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveUserDataToFirestore(name: String, email: String, age: String) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userRef = firestore.collection("users").document(userId)

        val data = mapOf(
            "name" to name,
            "email" to email,
            "age" to age
        )

        userRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                Log.d("Firestore", "User data saved successfully for UID: $userId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirestoreError", "Save failed for UID: $userId, Message: ${e.message}", e)
            }
    }

    private fun loadUserDataFromFirestore() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.getString("name")?.let { name ->
                        binding.userNameEdit.setText(name)
                        binding.userNameText.text = name
                    } ?: run {
                        binding.userNameEdit.setText(user.displayName ?: "No Name")
                        binding.userNameText.text = user.displayName ?: "No Name"
                    }
                    document.getString("email")?.let { email ->
                        binding.userEmailEdit.setText(email)
                    } ?: run {
                        binding.userEmailEdit.setText(user.email ?: "No Email")
                    }
                    document.getString("age")?.let { age ->
                        binding.age.setText(age)
                    }
                    document.getString("profileImageUri")?.let { uri ->
                        Glide.with(this)
                            .load(Uri.parse(uri))
                            .circleCrop()
                            .into(binding.profilePic)
                    }
                } else {
                    // If no Firestore data, use Firebase Auth data
                    binding.userNameEdit.setText(user.displayName ?: "No Name")
                    binding.userNameText.text = user.displayName ?: "No Name"
                    binding.userEmailEdit.setText(user.email ?: "No Email")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirestoreError", "Load failed for UID: $userId", e)
            }
    }

    private fun saveProfileImageUri(uri: String) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        firestore.collection("users").document(userId)
            .set(mapOf("profileImageUri" to uri), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image URI saved for UID: $userId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirestoreError", "Failed to save image URI for UID: $userId", e)
            }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_profile_to_login)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}