package com.example.legcurvaturedetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.legcurvaturedetection.databinding.FragmentAboutusBinding
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

class AboutUs : Fragment() {

    private var _binding: FragmentAboutusBinding? = null
    private val binding get() = _binding!!

    private val aboutText = Html.fromHtml( """
      <p>This project represents the collaborative effort of a dedicated team of students united by a common goal: to develop an intelligent system capable of detecting bowed legs using advanced image processing and artificial intelligence techniques.</p>
    
    <p>Our journey was marked by challenges, continuous learning, and countless hours spent on research, development, and rigorous testing. Each team member contributed their unique skills and passion, which were instrumental in transforming this vision into a functional solution.</p>
    
    <p>We take pride in presenting this project as a reflection of not only our technical capabilities but also our commitment to innovation, teamwork, and impact-driven development.</p>
    
    <p>This work was carried out under the esteemed supervision of <b>Dr. Mohammed E. El-Telbany</b>, whose guidance, expertise, and unwavering support played a crucial role in every stage of the development process.</p>
    
    <p><b>Team Members:</b><br>
    • <b>Abdelrahman Shawky</b><br>
    • <b>Ahmed Ayman Ahmed</b><br>
    • <b>Hosney Abdelhamid Ali</b><br>
    • <b>Abdelrahman AboElhassan</b><br>
    • <b>Mohamed Waleed Mohamed Ali</b></p>
    
    <p>We extend our sincere gratitude to everyone who supported and contributed to this project. We hope our work serves as a valuable step forward in the field of medical diagnostics and inspires further innovation.</p>
    """.trimIndent(), Html.FROM_HTML_MODE_LEGACY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutusBinding.inflate(inflater, container, false)

        // Set About Us content
        binding.aboutText.text = aboutText

        // Navigation
        binding.icHome.setOnClickListener {
            findNavController().navigate(R.id.action_to_help)
        }

        binding.icProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_Chat)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
