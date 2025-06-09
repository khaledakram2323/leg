package com.example.legcurvaturedetection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.legcurvaturedetection.databinding.FragmentTakeanduploadBinding
import java.io.File

class TakeAndUpload : Fragment() {

    private var _binding: FragmentTakeanduploadBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoUri: Uri

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakeanduploadBinding.inflate(inflater, container, false)

        // Navigation
        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_to_help)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        // Take photo
        binding.takePhotoButton.setOnClickListener {
            if (hasPermissions()) {
                launchCamera()
            } else {
                requestPermissions()
            }
        }

        // Upload photo
        binding.uploadPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return binding.root
    }

    private fun hasPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val readStoragePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        val readMediaImagesPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                (readStoragePermission == PackageManager.PERMISSION_GRANTED || readMediaImagesPermission == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES
            ),
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permissions granted", Toast.LENGTH_SHORT).show()
                launchCamera()
            } else {
                val showRationale = permissions.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
                }

                if (!showRationale) {
                    showSettingsDialog()
                } else {
                    Toast.makeText(requireContext(), "Permissions denied. Cannot proceed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Launch the camera
    private fun launchCamera() {
        val imageFile = File(requireContext().cacheDir, "temp_image.jpg")
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        takePicture.launch(photoUri)
    }

    // ✅ Dialog directing user to app settings
    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission was permanently denied. Please enable it in App Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccessful ->
        if (isSuccessful) {
            binding.imageview.setImageURI(photoUri)
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageview.setImageURI(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
