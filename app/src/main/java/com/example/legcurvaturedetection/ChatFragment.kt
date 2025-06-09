package com.example.legcurvaturedetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.legcurvaturedetection.databinding.FragmentChatBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Pair<String, Boolean>>()
    private val geminiVM: GeminiVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup navigation
        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_back_to_home)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_profile)
        }

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        // Add initial bot greeting
        addWelcomeMessage()

        // Handle send button click
        binding.sendButton.setOnClickListener {
            val message = binding.inputMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.inputMessage.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Please type a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = "Hi! I'm here to help you with leg curvature detection. 👣 Ask me anything!"
        messages.add(Pair(welcomeMessage, false))
        chatAdapter.notifyItemInserted(messages.size - 1)
    }

    private fun sendMessage(message: String) {
        // Add user message to list
        messages.add(Pair(message, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)

        // Get bot response using coroutine
        lifecycleScope.launch {
            try {
                val responseText = geminiVM.getResponse(message)
                messages.add(Pair(responseText, false)) // This will show API errors too
                chatAdapter.notifyItemInserted(messages.size - 1)
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to get response", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
