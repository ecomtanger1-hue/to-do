package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
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
import com.example.ui.components.FriendDetailDialog
import com.example.ui.components.NudgeDialog
import com.example.ui.components.SupabaseConfigDialog
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RoseNudge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: SquadViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald400.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "⚡", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SquadDo",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Supabase Realtime Quick Indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (state.realtimeStatus == RealtimeStatus.CONNECTED) OnlineGreen else Cyan400
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openSupabaseDialog() }
                            .padding(end = 8.dp)
                            .testTag("top_bar_supabase_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
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
                                text = if (state.realtimeStatus == RealtimeStatus.CONNECTED) "Supabase Live" else "Realtime",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == SquadTab.SQUAD_HUB,
                    onClick = { viewModel.setTab(SquadTab.SQUAD_HUB) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Squad Hub") },
                    label = { Text("Squad Hub", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_squad_hub")
                )
                NavigationBarItem(
                    selected = state.selectedTab == SquadTab.MY_TASKS,
                    onClick = { viewModel.setTab(SquadTab.MY_TASKS) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "My Tasks") },
                    label = { Text("My Tasks", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_my_tasks")
                )
                NavigationBarItem(
                    selected = state.selectedTab == SquadTab.LEADERBOARD,
                    onClick = { viewModel.setTab(SquadTab.LEADERBOARD) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                    label = { Text("Rankings", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_leaderboard")
                )
                NavigationBarItem(
                    selected = state.selectedTab == SquadTab.SETTINGS,
                    onClick = { viewModel.setTab(SquadTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Sync & Settings", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF022C22),
                        selectedTextColor = Emerald400,
                        indicatorColor = Emerald400
                    ),
                    modifier = Modifier.testTag("tab_settings")
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
                SquadTab.SQUAD_HUB -> {
                    SquadHubScreen(
                        state = state,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onCheerTask = { viewModel.cheerTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onNudgeMember = { viewModel.openNudgeDialog(it) },
                        onSelectFriend = { viewModel.selectFriend(it) },
                        onOpenAddTask = { viewModel.openAddTaskDialog() },
                        onOpenSupabaseConfig = { viewModel.openSupabaseDialog() }
                    )
                }
                SquadTab.MY_TASKS -> {
                    MyTasksScreen(
                        state = state,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onCheerTask = { viewModel.cheerTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onOpenAddTask = { viewModel.openAddTaskDialog() }
                    )
                }
                SquadTab.LEADERBOARD -> {
                    LeaderboardScreen(state = state)
                }
                SquadTab.SETTINGS -> {
                    SettingsScreen(
                        state = state,
                        onOpenSupabaseConfig = { viewModel.openSupabaseDialog() },
                        onToggleSimulator = { viewModel.toggleSimulator(it) }
                    )
                }
            }

            // Animated Celebration Toast / Banner at the top
            AnimatedVisibility(
                visible = state.celebrationMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Emerald400,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🎉", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.celebrationMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF022C22)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissCelebration() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF022C22), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (state.showAddTaskDialog) {
        AddEditTaskDialog(
            onDismiss = { viewModel.dismissAddTaskDialog() },
            onSave = { title, desc, pri, cat, due, pact ->
                viewModel.addTask(title, desc, pri, cat, due, pact)
            }
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
