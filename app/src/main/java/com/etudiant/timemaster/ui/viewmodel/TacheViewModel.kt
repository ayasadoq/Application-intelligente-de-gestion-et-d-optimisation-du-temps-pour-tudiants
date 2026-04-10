package com.etudiant.timemaster.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.etudiant.timemaster.data.database.TacheDatabase
import com.etudiant.timemaster.data.entity.Tache
import com.etudiant.timemaster.data.entity.UserStats
import com.etudiant.timemaster.data.repository.TacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class TacheViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TacheRepository
    val allTaches: LiveData<List<Tache>>
    val nonTerminees: LiveData<List<Tache>>
    val userStats: LiveData<UserStats?>

    init {
        val database = TacheDatabase.getDatabase(application)
        repository = TacheRepository(database.tacheDao(), database.userStatsDao())
        allTaches = repository.allTaches.asLiveData()
        nonTerminees = repository.nonTerminees.asLiveData()
        userStats = repository.userStats.asLiveData()
    }

    fun completeTask(tache: Tache) = viewModelScope.launch(Dispatchers.IO) {
        if (!tache.estTerminee) {
            tache.estTerminee = true
            repository.update(tache)
            // Reward XP: 50 base + 10 per priority level
            val xpReward = 50 + (tache.priorite * 10)
            repository.addXp(xpReward)
        }
    }

    fun completePomodoro() = viewModelScope.launch(Dispatchers.IO) {
        repository.addXp(100) // High reward for focus
        val stats = repository.userStats.first() ?: UserStats()
        stats.pomodoroSessionsCompleted++
        // Bonus for high focus score
        if (stats.focusScore > 90) {
            stats.addXp(50)
        }
        repository.updateStats(stats)
    }

    fun penalizeFocus() = viewModelScope.launch(Dispatchers.IO) {
        val stats = repository.userStats.first() ?: UserStats()
        stats.focusScore = (stats.focusScore - 5).coerceAtLeast(0)
        repository.updateStats(stats)
    }

    fun restoreFocus() = viewModelScope.launch(Dispatchers.IO) {
        val stats = repository.userStats.first() ?: UserStats()
        stats.focusScore = (stats.focusScore + 2).coerceAtMost(100)
        repository.updateStats(stats)
    }

    fun getTachesParDate(timestamp: Long): LiveData<List<Tache>> {
        val start = timestamp - (timestamp % 86400000)
        val end = start + 86400000 - 1
        return repository.getTachesParDate(start, end).asLiveData()
    }

    fun insert(tache: Tache) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(tache)
    }

    fun update(tache: Tache) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(tache)
    }

    fun delete(tache: Tache) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(tache)
    }

    fun deleteById(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    fun updateTempsPasse(tacheId: Int, secondes: Int) = viewModelScope.launch(Dispatchers.IO) {
        val currentTache = repository.getTacheById(tacheId)
        currentTache?.let {
            it.dureeReelle += secondes
            repository.update(it)
        }
    }

    fun toggleTerminee(tache: Tache) = viewModelScope.launch(Dispatchers.IO) {
        tache.estTerminee = !tache.estTerminee
        repository.update(tache)
    }
}
