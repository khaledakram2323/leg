package com.example.legcurvaturedetection

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.legcurvaturedetection.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private val instructionsText = Html.fromHtml("""
        <p><b>Instructions:</b></p>
        <p>To allow the AI model to accurately classify your leg posture, please follow these guidelines while taking a photo:</p>
        <ul>
            <li>Stand straight on a flat surface with your legs shoulder-width apart.</li>
            <li>Make sure both legs are clearly visible from the front view.</li>
            <li>Ensure good lighting – avoid shadows or dim environments.</li>
            <li>Wear clothes that do not obscure your leg shape (e.g., shorts or fitted pants).</li>
            <li>Capture the image from knee level or slightly below to ensure clarity.</li>
        </ul>
        <p>Once ready, click the button below to take a photo. The AI system will analyze the image and detect any signs of bowed legs.</p>
    """.trimIndent(), Html.FROM_HTML_MODE_LEGACY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)

        binding.instructionsText.text = instructionsText

        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_to_home)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_chat)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
