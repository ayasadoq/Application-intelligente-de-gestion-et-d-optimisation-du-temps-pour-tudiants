package com.studyflowai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.studyflowai.agent.GuideAgent
import com.studyflowai.ui.adapter.ChatMessage
import com.studyflowai.utils.AiPlannerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.lifecycle.asLiveData
import com.studyflowai.data.entity.ChatMessageEntity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.delay

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = com.studyflowai.data.database.AppDatabase.getDatabase(application)
    private val chatDao = database.chatMessageDao()

    val messages: LiveData<List<ChatMessage>> = chatDao.getActiveMessages("default_user")
        .map { entities ->
            entities.map { ChatMessage(it.text, it.isUser) }
        }.asLiveData()

    val archivedMessages: LiveData<List<ChatMessage>> = chatDao.getArchivedMessages("default_user")
        .map { entities ->
            entities.map { ChatMessage(it.text, it.isUser) }
        }.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val guideAgent = GuideAgent(application)
    private val plannerManager = AiPlannerManager("default_user")

    init {
        viewModelScope.launch {
            val existing = chatDao.getActiveMessages("default_user").first()
            if (existing.isEmpty()) {
                chatDao.insertMessage(ChatMessageEntity(
                    text = "Bonjour ! Je suis ton coach StudyFlow. Comment puis-je t'aider aujourd'hui ?",
                    isUser = false
                ))
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value == true) return

        viewModelScope.launch {
            // 1. Sauvegarder immédiatement le message utilisateur
            chatDao.insertMessage(ChatMessageEntity(text = text, isUser = true))
            
            _isLoading.value = true

            try {
                val currentHistory = withContext(Dispatchers.IO) {
                    val allActive = chatDao.getActiveMessages("default_user").first()
                    // On exclut le tout dernier message (celui qu'on vient d'insérer) pour que GuideAgent l'ajoute lui-même
                    allActive.dropLast(1).map { 
                        com.studyflowai.api.GroqMessage(role = if (it.isUser) "user" else "assistant", content = it.text) 
                    }
                }

                val tasksContext = withContext(Dispatchers.IO) {
                    database.taskDao().getAllTasksSync("default_user")
                        .filter { !it.isCompleted }
                        .joinToString("\n") { "- ${it.title}" }
                }

                val rawResponse = withContext(Dispatchers.IO) {
                    guideAgent.chat(text, currentHistory, tasksContext)
                }
                
                // 2. Nettoyage et découpage des messages
                val cleanMessage = plannerManager.getDisplayableText(rawResponse)
                
                // 3. Sauvegarder la réponse de l'IA (en plusieurs bulles avec un petit délai pour l'effet interactif)
                val messageParts = cleanMessage.split("[SPLIT]")
                messageParts.forEachIndexed { index, part ->
                    val trimmedPart = part.trim()
                    if (trimmedPart.isNotEmpty()) {
                        if (index > 0) delay(800) // Délai entre les bulles
                        chatDao.insertMessage(ChatMessageEntity(text = trimmedPart, isUser = false))
                    }
                }

                // 4. Traitement du planning si présent
                withContext(Dispatchers.IO) {
                    val aiResponse = plannerManager.parseAiResponse(rawResponse)
                    if (aiResponse.tasks.isNotEmpty()) {
                        val entities = plannerManager.convertAiTasksToEntities(aiResponse)
                        entities.forEach { database.taskDao().insertTask(it) }
                    }
                }

            } catch (e: Exception) {
                chatDao.insertMessage(ChatMessageEntity(
                    text = "Désolé, j'ai une petite erreur de connexion. On réessaie ?",
                    isUser = false
                ))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            // On archive au lieu de supprimer
            chatDao.archiveActiveChat("default_user")
            
            // Rajouter le message de bienvenue après avoir archivé
            chatDao.insertMessage(ChatMessageEntity(
                text = "Chat réinitialisé. Tes anciens messages sont dans l'historique. Comment puis-je t'aider à nouveau ?",
                isUser = false
            ))
        }
    }
}
