package com.etudiant.timemaster.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titre: String,
    val description: String,
    val dateLimite: Long,
    val priorite: Int, // 1: Basse, 2: Moyenne, 3: Haute
    val importance: Int, // 1: Basse, 2: Moyenne, 3: Haute
    val dureeEstimee: Long, // en minutes
    var tempsPasse: Long = 0, // en secondes
    var estTerminee: Boolean = false,
    var datePlanifiee: Long = 0
) : Serializable {
    val scorePriorite: Int
        get() = priorite * importance
}
