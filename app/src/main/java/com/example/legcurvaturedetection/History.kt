package com.example.legcurvaturedetection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.legcurvaturedetection.databinding.FragmentHistoryBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

        binding.icHome1.setOnClickListener {
            findNavController().navigate(R.id.action_back_to_home)
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
        adapter = HistoryAdapter(historyList) { item ->
            showDetailsDialog(item)
        }

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter
    }

    private fun refreshHistory() {
        historyList.clear()
        historyList.addAll(getHistoryList())
        adapter.notifyDataSetChanged()
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

    private fun showDetailsDialog(item: HistoryItem) {
        val message = "Date: ${item.date}\nPlace: ${item.place}\nResult: ${item.result}"

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
