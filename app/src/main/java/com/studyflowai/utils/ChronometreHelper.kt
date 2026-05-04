package com.studyflowai.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ChronometreHelper {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var seconds = 0
    private var currentTaskId: Int? = null
    
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

    fun start(taskId: Int) {
        if (currentTaskId != taskId) {
            stop()
            currentTaskId = taskId
            seconds = 0
            _tempsEcoule.value = 0
        }
        if (!isRunning) {
            isRunning = true
            handler.post(runnable)
        }
    }

    fun stop(): Pair<Int?, Int> {
        val result = Pair(currentTaskId, seconds)
        isRunning = false
        handler.removeCallbacks(runnable)
        // Reset pour la prochaine tâche
        currentTaskId = null
        seconds = 0
        _tempsEcoule.value = 0
        return result
    }

    fun isTaskRunning(taskId: Int): Boolean {
        return isRunning && currentTaskId == taskId
    }

    fun getCurrentTaskId(): Int? = currentTaskId
}

