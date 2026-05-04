package com.studyflowai.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.studyflowai.MainActivity
import com.studyflowai.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf(
            OnboardingFragment.newInstance("Bienvenue sur StudyFlow AI", "L'intelligence artificielle au service de ta réussite scolaire.", android.R.drawable.ic_lock_idle_alarm),
            OnboardingFragment.newInstance("BYOK : Ta clé, Ta vie", "Sécurise tes données avec ta propre clé API Groq (Llama 3).", android.R.drawable.ic_lock_lock),
            OnboardingFragment.newInstance("Smart Planning", "Génère des plannings optimisés en une phrase simple.", android.R.drawable.ic_menu_agenda),
            OnboardingFragment.newInstance("Coach Proactif", "Ton coach AI t'aide à surmonter tes blocages en temps réel.", android.R.drawable.ic_menu_help),
            OnboardingFragment.newInstance("Prêt à décoller ?", "Finalise ta configuration et commence à transformer tes études.", android.R.drawable.ic_menu_send)
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = pages.size
            override fun createFragment(position: Int): Fragment = pages[position]
        }

        // Liaison du TabLayout avec le ViewPager2 pour les indicateurs
        com.google.android.material.tabs.TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < pages.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences("studyflow_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
