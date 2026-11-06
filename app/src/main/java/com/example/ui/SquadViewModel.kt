package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.RealtimeStatus
import com.example.data.model.SquadActivity
import com.example.data.model.SquadMember
import com.example.data.model.SupabaseConfig
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.repository.SquadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SquadTab {
    SQUAD_HUB,
    MY_TASKS,
    LEADERBOARD,
    SETTINGS
}

data class SquadUiState(
    val tasks: List<TaskItem> = emptyList(),
    val members: List<SquadMember> = emptyList(),
    val activities: List<SquadActivity> = emptyList(),
    val realtimeStatus: RealtimeStatus = RealtimeStatus.DISCONNECTED,
    val supabaseConfig: SupabaseConfig = SupabaseConfig(),
    val isSimulatorEnabled: Boolean = true,
    val currentUserId: String = "user_me",
    val selectedTab: SquadTab = SquadTab.SQUAD_HUB,
    val selectedFriend: SquadMember? = null,
    val showAddTaskDialog: Boolean = false,
    val showSupabaseDialog: Boolean = false,
    val showNudgeDialogFor: SquadMember? = null,
    val celebrationMessage: String? = null,
    val testConnectionResult: String? = null,
    val isTestingConnection: Boolean = false
)

class SquadViewModel(
    private val repository: SquadRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(SquadTab.SQUAD_HUB)
    private val _selectedFriend = MutableStateFlow<SquadMember?>(null)
    private val _showAddTaskDialog = MutableStateFlow(false)
    private val _showSupabaseDialog = MutableStateFlow(false)
    private val _showNudgeDialogFor = MutableStateFlow<SquadMember?>(null)
    private val _celebrationMessage = MutableStateFlow<String?>(null)
    private val _testConnectionResult = MutableStateFlow<String?>(null)
    private val _isTestingConnection = MutableStateFlow(false)

    val uiState: StateFlow<SquadUiState> = combine(
        repository.tasksFlow,
        repository.membersFlow,
        repository.activitiesFlow,
        repository.realtimeStatus,
        repository.supabaseConfigFlow,
        repository.simulatorEnabledFlow,
        _selectedTab,
        _selectedFriend,
        _showAddTaskDialog,
        _showSupabaseDialog,
        _showNudgeDialogFor,
        _celebrationMessage,
        _testConnectionResult,
        _isTestingConnection
    ) { params ->
        val tasks = params[0] as List<TaskItem>
        val members = params[1] as List<SquadMember>
        val activities = params[2] as List<SquadActivity>
        val status = params[3] as RealtimeStatus
        val config = params[4] as SupabaseConfig
        val simEnabled = params[5] as Boolean
        val tab = params[6] as SquadTab
        val friend = params[7] as? SquadMember
        val showAdd = params[8] as Boolean
        val showSupa = params[9] as Boolean
        val nudgeFor = params[10] as? SquadMember
        val celeb = params[11] as? String
        val testRes = params[12] as? String
        val isTesting = params[13] as Boolean

        SquadUiState(
            tasks = tasks,
            members = members,
            activities = activities,
            realtimeStatus = status,
            supabaseConfig = config,
            isSimulatorEnabled = simEnabled,
            currentUserId = "user_me",
            selectedTab = tab,
            selectedFriend = friend,
            showAddTaskDialog = showAdd,
            showSupabaseDialog = showSupa,
            showNudgeDialogFor = nudgeFor,
            celebrationMessage = celeb,
            testConnectionResult = testRes,
            isTestingConnection = isTesting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SquadUiState()
    )

    fun setTab(tab: SquadTab) {
        _selectedTab.value = tab
    }

    fun selectFriend(member: SquadMember?) {
        _selectedFriend.value = member
    }

    fun openAddTaskDialog() {
        _showAddTaskDialog.value = true
    }

    fun dismissAddTaskDialog() {
        _showAddTaskDialog.value = false
    }

    fun openSupabaseDialog() {
        _showSupabaseDialog.value = true
        _testConnectionResult.value = null
    }

    fun dismissSupabaseDialog() {
        _showSupabaseDialog.value = false
    }

    fun openNudgeDialog(member: SquadMember) {
        _showNudgeDialogFor.value = member
    }

    fun dismissNudgeDialog() {
        _showNudgeDialogFor.value = null
    }

    fun dismissCelebration() {
        _celebrationMessage.value = null
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            repository.toggleTaskCompletion(task)
            if (willBeCompleted && task.userId == "user_me") {
                _celebrationMessage.value = "Great job! Task checked off & synced to squad! ⚡"
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDate: String,
        isPact: Boolean
    ) {
        viewModelScope.launch {
            repository.addTask(title, description, priority, category, dueDate, isPact)
            _showAddTaskDialog.value = false
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun sendNudge(member: SquadMember, message: String) {
        viewModelScope.launch {
            repository.sendNudge(member, message)
            _showNudgeDialogFor.value = null
            _celebrationMessage.value = "Accountability nudge sent to ${member.name}! ⚡"
        }
    }

    fun cheerTask(task: TaskItem) {
        viewModelScope.launch {
            repository.cheerTask(task)
            _celebrationMessage.value = "Cheered ${task.userName}'s task! 👏"
        }
    }

    fun saveSupabaseSettings(url: String, anonKey: String, channel: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSupabaseSettings(url, anonKey, channel, enabled)
            _showSupabaseDialog.value = false
        }
    }

    fun toggleSimulator(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSimulatorEnabled(enabled)
        }
    }

    fun testSupabaseConnection(url: String, anonKey: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testConnectionResult.value = null
            val result = repository.testSupabaseConnection(url, anonKey)
            _isTestingConnection.value = false
            _testConnectionResult.value = if (result.isSuccess) {
                "✅ Connected! Supabase reachable. Realtime subscriptions ready."
            } else {
                "❌ Connection failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}

class SquadViewModelFactory(
    private val repository: SquadRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SquadViewModel(repository) as T
    }
}
