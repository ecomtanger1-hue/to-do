package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RealtimeStatus
import com.example.ui.SquadTab
import com.example.ui.SquadViewModel
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.FindFriendsDialog
import com.example.ui.components.FriendDetailDialog
import com.example.ui.components.NudgeDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SupabaseConfigDialog
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.OnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: SquadViewModel
) {
    val state by viewModel.uiState.collectAsState()

    // 1. Account Onboarding Gate: User creates an account first!
    if (!state.isLoggedIn) {
        AuthScreen(
            onAccountCreated = { name, handle, email, avatar, bio ->
                viewModel.createAccount(name, handle, email, avatar, bio)
            }
        )
        return
    }

    // 2. Minimalist Main App Experience
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald400.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = state.userAvatar, fontSize = 16.sp)
                            }
                        }
                        Text(
                            text = "SquadDo",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Supabase Realtime Dot Indicator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.openSupabaseDialog() }
                            .padding(end = 4.dp)
                            .testTag("top_bar_supabase_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.realtimeStatus == RealtimeStatus.CONNECTED) OnlineGreen else Cyan400
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (state.realtimeStatus == RealtimeStatus.CONNECTED) "Live" else "Sync",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Find Friends Quick Action
                    IconButton(
                        onClick = { viewModel.openFindFriendsDialog() },
                        modifier = Modifier.testTag("top_bar_find_friends")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Find Friends",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Quick Action
                    IconButton(
                        onClick = { viewModel.openSettingsDialog() },
                        modifier = Modifier.testTag("top_bar_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            val isTodaySelected = state.selectedTab == SquadTab.TODAY ||
                    state.selectedTab == SquadTab.SQUAD_HUB ||
                    state.selectedTab == SquadTab.MY_TASKS
            val isSquadSelected = state.selectedTab == SquadTab.SQUAD ||
                    state.selectedTab == SquadTab.LEADERBOARD

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = isTodaySelected,
                    onClick = { viewModel.setTab(SquadTab.TODAY) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Today") },
                    label = { Text("Today", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_today")
                )
                NavigationBarItem(
                    selected = isSquadSelected,
                    onClick = { viewModel.setTab(SquadTab.SQUAD) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Squad") },
                    label = { Text("Squad", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_squad")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                SquadTab.TODAY, SquadTab.SQUAD_HUB, SquadTab.MY_TASKS -> {
                    SquadHubScreen(
                        state = state,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onCheerTask = { viewModel.cheerTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onNudgeMember = { viewModel.openNudgeDialog(it) },
                        onSelectFriend = { viewModel.selectFriend(it) },
                        onOpenAddTask = { viewModel.openAddTaskDialog() },
                        onOpenSupabaseConfig = { viewModel.openSupabaseDialog() },
                        onOpenFindFriends = { viewModel.openFindFriendsDialog() },
                        onAcceptInvite = { viewModel.acceptInvitation(it) },
                        onQuickAdd = { title, pact -> viewModel.quickAddTask(title, pact) }
                    )
                }
                SquadTab.SQUAD, SquadTab.LEADERBOARD -> {
                    LeaderboardScreen(
                        state = state,
                        onOpenFindFriends = { viewModel.openFindFriendsDialog() }
                    )
                }
                SquadTab.SETTINGS -> {
                    SettingsScreen(
                        state = state,
                        onOpenSupabaseConfig = { viewModel.openSupabaseDialog() },
                        onToggleSimulator = { viewModel.toggleSimulator(it) },
                        onLogOut = { viewModel.logOut() }
                    )
                }
            }

            // Minimalist Celebration Notification Banner
            AnimatedVisibility(
                visible = state.celebrationMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald400,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = state.celebrationMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF022C22),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissCelebration() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF022C22), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (state.showSettingsDialog) {
        SettingsDialog(
            state = state,
            onDismiss = { viewModel.dismissSettingsDialog() },
            onOpenSupabaseConfig = {
                viewModel.dismissSettingsDialog()
                viewModel.openSupabaseDialog()
            },
            onToggleSimulator = { viewModel.toggleSimulator(it) },
            onLogOut = {
                viewModel.dismissSettingsDialog()
                viewModel.logOut()
            }
        )
    }

    if (state.showAddTaskDialog) {
        AddEditTaskDialog(
            onDismiss = { viewModel.dismissAddTaskDialog() },
            onSave = { title, desc, pri, cat, due, pact ->
                viewModel.addTask(title, desc, pri, cat, due, pact)
            }
        )
    }

    if (state.showFindFriendsDialog) {
        FindFriendsDialog(
            userHandle = state.userHandle,
            squadCode = "SQUAD-7788",
            searchResults = state.searchResults,
            isSearching = state.isSearching,
            pendingInvitations = state.invitations,
            onSearch = { viewModel.searchUsers(it) },
            onInviteFriend = { viewModel.inviteFriend(it) },
            onAcceptInvite = { viewModel.acceptInvitation(it) },
            onJoinSquadCode = { viewModel.joinSquadByCode(it) },
            onDismiss = { viewModel.dismissFindFriendsDialog() }
        )
    }

    if (state.showSupabaseDialog) {
        SupabaseConfigDialog(
            initialConfig = state.supabaseConfig,
            testResult = state.testConnectionResult,
            isTesting = state.isTestingConnection,
            onTestConnection = { url, key -> viewModel.testSupabaseConnection(url, key) },
            onSave = { url, key, chan, en -> viewModel.saveSupabaseSettings(url, key, chan, en) },
            onDismiss = { viewModel.dismissSupabaseDialog() }
        )
    }

    state.showNudgeDialogFor?.let { member ->
        NudgeDialog(
            member = member,
            onDismiss = { viewModel.dismissNudgeDialog() },
            onSend = { message -> viewModel.sendNudge(member, message) }
        )
    }

    state.selectedFriend?.let { friend ->
        val friendTasks = state.tasks.filter { it.userId == friend.id }
        FriendDetailDialog(
            member = friend,
            memberTasks = friendTasks,
            isCurrentUser = friend.id == state.currentUserId,
            onCheerTask = { viewModel.cheerTask(it) },
            onNudge = { viewModel.openNudgeDialog(it) },
            onDismiss = { viewModel.selectFriend(null) }
        )
    }
}
