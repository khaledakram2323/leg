package com.example.legcurvaturedetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.legcurvaturedetection.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class History : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_history_to_loginFragment)
            return binding.root
        }

        binding.icHome1.setOnClickListener {
            findNavController().navigate(R.id.action_to_help)
        }

        binding.icProfile1.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        setupRecyclerView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            historyList,
            onItemClick = { item -> showDetailsDialog(item) },
            onDeleteClick = { item -> deleteHistoryItem(item) }
        )

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter
    }

    private fun refreshHistory() {
        getHistoryList { items ->
            historyList.clear()
            historyList.addAll(items)
            adapter.notifyDataSetChanged()
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
                            documentId = doc.id,
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
                Toast.makeText(
                    requireContext(),
                    "Failed to load history: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(emptyList())
            }
    }

    private fun deleteHistoryItem(item: HistoryItem) {
        if (item.documentId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid history item", Toast.LENGTH_SHORT).show()
            return
        }

        // Delete image from Firebase Storage if it exists and is a valid URL
        item.imageUri?.let { uri ->
            try {
                val storageRef = storage.getReferenceFromUrl(uri)
                storageRef.delete()
                    .addOnSuccessListener {
                        deleteFirestoreDocument(item)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Proceed to delete Firestore document even if image deletion fails
                        deleteFirestoreDocument(item)
                    }
            } catch (e: IllegalArgumentException) {
                // Invalid URL (e.g., local URI), skip image deletion
                android.util.Log.w("StorageError", "Invalid image URI: $uri", e)
                deleteFirestoreDocument(item)
            }
        } ?: run {
            // No image to delete
            deleteFirestoreDocument(item)
        }
    }

    private fun deleteFirestoreDocument(item: HistoryItem) {
        db.collection("history").document(item.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "History item deleted", Toast.LENGTH_SHORT).show()
                refreshHistory()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete history item: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDetailsDialog(item: HistoryItem) {
        val message = "Date: ${item.date}\nResult: ${item.result}"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(item.name)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}