package com.example.legcurvaturedetection

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.legcurvaturedetection.databinding.FragmentTakeanduploadBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Date
import java.util.Locale

class TakeAndUpload : Fragment() {

    private var _binding: FragmentTakeanduploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var photoUri: Uri
    private var currentImageUri: Uri? = null

    companion object {
        const val API_KEY = "cYULQffju5LW0Te07G3r"
        const val REQUEST_CODE_PERMISSIONS = 1001
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakeanduploadBinding.inflate(inflater, container, false)

        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_to_help)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        binding.takePhotoButton.setOnClickListener {
            if (hasPermissions()) launchCamera()
            else requestPermissions()
        }

        binding.uploadPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return binding.root
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                launchCamera()
            } else {
                if (permissions.any {
                        !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)
                    }) {
                    showSettingsDialog()
                } else {
                    toast("Permissions denied.")
                }
            }
        }
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
        val bytes = try {
            requireContext().contentResolver.openInputStream(uri)?.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (bytes == null) {
            toast("Failed to read image.")
            return
        }

        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

        RetrofitInstance.api.inferViaFile(API_KEY, body)
            .enqueue(object : Callback<RoboflowResponse> {
                override fun onResponse(
                    call: Call<RoboflowResponse>,
                    response: Response<RoboflowResponse>
                ) {
                    if (response.isSuccessful) {
                        displayPredictions(response.body()?.predictions)
                    } else {
                        toast("Server error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<RoboflowResponse>, t: Throwable) {
                    toast("Network error: ${t.localizedMessage}")
                }
            })
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
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val historyItem = HistoryItem(
                name = "Leg Scan ${System.currentTimeMillis()}",
                date = currentDate,
                place = "Mobile App",
                result = resultString,
                imageUri = imageUri.toString()
            )

            saveHistoryItem(historyItem)

            toast("Result saved to history")

            dialog.dismiss()

            // Navigate to History screen
            findNavController().navigate(R.id.action_takeAndUpload_to_History)
        }

        dialog.show()
    }
    private fun saveHistoryItem(item: HistoryItem) {
        val sharedPref = requireContext().getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        val gson = Gson()

        // Get existing items
        val existingItems = getHistoryList().toMutableList()

        // Add new item
        existingItems.add(item)

        // Save back
        sharedPref.edit()
            .putString("history_list", gson.toJson(existingItems))
            .apply()
    }

    private fun getHistoryList(): List<HistoryItem> {
        val sharedPref = requireContext().getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("history_list", null)

        return if (json != null) {
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
