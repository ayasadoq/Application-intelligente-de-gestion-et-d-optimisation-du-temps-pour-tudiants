package com.studyflowai.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.studyflowai.data.database.AppDatabase
import com.studyflowai.data.entity.Achievement
import com.studyflowai.data.entity.Task
import com.studyflowai.data.entity.UserStats
import com.studyflowai.data.repository.TaskRepository
import com.studyflowai.utils.AiPlannerManager
import com.studyflowai.utils.SecurityManager
import com.studyflowai.api.GroqService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository by lazy {
        val database = AppDatabase.getDatabase(getApplication())
        TaskRepository(
            database.taskDao(),
            database.userStatsDao(),
            database.achievementDao(),
            database.extracurricularDao()
        )
    }

    private val userId = MutableLiveData<String>().apply { value = "default_user" }

    val allTasks: LiveData<List<Task>> by lazy {
        userId.switchMap { id ->
            repository.getAllTasks(id).asLiveData()
        }
    }

    val userStats: LiveData<UserStats?> by lazy { repository.userStats.asLiveData() }
    val allAchievements: LiveData<List<Achievement>> by lazy { repository.allAchievements }

    // --- AI SERVICES (LAZY) ---
    private val aiPlannerManager by lazy { AiPlannerManager("default_user") }
    private val groqService by lazy { GroqService(getApplication()) }

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse
    val planningAiSuggestion: LiveData<String> = _aiResponse
    
    private val _healthScore = MutableLiveData<Int>()
    val healthScore: LiveData<Int> = _healthScore

    private val _aiError = MutableLiveData<String>()
    val aiError: LiveData<String> = _aiError
    private val _isAiLoading = MutableLiveData<Boolean>()
    val isAiLoading: LiveData<Boolean> = _isAiLoading

    private val _showClarificationDialog = MutableLiveData<Boolean>()
    val showClarificationDialog: LiveData<Boolean> = _showClarificationDialog

    private var lastVagueInput: String? = null

    init {
        // Ne rien faire de lourd ici
    }

    fun startDataInitialization() {
        seedDefaultAchievements()
    }

    private fun seedDefaultAchievements() = viewModelScope.launch(Dispatchers.IO) {
        // On vérifie si on doit peupler les trophées
        val currentAchievements = repository.allAchievementsSync()
        if (currentAchievements.isEmpty()) {
            val defaults = listOf(
                Achievement(name = "Bienvenue", description = "Première tâche créée", iconResId = android.R.drawable.ic_dialog_info, requirementType = "TASK_COUNT", requirementValue = 1),
                Achievement(name = "Productif", description = "5 tâches complétées", iconResId = android.R.drawable.ic_menu_edit, requirementType = "TASK_COUNT", requirementValue = 5),
                Achievement(name = "Maître Focus", description = "10 sessions Pomodoro", iconResId = android.R.drawable.ic_lock_idle_alarm, requirementType = "POMODORO_COUNT", requirementValue = 10),
                Achievement(name = "Persévérant", description = "3 jours de suite", iconResId = android.R.drawable.star_on, requirementType = "STREAK", requirementValue = 3),
                Achievement(name = "Légende", description = "Niveau 5 atteint", iconResId = android.R.drawable.btn_star_big_on, requirementType = "LEVEL", requirementValue = 5)
            )
            repository.insertAchievements(defaults)
        }
    }

    // --- Data Stream for Smart Planner ---
    val smartPlannedTasks: LiveData<List<Task>> = allTasks.switchMap { tasks ->
        liveData(Dispatchers.Default) {
            val sorted = tasks.filter { !it.isCompleted }
                .sortedByDescending { it.calculateScore(60) }
            emit(sorted)
        }
    }

    // --- Data Stream for Dashboard ---
    val dashboardData: LiveData<DashboardData> = allTasks.switchMap { tasks ->
        liveData(Dispatchers.Default) {
            val total = tasks.size
            val completed = tasks.count { it.isCompleted }
            val overdue = tasks.count { it.deadline < System.currentTimeMillis() && !it.isCompleted }
            val progress = if (total > 0) (completed * 100) / total else 0

            emit(DashboardData(
                progressPercent = progress,
                completedCount = completed,
                overdueCount = overdue,
                totalTasks = total,
                pendingTasksCount = tasks.count { !it.isCompleted }
            ))
        }
    }

    // --- Data Stream for Analytics ---
    val analyticsData: LiveData<AnalyticsData> = allTasks.switchMap { tasks ->
        liveData(Dispatchers.Default) {
            val total = tasks.size
            val completed = tasks.count { it.isCompleted }

            // 1. Time Comparison
            val totalEstimated = tasks.sumOf { it.estimatedTime }.toFloat()
            val totalActual = tasks.sumOf { it.realTime }.toFloat()

            // 2. Priority Distribution
            val priorityMap = tasks.groupBy { it.priority }
                .mapValues { it.value.size.toFloat() }

            // 3. Weekly Stats
            val weeklyCompletion = mutableMapOf<Int, Int>()
            val calendar = Calendar.getInstance()
            tasks.filter { it.isCompleted }.forEach { task ->
                calendar.timeInMillis = task.createdAt
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                weeklyCompletion[day] = (weeklyCompletion[day] ?: 0) + 1
            }

            emit(AnalyticsData(
                completionRate = if (total > 0) (completed.toFloat() / total) * 100 else 0f,
                overdueRate = if (total > 0) (tasks.count { it.deadline < System.currentTimeMillis() && !it.isCompleted }.toFloat() / total) * 100 else 0f,
                totalEstimatedTime = totalEstimated,
                totalActualTime = totalActual,
                priorityDistribution = priorityMap,
                weeklyCompletion = weeklyCompletion
            ))
        }
    }

    // --- CRUD OPERATIONS ---
    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(task)
    }

    fun toggleTaskStatus(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        val isNowCompleted = !task.isCompleted
        val newTask = task.copy(isCompleted = isNowCompleted)
        repository.update(newTask)

        if (isNowCompleted) {
            addXp(100) // Reward for completion
        }
    }

    fun updateTaskTime(task: Task, newTime: Int) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTask = task.copy(realTime = newTime)
        repository.update(updatedTask)
    }

    fun seedTestTasks() = viewModelScope.launch(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        val currentUserId = userId.value ?: "default_user"

        val testTasks = listOf(
            Task(userId = currentUserId, title = "Audit Sécurité Réseau", description = "Analyser les logs du pare-feu", deadline = now + dayMillis, priority = 3, estimatedTime = 120),
            Task(userId = currentUserId, title = "Rédaction Rapport Stage", description = "Finir la partie technique", deadline = now + 2 * dayMillis, priority = 2, estimatedTime = 180),
            Task(userId = currentUserId, title = "Préparation Présentation AI", description = "Faire les slides sur les Transformers", deadline = now + 12 * 60 * 60 * 1000, priority = 3, estimatedTime = 60),
            Task(userId = currentUserId, title = "Révision Mathématiques", description = "Calcul intégral et probabilités", deadline = now + 3 * dayMillis, priority = 1, estimatedTime = 90)
        )
        testTasks.forEach { repository.insert(it) }
    }

    fun addXp(amount: Int, isTaskCompletion: Boolean = true, pomodoroCompleted: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        repository.addXpAndTask(amount, isTaskCompletion, pomodoroCompleted)

        val stats = repository.getUserStatsSync()
        val achievements = repository.allAchievementsSync()

        if (stats != null) {
            achievements.forEach { achievement ->
                if (!achievement.isUnlocked) {
                    val shouldUnlock = when (achievement.requirementType) {
                        "TASK_COUNT" -> stats.totalTasksCompleted >= achievement.requirementValue
                        "POMODORO_COUNT" -> stats.totalPomodoroSessions >= achievement.requirementValue
                        "STREAK" -> stats.currentStreak >= achievement.requirementValue
                        "LEVEL" -> stats.level >= achievement.requirementValue
                        else -> false
                    }

                    if (shouldUnlock) {
                        val updated = achievement.copy(isUnlocked = true, unlockDate = System.currentTimeMillis())
                        repository.updateAchievement(updated)
                    }
                }
            }
        }
    }

    // --- AI METHODS ---
    fun loadTasksForDate(timestamp: Long) {
        // Logique pour charger ou filtrer les tâches d'une journée spécifique
        // Pour l'instant, on peut simplement mettre à jour une LiveData si besoin
    }

    fun fetchAiInsight(progress: Int) {
        _isAiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val prompt = """
                Tu es un coach de productivité expert pour étudiant.
                Ma progression est de $progress% aujourd'hui.
                Donne moi un conseil court (max 2 phrases), motivant et concret.
                Sois direct et encourageant.
            """.trimIndent()

            try {
                val result = groqService.generateContent(prompt)
                result.fold(
                    onSuccess = { rawAdvice ->
                        val cleanAdvice = if (rawAdvice.trim().startsWith("{")) {
                            aiPlannerManager.extractMessage(rawAdvice)
                        } else {
                            rawAdvice
                        }
                        _aiResponse.postValue(cleanAdvice)
                    },
                    onFailure = { e ->
                        _aiError.postValue("Erreur AI: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                _aiError.postValue("Erreur AI: ${e.message}")
            } finally {
                _isAiLoading.postValue(false)
            }
        }
    }

    fun generateSmartSchedule(userInput: String, extraContext: String = "") {
        if (userInput.length < 5 && !isInputDetailed(userInput) && extraContext.isEmpty()) {
            lastVagueInput = userInput
            _showClarificationDialog.value = true
            return
        }

        // Tactical handling for "Quick-Action Hub" inputs
        val lowEnergy = userInput.contains("fatigué", ignoreCase = true) || userInput.contains("épuisé", ignoreCase = true)
        val normalEnergy = userInput.contains("normal", ignoreCase = true) || userInput.contains("bien", ignoreCase = true)
        val highEnergy = userInput.contains("boosté", ignoreCase = true) || userInput.contains("énergie", ignoreCase = true)
        
        if (lowEnergy || normalEnergy || highEnergy) {
            val tasks = allTasks.value?.filter { !it.isCompleted } ?: emptyList()
            if (tasks.isNotEmpty()) {
                optimizeByEnergy(userInput, tasks)
                return
            }
        }

        // Special handling for SOS / Focus Express
        val isEmergency = userInput.contains("SOS", ignoreCase = true) || userInput.contains("retard", ignoreCase = true)
        val isFocusExpress = userInput.contains("Focus Express", ignoreCase = true)

        _isAiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val extras = repository.getExtracurricularsSync().joinToString {
                "${it.name} le jour ${it.dayOfWeek} à ${it.startHour}h"
            }

            val user = FirebaseAuth.getInstance().currentUser
            val name = user?.displayName ?: "Étudiant"
            val stats = "Level ${userStats.value?.level}, XP ${userStats.value?.xp}"

            try {
                val fullInput = if (extraContext.isNotEmpty()) "$userInput. Détails additionnels : $extraContext" else userInput

                val systemPrompt = """
                    Tu es l'AI Coach de StudyFlow, un expert en productivité étudiant.
                    CONTEXTE : $name ($stats). ACTIVITÉS : $extras
                    
                    MODE : ${if (isEmergency) "URGENCE (Sauve la soirée, priorise le critique)" else if (isFocusExpress) "FOCUS (Session courte 15-25min)" else "NORMAL"}
                    
                    Réponds UNIQUEMENT en JSON strict :
                    {
                      "needsClarification": boolean,
                      "tasks": [
                        { "title": "string", "category": "string", "durationMin": integer, "priority": "high|medium|low" }
                      ],
                      "coach_message": "string (ton explication humaine et motivante)"
                    }
                """.trimIndent()

                val result = groqService.generateContent("$systemPrompt\n\nUTILISATEUR: $fullInput")

                result.fold(
                    onSuccess = { jsonResponse ->
                        withContext(Dispatchers.Default) {
                            val aiPlan = aiPlannerManager.parseAiResponse(jsonResponse)
                            aiPlan?.let { plan ->
                                if (plan.needsClarification == true) {
                                    val questions = plan.clarificationQuestions?.joinToString(" ")
                                    _aiResponse.postValue("J'ai besoin de précisions : $questions")
                                } else {
                                    val message = plan.motivationalMessage ?: plan.coachMessage ?: "Planning généré avec succès !"
                                    _aiResponse.postValue(message)
                                    val newTasks = aiPlannerManager.convertAiTasksToEntities(plan)
                                    newTasks.forEach { task ->
                                        repository.insert(task)
                                    }
                                }
                            } ?: run {
                                _aiError.postValue("Erreur de format IA")
                            }
                        }
                    },
                    onFailure = { e ->
                        _aiError.postValue("Erreur de connexion IA: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                _aiError.postValue("AI Error: ${e.message}")
            } finally {
                _isAiLoading.postValue(false)
            }
        }
    }

    private fun isInputDetailed(input: String): Boolean {
        val keywords = listOf("dans", "pour", "avant", "le", "jour")
        return keywords.any { input.contains(it, ignoreCase = true) }
    }

    fun onClarificationProvided(details: String) {
        lastVagueInput?.let {
            generateSmartSchedule(it, details)
        }
        _showClarificationDialog.value = false
    }

    fun getTasksByDate(date: Long): LiveData<List<Task>> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        return allTasks.switchMap { tasks ->
            liveData(Dispatchers.Default) {
                val filtered = tasks.filter { it.deadline in startOfDay..endOfDay }
                    .sortedByDescending { it.calculateScore(60) }
                emit(filtered)
            }
        }
    }

    fun getPlanningSuggestion(tasks: List<Task>, energyLevel: String = "Normal") {
        if (tasks.isEmpty()) {
            _aiResponse.postValue("Vous n'avez pas de tâches prévues pour ce jour. C'est le moment idéal pour planifier !")
            return
        }

        _isAiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val tasksSummary = tasks.joinToString("\n") { "- ${it.title} (Priorité: ${it.priority}, Durée: ${it.estimatedTime}min)" }
            val prompt = """
                Tu es un expert en productivité. Analyse ces tâches pour un étudiant qui se sent "$energyLevel" :
                $tasksSummary
                
                1. Calcule un "Score de Santé de la Journée" sur 100 (100 = parfait équilibre, 20 = burnout/surcharge).
                2. Donne un conseil ultra-court et humain.
                
                Réponds UNIQUEMENT au format JSON : {"score": 85, "message": "Ta journée est bien équilibrée, fonce !", "hint": "Peut-être réduire la durée de..."}
            """.trimIndent()

            try {
                val result = groqService.generateContent(prompt)
                result.fold(
                    onSuccess = { response ->
                        try {
                            val cleanJson = aiPlannerManager.sanitizeJson(response)
                            val jsonMatch = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL).find(cleanJson)
                            val finalJson = jsonMatch?.value ?: cleanJson
                            
                            val json = android.util.JsonReader(java.io.StringReader(finalJson))
                            var score = 0
                            var message = ""
                            var hint = ""
                            json.beginObject()
                            while (json.hasNext()) {
                                when (json.nextName()) {
                                    "score" -> score = json.nextInt()
                                    "message" -> message = json.nextString()
                                    "hint" -> hint = json.nextString()
                                    else -> json.skipValue()
                                }
                            }
                            json.endObject()
                            _healthScore.postValue(score)
                            
                            val fullMessage = if (hint.isNotEmpty()) "$message\n\n💡 Conseil : $hint" else message
                            _aiResponse.postValue(fullMessage)
                        } catch (e: Exception) {
                            _aiResponse.postValue(aiPlannerManager.extractMessage(response))
                        }
                    },
                    onFailure = { _aiError.postValue("Erreur AI: ${it.message}") }
                )
            } catch (e: Exception) {
                _aiError.postValue("Erreur: ${e.message}")
            } finally {
                _isAiLoading.postValue(false)
            }
        }
    }

    fun optimizeByEnergy(energyLevel: String, tasks: List<Task>) {
        if (tasks.isEmpty()) return

        _isAiLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val tasksSummary = tasks.joinToString("\n") { "- ID ${it.id}: ${it.title} (${it.estimatedTime}min)" }
            val prompt = """
                Tu es un expert en productivité. L'utilisateur se sent : $energyLevel.
                Voici ses tâches pour aujourd'hui :
                $tasksSummary
                
                Propose des modifications de durée (newDurationMin) réalistes pour son état.
                Si "fatigué", réduis les durées. Si "boosté", on peut les augmenter ou les laisser.
                
                Réponds UNIQUEMENT avec ce format JSON :
                {
                  "modifications": [
                    { "id": Integer, "newDurationMin": Int, "reason": "string" }
                  ],
                  "coach_message": "Courte explication ici"
                }
            """.trimIndent()

            try {
                val result = groqService.generateContent(prompt)
                result.fold(
                    onSuccess = { jsonResponse ->
                        val plan = aiPlannerManager.parseAiResponse(jsonResponse)
                        plan.modifications.forEach { mod ->
                            tasks.find { it.id == mod.id }?.let { task ->
                                val updatedTask = task.copy(estimatedTime = mod.newDurationMin)
                                repository.update(updatedTask)
                            }
                        }
                        _aiResponse.postValue(plan.coachMessage.ifBlank { "Planning optimisé pour ton état : $energyLevel" })
                    },
                    onFailure = { _aiError.postValue("Erreur AI: ${it.message}") }
                )
            } catch (e: Exception) {
                _aiError.postValue("Erreur: ${e.message}")
            } finally {
                _isAiLoading.postValue(false)
            }
        }
    }
}

data class DashboardData(
    val progressPercent: Int,
    val completedCount: Int,
    val overdueCount: Int,
    val totalTasks: Int,
    val pendingTasksCount: Int
)

data class AnalyticsData(
    val completionRate: Float,
    val overdueRate: Float,
    val totalEstimatedTime: Float,
    val totalActualTime: Float,
    val priorityDistribution: Map<Int, Float>,
    val weeklyCompletion: Map<Int, Int>
)
