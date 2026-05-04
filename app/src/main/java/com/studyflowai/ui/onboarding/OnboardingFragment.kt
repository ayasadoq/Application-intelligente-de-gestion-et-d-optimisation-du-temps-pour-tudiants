package com.studyflowai.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.studyflowai.databinding.FragmentOnboardingPageBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title")
        val desc = arguments?.getString("desc")
        val img = arguments?.getInt("img") ?: 0

        binding.tvTitle.text = title
        binding.tvDescription.text = desc
        binding.ivOnboarding.setImageResource(img)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, desc: String, img: Int): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("desc", desc)
                    putInt("img", img)
                }
            }
        }
    }
}
