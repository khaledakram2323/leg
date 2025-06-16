package com.example.legcurvaturedetection

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.legcurvaturedetection.databinding.FragmentTakeanduploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TakeAndUpload : Fragment() {

    private var _binding: FragmentTakeanduploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var photoUri: Uri
    private var currentImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private var isButtonClickInProgress = false
    private var waitAnimator: ObjectAnimator? = null


    companion object {
        const val API_KEY = "cYULQffju5LW0Te07G3r"
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L // 1-second debounce
    }

    private val requiredPermissions = mutableListOf(
        Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isButtonClickInProgress = false
        if (permissions.all { it.value }) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Permissions denied. Some features may not work.", Toast.LENGTH_SHORT).show()
            showSettingsDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakeanduploadBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_to_help)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        binding.takePhotoButton.setOnClickListener {
            if (!isButtonClickInProgress) {
                isButtonClickInProgress = true
                if (hasPermissions()) {
                    launchCamera()
                } else {
                    requestPermissions()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    isButtonClickInProgress = false
                }, CLICK_DEBOUNCE_DELAY_MS)
            }
        }

        binding.uploadPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return view
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(requiredPermissions)
    }

    private fun launchCamera() {
        val imageFile = File(requireContext().cacheDir, "temp_image.jpg")
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        takePictureLauncher.launch(photoUri)
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            currentImageUri = photoUri
            binding.imageview.setImageURI(photoUri)
            uploadImage(photoUri)
        } else {
            toast("Failed to take picture.")
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            binding.imageview.setImageURI(it)
            uploadImage(it)
        }
    }

    private fun uploadImage(uri: Uri) {
        // Show ProgressBar and start animation
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.wait.visibility = View.VISIBLE
        waitAnimator = ObjectAnimator.ofFloat(binding.wait, "rotation", 0f, 360f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val compressedBytes = try {
                val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                bitmap.recycle()
                outputStream.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            withContext(Dispatchers.Main) {
                if (compressedBytes == null) {
                    stopSpinner()
                    toast("Failed to read image.")
                    return@withContext
                }

                val requestBody = compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

                RetrofitInstance.api.inferViaFile(API_KEY, body)
                    .enqueue(object : Callback<RoboflowResponse> {
                        override fun onResponse(
                            call: Call<RoboflowResponse>,
                            response: Response<RoboflowResponse>
                        ) {
                            stopSpinner()
                            if (response.isSuccessful) {
                                displayPredictions(response.body()?.predictions)
                            } else {
                                toast("Server error: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<RoboflowResponse>, t: Throwable) {
                            stopSpinner()
                            toast("Network error: ${t.localizedMessage}")
                        }
                    })
            }
        }
    }


    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Permissions are required to use this feature. Please enable them in app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun displayPredictions(pred: List<Prediction>?) {
        if (pred.isNullOrEmpty()) {
            toast("No objects detected")
            return
        }

        currentImageUri?.let {
            showResultDialog(pred, it)
        } ?: toast("Image URI not available")
    }

    private fun showResultDialog(predictions: List<Prediction>, imageUri: Uri) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_result, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
        val resultText = dialogView.findViewById<TextView>(R.id.dialogResultText)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        imageView.setImageURI(imageUri)

        val resultBuilder = StringBuilder()
        predictions.forEach {
            resultBuilder.append("${it.`class`} (${String.format("%.2f", it.confidence * 100)}%)\n")
        }
        val resultString = resultBuilder.toString()
        resultText.text = resultString

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        saveButton.setOnClickListener {
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val newItem = HistoryItem(
                name = "Leg Analysis",
                date = currentDate,
                result = resultString.trim(),
                imageUri = imageUri.toString()
            )

            saveHistoryItem(newItem)
            toast("Saved to history.")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveHistoryItem(item: HistoryItem) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""
        val historyData = hashMapOf(
            "name" to item.name,
            "date" to item.date,
            "result" to item.result,
            "imageUri" to item.imageUri,
            "userId" to userId
        )
        android.util.Log.d("FirestoreData", "Saving: $historyData")

        db.collection("history")
            .add(historyData)
            .addOnSuccessListener {
                toast("Successfully saved to Firestore")
            }
            .addOnFailureListener { e ->
                toast("Failed to save to Firestore: ${e.message}")
                android.util.Log.e("FirestoreError", "Save failed", e)
            }
    }

    private fun getHistoryList(callback: (List<HistoryItem>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        db.collection("history")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val historyList = result.documents.mapNotNull { doc ->
                    try {
                        HistoryItem(
                            name = doc.getString("name") ?: "",
                            date = doc.getString("date") ?: "",
                            result = doc.getString("result") ?: "",
                            imageUri = doc.getString("imageUri"),
                            userId = doc.getString("userId")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                callback(historyList)
            }
            .addOnFailureListener { e ->
                toast("Failed to load history: ${e.message}")
                callback(emptyList())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun stopSpinner() {
        waitAnimator?.cancel()
        binding.wait.visibility = View.GONE
        binding.loadingOverlay.visibility = View.GONE
    }

}