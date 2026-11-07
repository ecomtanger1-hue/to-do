package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FriendInvitation
import com.example.data.model.RealtimeStatus
import com.example.data.model.SquadActivity
import com.example.data.model.SquadMember
import com.example.data.model.SupabaseConfig
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.UserSearchResult
import com.example.data.repository.SquadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SquadTab {
    TODAY,
    SQUAD,
    SQUAD_HUB,
    MY_TASKS,
    LEADERBOARD,
    SETTINGS
}

data class SquadUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "You",
    val userHandle: String = "@pact_hero",
    val userAvatar: String = "🚀",
    val userBio: String = "Accountability Partner",
    val tasks: List<TaskItem> = emptyList(),
    val members: List<SquadMember> = emptyList(),
    val activities: List<SquadActivity> = emptyList(),
    val invitations: List<FriendInvitation> = emptyList(),
    val realtimeStatus: RealtimeStatus = RealtimeStatus.DISCONNECTED,
    val supabaseConfig: SupabaseConfig = SupabaseConfig(),
    val isSimulatorEnabled: Boolean = true,
    val currentUserId: String = "user_me",
    val selectedTab: SquadTab = SquadTab.TODAY,
    val selectedFriend: SquadMember? = null,
    val showAddTaskDialog: Boolean = false,
    val showSupabaseDialog: Boolean = false,
    val showFindFriendsDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showNudgeDialogFor: SquadMember? = null,
    val celebrationMessage: String? = null,
    val testConnectionResult: String? = null,
    val isTestingConnection: Boolean = false,
    val searchResults: List<UserSearchResult> = emptyList(),
    val isSearching: Boolean = false
)

class SquadViewModel(
    private val repository: SquadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SquadUiState())
    val uiState: StateFlow<SquadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.isLoggedInFlow.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            repository.userNameFlow.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            repository.userHandleFlow.collect { handle ->
                _uiState.update { it.copy(userHandle = handle) }
            }
        }
        viewModelScope.launch {
            repository.userAvatarFlow.collect { avatar ->
                _uiState.update { it.copy(userAvatar = avatar) }
            }
        }
        viewModelScope.launch {
            repository.userBioFlow.collect { bio ->
                _uiState.update { it.copy(userBio = bio) }
            }
        }
        viewModelScope.launch {
            repository.tasksFlow.collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
        viewModelScope.launch {
            repository.membersFlow.collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }
        viewModelScope.launch {
            repository.activitiesFlow.collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
        viewModelScope.launch {
            repository.invitationsFlow.collect { invites ->
                _uiState.update { it.copy(invitations = invites) }
            }
        }
        viewModelScope.launch {
            repository.realtimeStatus.collect { status ->
                _uiState.update { it.copy(realtimeStatus = status) }
            }
        }
        viewModelScope.launch {
            repository.supabaseConfigFlow.collect { config ->
                _uiState.update { it.copy(supabaseConfig = config) }
            }
        }
        viewModelScope.launch {
            repository.simulatorEnabledFlow.collect { simEnabled ->
                _uiState.update { it.copy(isSimulatorEnabled = simEnabled) }
            }
        }
    }

    fun createAccount(name: String, handle: String, email: String, avatar: String, bio: String) {
        viewModelScope.launch {
            repository.createAccount(name, handle, email, avatar, bio)
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userName = name,
                    userHandle = handle,
                    userAvatar = avatar,
                    userBio = bio,
                    celebrationMessage = "Welcome to SquadDo, $name! Your accountability journey begins. ⚡"
                )
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
            _uiState.update { it.copy(isLoggedIn = false) }
        }
    }

    fun setTab(tab: SquadTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFriend(member: SquadMember?) {
        _uiState.update { it.copy(selectedFriend = member) }
    }

    fun openAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }

    fun dismissAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }

    fun openSupabaseDialog() {
        _uiState.update { it.copy(showSupabaseDialog = true, testConnectionResult = null) }
    }

    fun dismissSupabaseDialog() {
        _uiState.update { it.copy(showSupabaseDialog = false) }
    }

    fun openFindFriendsDialog() {
        _uiState.update { it.copy(showFindFriendsDialog = true) }
        searchUsers("")
    }

    fun dismissFindFriendsDialog() {
        _uiState.update { it.copy(showFindFriendsDialog = false) }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = repository.searchUsers(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun inviteFriend(candidate: UserSearchResult) {
        viewModelScope.launch {
            repository.inviteFriend(candidate)
            _uiState.update { current ->
                current.copy(
                    celebrationMessage = "Accountability invite sent to ${candidate.name} (${candidate.handle})! 🤝",
                    searchResults = current.searchResults.map {
                        if (it.id == candidate.id) it.copy(isAlreadyInSquad = true) else it
                    }
                )
            }
        }
    }

    fun acceptInvitation(invitation: FriendInvitation) {
        viewModelScope.launch {
            repository.acceptInvitation(invitation)
            _uiState.update {
                it.copy(celebrationMessage = "Joined squad with ${invitation.fromUserName}! 🎉")
            }
        }
    }

    fun joinSquadByCode(code: String) {
        viewModelScope.launch {
            val success = repository.joinSquadByCode(code)
            if (success) {
                _uiState.update {
                    it.copy(
                        showFindFriendsDialog = false,
                        celebrationMessage = "Connected to squad '$code' successfully! 🔥"
                    )
                }
            }
        }
    }

    fun openNudgeDialog(member: SquadMember) {
        _uiState.update { it.copy(showNudgeDialogFor = member) }
    }

    fun dismissNudgeDialog() {
        _uiState.update { it.copy(showNudgeDialogFor = null) }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(celebrationMessage = null) }
    }

    fun openSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun dismissSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun quickAddTask(title: String, isPact: Boolean = false) {
        if (title.isBlank()) return
        addTask(
            title = title.trim(),
            description = "",
            priority = if (isPact) TaskPriority.HIGH else TaskPriority.MEDIUM,
            category = TaskCategory.WORK,
            dueDate = "Today",
            isPact = isPact
        )
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            repository.toggleTaskCompletion(task)
            if (willBeCompleted && task.userId == "user_me") {
                _uiState.update {
                    it.copy(celebrationMessage = "Great job! Task checked off & synced to squad! ⚡")
                }
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
            _uiState.update { it.copy(showAddTaskDialog = false) }
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
            _uiState.update {
                it.copy(
                    showNudgeDialogFor = null,
                    celebrationMessage = "Accountability nudge sent to ${member.name}! ⚡"
                )
            }
        }
    }

    fun cheerTask(task: TaskItem) {
        viewModelScope.launch {
            repository.cheerTask(task)
            _uiState.update {
                it.copy(celebrationMessage = "Cheered ${task.userName}'s task! 👏")
            }
        }
    }

    fun saveSupabaseSettings(url: String, anonKey: String, channel: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSupabaseSettings(url, anonKey, channel, enabled)
            _uiState.update { it.copy(showSupabaseDialog = false) }
        }
    }

    fun toggleSimulator(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSimulatorEnabled(enabled)
            _uiState.update { it.copy(isSimulatorEnabled = enabled) }
        }
    }

    fun testSupabaseConnection(url: String, anonKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, testConnectionResult = null) }
            val result = repository.testSupabaseConnection(url, anonKey)
            val msg = if (result.isSuccess) {
                "✅ Connected! Supabase reachable. Realtime subscriptions ready."
            } else {
                "❌ Connection failed: ${result.exceptionOrNull()?.message}"
            }
            _uiState.update { it.copy(isTestingConnection = false, testConnectionResult = msg) }
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
