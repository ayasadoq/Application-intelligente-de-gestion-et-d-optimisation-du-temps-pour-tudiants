package com.studyflowai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Entité Task représentant une mission étudiante.
 * Respecte le cahier des charges BF1 et BF3.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,      // ID de l'utilisateur Firebase
    val title: String,
    val description: String,
    val deadline: Long,      // Timestamp
    val priority: Int,      // 1: Basse, 2: Moyenne, 3: Haute
    val estimatedTime: Int, // En minutes
    var realTime: Int = 0,   // En minutes (cumulé via chrono)
    var isCompleted: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
) : Serializable {
    
    /**
     * BF2 - Algorithme de Priorisation intelligente
     * Score = (Urgence * Importance) / TempsDisponible
     * Note: TempsDisponible est passé en paramètre ici pour le calcul.
     */
    fun calculateScore(availableTime: Int): Double {
        val now = System.currentTimeMillis()
        val daysRemaining = ((deadline - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(1L).toDouble()
        
        // Urgence : Inversement proportionnelle aux jours restants
        val urgency = 10.0 / daysRemaining
        
        // Importance : basée sur la priorité
        val importance = priority.toDouble()
        
        // On évite la division par zéro
        val timeFactor = if (availableTime > 0) availableTime.toDouble() else 60.0
        
        return (urgency * importance * 100) / timeFactor
    }
}

