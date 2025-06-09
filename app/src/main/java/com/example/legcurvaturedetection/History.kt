package com.example.legcurvaturedetection

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.legcurvaturedetection.databinding.FragmentHistoryBinding

class History : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Navigation buttons
        binding.icHome1.setOnClickListener {
            findNavController().navigate(R.id.action_back_to_home)
        }

        binding.icProfile1.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        setupRecyclerView()
        loadSampleHistory()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(historyList) { item ->
            showDetailsDialog(item)
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter
    }

    private fun loadSampleHistory() {
        historyList.add(HistoryItem("Scan 1", "2025-01-01", "Clinic A"))
        historyList.add(HistoryItem("Scan 2", "2025-02-15", "Clinic B"))
        historyList.add(HistoryItem("Scan 3", "2025-03-10", "Clinic C"))
        adapter.notifyDataSetChanged()
    }

    private fun showDetailsDialog(item: HistoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(item.name)
            .setMessage("Date: ${item.date}\nPlace: ${item.place}")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
