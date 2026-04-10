package com.etudiant.timemaster.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ChronometreHelper {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var seconds = 0
    private var currentTacheId: Int? = null
    
    private val _tempsEcoule = MutableStateFlow<Int>(0)
    val tempsEcoule: StateFlow<Int> = _tempsEcoule

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                _tempsEcoule.value = seconds
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun start(tacheId: Int) {
        if (currentTacheId != tacheId) {
            stop()
            currentTacheId = tacheId
            seconds = 0
            _tempsEcoule.value = 0
        }
        if (!isRunning) {
            isRunning = true
            handler.post(runnable)
        }
    }

    fun stop(): Pair<Int?, Int> {
        val result = Pair(currentTacheId, seconds)
        isRunning = false
        handler.removeCallbacks(runnable)
        // Reset pour la prochaine tâche
        currentTacheId = null
        seconds = 0
        _tempsEcoule.value = 0
        return result
    }

    fun isTacheRunning(tacheId: Int): Boolean {
        return isRunning && currentTacheId == tacheId
    }

    fun getCurrentTacheId(): Int? = currentTacheId
}
