package com.etudiant.timemaster.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titre: String,
    val description: String,
    val dateLimite: Long, // Timestamp
    val priorite: Int,    // 1: Basse, 2: Moyenne, 3: Haute
    val dureeEstimee: Int, // En minutes
    var dureeReelle: Int = 0, // En secondes (total chronométré)
    var estTerminee: Boolean = false,
    var dateCreation: Long = System.currentTimeMillis()
) : Serializable {
    // BF2 - Priorisation Intelligente
    // Calcule un score basé sur l'Urgence (60%), l'Importance (30%) et l'Effort/Temps (10%)
    val scorePriorite: Double
        get() {
            val maintenant = System.currentTimeMillis()
            val delaiRestant = (dateLimite - maintenant).toDouble()
            
            // 1. Score d'Urgence (0 à 100)
            // Plus l'échéance est proche (ou passée), plus le score est élevé
            val urgenceScore = when {
                delaiRestant <= 0 -> 100.0 // Déjà en retard
                delaiRestant > 7 * 24 * 3600000 -> 10.0 // Plus d'une semaine
                else -> 100.0 * (1.0 - (delaiRestant / (7 * 24 * 3600000.0)))
            }

            // 2. Score d'Importance (basé sur la priorité 1-3)
            val importanceScore = (priorite / 3.0) * 100.0

            // 3. Score d'Effort (Temps estimé)
            // On privilégie légèrement les tâches plus longues pour ne pas les repousser
            val effortScore = Math.min(100.0, (dureeEstimee / 120.0) * 100.0)

            return (urgenceScore * 0.6) + (importanceScore * 0.3) + (effortScore * 0.1)
        }
}
