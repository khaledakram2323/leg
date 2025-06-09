package com.example.legcurvaturedetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.legcurvaturedetection.databinding.FragmentHOMEBinding

class HOME : Fragment() {

    private var _binding: FragmentHOMEBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHOMEBinding.inflate(inflater, container, false)

        binding.card1LearnMoreButton.setOnClickListener {
            showPopup(
                "1. What are bowed legs?",
                "Bowed legs, medically known as genu varum, is a condition where a person's legs curve outward at the knees while the feet and ankles stay close together. This creates a visible gap between the knees when standing with the feet flat and together.\n\n" +
                        "In infants, bowed legs can be a normal part of growth and may correct naturally with time. However, when the condition persists beyond early childhood or appears in adolescents or adults, it may be a sign of an underlying bone condition that requires medical attention."
            )
        }

        binding.card2LearnMoreButton.setOnClickListener {
            showPopup(
                "2. What causes bowed legs?",
                "Bowed legs can have several causes depending on age and health conditions. In young children, it is often due to the natural positioning in the womb and usually straightens with growth.\n\n" +
                        "In older children and adults, it may be caused by:\n" +
                        "• Rickets: A bone disease caused by a deficiency of vitamin D, calcium, or phosphate.\n" +
                        "• Blount’s Disease: A growth disorder affecting the shin bone, causing it to curve outward.\n" +
                        "• Bone malnutrition or genetic conditions that affect bone strength and development.\n" +
                        "• In some cases, past injuries or bone infections may also lead to deformities resulting in bowed legs."
            )
        }

        binding.card3LearnMoreButton.setOnClickListener {
            showPopup(
                "3. Can bowed legs be treated?",
                "Yes, bowed legs can be treated, and the type of treatment depends on the severity of the condition and its underlying cause.\n\n" +
                        "For mild cases in children, no treatment may be necessary as the legs often straighten naturally with growth. However, if the condition persists or worsens, treatment options include:\n" +
                        "• Nutritional supplements such as vitamin D or calcium if a deficiency is the cause.\n" +
                        "• Bracing to help guide bone growth in young children.\n" +
                        "• Physical therapy to improve leg strength and alignment.\n" +
                        "• Surgery in severe or advanced cases, especially in adults or when bracing and therapy are not effective.\n\n" +
                        "Early diagnosis plays a key role in successful treatment and preventing long-term complications."
            )
        }

        binding.card4LearnMoreButton.setOnClickListener {
            showPopup(
                "4. How can AI help detect bowed legs?",
                "Artificial Intelligence (AI) and image processing technologies can revolutionize the early detection of bowed legs by analyzing leg images accurately and efficiently. Using deep learning algorithms, AI systems can automatically assess the curvature of the legs, identify abnormalities, and classify them as normal or bowed. This is especially useful in environments where access to medical specialists is limited. AI-based tools can assist doctors by providing a second opinion, reducing human error, and enabling mass screenings in schools, military settings, or remote clinics. Early detection using AI allows for timely intervention, which can significantly improve treatment outcomes."
            )
        }

        return binding.root
    }

    private fun showPopup(title: String, message: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_dialog, null, false)

        val titleView = dialogView.findViewById<TextView>(R.id.popupTitle)
        val messageView = dialogView.findViewById<TextView>(R.id.popupMessage)
        val closeButton = dialogView.findViewById<Button>(R.id.closeBtn)

        titleView.text = title
        messageView.text = message

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
