package com.studyflowai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.studyflowai.R
import com.studyflowai.databinding.BottomSheetHelpBinding
import com.studyflowai.ui.adapter.HelpPage
import com.studyflowai.ui.adapter.HelpPagerAdapter

class HelpBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pages = listOf(
            HelpPage("Bienvenue 🚀", "StudyFlow AI utilise l'intelligence artificielle pour transformer votre manière d'étudier.", android.R.drawable.ic_dialog_info),
            HelpPage("Pourquoi une clé ?", "Pour que votre Coach IA puisse vous parler, il a besoin d'une clé API Groq personnelle.", android.R.drawable.ic_lock_idle_lock),
            HelpPage("C'est quoi Groq ?", "C'est un moteur ultra-rapide qui permet à l'IA de répondre instantanément à vos besoins.", android.R.drawable.ic_media_play),
            HelpPage("Étape 1 : Obtenir", "Allez sur console.groq.com (gratuit), créez un compte et générez votre 'API Key'.", android.R.drawable.ic_menu_edit),
            HelpPage("Étape 2 : Configurer", "Cliquez sur l'icône 'Globe' en haut du Dashboard et collez votre clé précieusement.", android.R.drawable.ic_menu_set_as),
            HelpPage("Prêt à briller ?", "Félicitations ! Votre planning va maintenant devenir vraiment intelligent. À vous de jouer !", android.R.drawable.btn_star_big_on)
        )

        val adapter = HelpPagerAdapter(pages)
        binding.viewPagerHelp.adapter = adapter

        // Dots indicator logic
        TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerHelp) { _, _ -> }.attach()

        binding.viewPagerHelp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnPrev.visibility = if (position > 0) View.VISIBLE else View.GONE
                binding.btnNext.text = if (position == pages.size - 1) "C'EST PARTI !" else "SUIVANT"
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPagerHelp.currentItem < pages.size - 1) {
                binding.viewPagerHelp.currentItem += 1
            } else {
                dismiss()
            }
        }

        binding.btnPrev.setOnClickListener {
            binding.viewPagerHelp.currentItem -= 1
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HelpBottomSheet"
    }
}
