package com.studyflowai.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.studyflowai.MainActivity
import com.studyflowai.R
import com.studyflowai.databinding.ActivityWelcomeBinding
import com.studyflowai.ui.base.BaseActivity
import java.util.Locale

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                binding.btnStart.isEnabled = true
                Toast.makeText(this, "Connexion échouée", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.btnStart.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. Appliquer le Gradient au Titre
        setupGradientTitle()

        // 2. Boutons Nav
        binding.btnTheme.setOnClickListener { toggleTheme() }
        binding.btnLanguage.setOnClickListener { toggleLanguage() }

        // 3. Affichage direct
        binding.ivHeroLogo.alpha = 1f
        binding.tvTitle.alpha = 1f
        binding.tvSubtitle.alpha = 1f
        binding.llActions.alpha = 1f

        if (auth.currentUser != null) {
            goToMainActivity()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("719367275494-g95d909p95f7u5b6q6j087q89s7o0p6r.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnStart.setOnClickListener {
            animateButtonClick(it)
            binding.btnStart.isEnabled = false
            signInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.btnGuest.setOnClickListener {
            animateButtonClick(it)
            goToMainActivity()
        }
    }

    private fun setupGradientTitle() {
        // Désactivé temporairement pour stabiliser l'UX
        binding.tvTitle.setTextColor(getColorFromAttr(com.google.android.material.R.attr.colorPrimary))
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun runStartupAnimations() {
        // Préparation des états initiaux
        binding.ivHeroLogo.alpha = 0f
        binding.ivHeroLogo.scaleX = 0.5f
        binding.ivHeroLogo.scaleY = 0.5f
        
        binding.vLogoGlow.alpha = 0f
        binding.vLogoGlow.scaleX = 0f
        binding.vLogoGlow.scaleY = 0f

        binding.tvTitle.alpha = 0f
        binding.tvTitle.translationY = 80f
        
        binding.tvSubtitle.alpha = 0f
        binding.tvSubtitle.translationY = 40f
        
        binding.llActions.alpha = 0f
        binding.llActions.translationY = 150f

        // Séquence d'animation Spring optimisée
        val springInterpolator = AnticipateOvershootInterpolator(1.2f)

        binding.ivHeroLogo.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(1000).setInterpolator(springInterpolator).setStartDelay(200).start()
            
        binding.vLogoGlow.animate().alpha(0.15f).scaleX(1f).scaleY(1f)
            .setDuration(1500).setStartDelay(400).start()
            
        binding.tvTitle.animate().alpha(1f).translationY(0f)
            .setDuration(900).setInterpolator(springInterpolator).setStartDelay(500).start()
            
        binding.tvSubtitle.animate().alpha(1f).translationY(0f)
            .setDuration(900).setStartDelay(700).start()
            
        binding.llActions.animate().alpha(1f).translationY(0f)
            .setDuration(1000).setInterpolator(springInterpolator).setStartDelay(900).start()
    }

    private fun animateButtonClick(view: View) {
        view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    private fun toggleTheme() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        val nextMode = !isDark
        
        prefs.edit().putBoolean("dark_mode", nextMode).apply()
        
        AppCompatDelegate.setDefaultNightMode(
            if (nextMode) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun toggleLanguage() {
        val languages = arrayOf("Français", "English", "العربية")
        val langCodes = arrayOf("fr", "en", "ar")
        
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentLang = prefs.getString("language", "fr") ?: "fr"
        val checkedItem = langCodes.indexOf(currentLang)

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.CalendarTheme)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val nextLang = langCodes[which]
                prefs.edit().putString("language", nextLang).apply()
                
                dialog.dismiss()
                
                // Redémarrer l'app pour appliquer la langue partout
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) goToMainActivity()
            else {
                binding.btnStart.isEnabled = true
                Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMainActivity() {
        val prefs = getSharedPreferences("studyflow_prefs", MODE_PRIVATE)
        val onboardingComplete = prefs.getBoolean("onboarding_complete", false)
        
        if (!onboardingComplete) {
            startActivity(Intent(this, com.studyflowai.ui.onboarding.OnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
