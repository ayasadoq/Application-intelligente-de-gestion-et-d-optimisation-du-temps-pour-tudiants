package com.etudiant.timemaster.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.etudiant.timemaster.data.TacheDatabase
import com.etudiant.timemaster.data.TacheRepository
import com.etudiant.timemaster.model.Tache
import kotlinx.coroutines.launch

class TacheViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TacheRepository
    val allTaches: LiveData<List<Tache>>

    init {
        val tacheDao = TacheDatabase.getDatabase(application).tacheDao()
        repository = TacheRepository(tacheDao)
        allTaches = repository.allTaches
    }

    fun insert(tache: Tache) = viewModelScope.launch {
        repository.insert(tache)
    }

    fun update(tache: Tache) = viewModelScope.launch {
        repository.update(tache)
    }

    fun delete(tache: Tache) = viewModelScope.launch {
        repository.delete(tache)
    }

    fun getTachesByDate(date: Long): LiveData<List<Tache>> {
        return repository.getTachesByDate(date)
    }
}
