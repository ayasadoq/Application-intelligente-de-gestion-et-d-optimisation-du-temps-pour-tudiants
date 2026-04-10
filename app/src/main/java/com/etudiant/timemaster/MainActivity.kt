package com.etudiant.timemaster

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.etudiant.timemaster.data.database.TacheDatabase
import com.etudiant.timemaster.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation de la navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Liaison avec la Bottom Navigation
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_export) {
            exportToCSV()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exportToCSV() {
        lifecycleScope.launch {
            val database = TacheDatabase.getDatabase(this@MainActivity)
            val allTaches = database.tacheDao().getAllTaches().first()
            
            val csvContent = StringBuilder("ID,Titre,Description,Priorite,DureeEstimee,DureeReelle,Statut\n")
            allTaches.forEach {
                csvContent.append("${it.id},${it.titre},${it.description},${it.priorite},${it.dureeEstimee},${it.dureeReelle},${if (it.estTerminee) "Terminee" else "En cours"}\n")
            }

            try {
                val fileName = "TimeMaster_Export_${System.currentTimeMillis()}.csv"
                val file = File(getExternalFilesDir(null), fileName)
                FileOutputStream(file).use { it.write(csvContent.toString().toByteArray()) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "CSV exporté dans : ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur lors de l'export", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
