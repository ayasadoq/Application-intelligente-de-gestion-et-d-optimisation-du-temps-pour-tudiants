package com.etudiant.timemaster

import android.os.Handler
import android.os.Looper
import com.etudiant.timemaster.model.Tache

object ChronometreHelper {
    private var activeTacheId: Int? = null
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var onUpdateCallback: ((Tache) -> Unit)? = null
    private var currentTache: Tache? = null

    fun toggleChrono(tache: Tache, onUpdate: (Tache) -> Unit) {
        if (activeTacheId == tache.id) {
            stopChrono()
        } else {
            startChrono(tache, onUpdate)
        }
    }

    private fun startChrono(tache: Tache, onUpdate: (Tache) -> Unit) {
        stopChrono()
        activeTacheId = tache.id
        currentTache = tache
        onUpdateCallback = onUpdate
        
        runnable = object : Runnable {
            override fun run() {
                currentTache?.let {
                    it.tempsPasse++
                    onUpdateCallback?.invoke(it)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable!!)
    }

    fun stopChrono() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = null
        activeTacheId = null
        currentTache = null
        onUpdateCallback = null
    }

    fun isRunning(tacheId: Int): Boolean = activeTacheId == tacheId
}
