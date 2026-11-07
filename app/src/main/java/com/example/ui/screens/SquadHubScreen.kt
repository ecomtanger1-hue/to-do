package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendInvitation
import com.example.data.model.SquadMember
import com.example.data.model.TaskItem
import com.example.ui.SquadUiState
import com.example.ui.components.FriendAccountabilityCard
import com.example.ui.components.TaskCard
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400

@Composable
fun SquadHubScreen(
    state: SquadUiState,
    onToggleTask: (TaskItem) -> Unit,
    onCheerTask: (TaskItem) -> Unit,
    onDeleteTask: (String) -> Unit,
    onNudgeMember: (SquadMember) -> Unit,
    onSelectFriend: (SquadMember) -> Unit,
    onOpenAddTask: () -> Unit,
    onOpenSupabaseConfig: () -> Unit,
    onOpenFindFriends: () -> Unit,
    onAcceptInvite: (FriendInvitation) -> Unit,
    onQuickAdd: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val myTasks = state.tasks.filter { it.userId == state.currentUserId }
    val myCompleted = myTasks.count { it.isCompleted }
    val myTotal = myTasks.size
    val myProgress = if (myTotal > 0) myCompleted.toFloat() / myTotal else 0f

    var quickTaskText by remember { mutableStateOf("") }
    var isPactSelected by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Pending Invitation Banner (if any)
            if (state.invitations.isNotEmpty()) {
                items(state.invitations) { invite ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Cyan400.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "🤝 ${invite.fromUserName} invited you to '${invite.squadName}'",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { onAcceptInvite(invite) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF022C22))
                            }
                        }
                    }
                }
            }

            // Minimalist Progress Header
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = if (myTotal == 0) "No tasks yet" else "$myCompleted of $myTotal completed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (myProgress >= 1f && myTotal > 0) Emerald400 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { myProgress },
                        color = Emerald400,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            // Fast Inline Task Creator
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = quickTaskText,
                            onValueChange = { quickTaskText = it },
                            placeholder = { Text("Add a task...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_quick_task")
                        )

                        // Pact Toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPactSelected) AmberFlame.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPactSelected) AmberFlame else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isPactSelected = !isPactSelected }
                        ) {
                            Text(
                                text = "🔥 Pact",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPactSelected) AmberFlame else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Add Button
                        Button(
                            onClick = {
                                if (quickTaskText.isNotBlank()) {
                                    onQuickAdd(quickTaskText.trim(), isPactSelected)
                                    quickTaskText = ""
                                    isPactSelected = false
                                }
                            },
                            enabled = quickTaskText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("button_quick_add_task")
                        ) {
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF022C22))
                        }
                    }
                }
            }

            // Section: My Tasks
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "My Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (myTasks.isNotEmpty()) {
                        Text(
                            text = "${myTasks.count { !it.isCompleted }} left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (myTasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks yet. Add one above to keep yourself accountable!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(myTasks) { task ->
                    TaskCard(
                        task = task,
                        isCurrentUser = true,
                        onToggle = onToggleTask,
                        onCheer = onCheerTask,
                        onDelete = onDeleteTask
                    )
                }
            }

            // Section: Friends Today (Live Accountability)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Friends Live",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Emerald400, CircleShape)
                        )
                    }

                    Text(
                        text = "Tap friend for tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Other friends in the squad
            val friends = state.members.filter { it.id != state.currentUserId }
            if (friends.isEmpty()) {
                item {
                    Text(
                        text = "No friends joined yet. Tap 'Find Friends' to invite buddies!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(friends) { member ->
                    val memberTasks = state.tasks.filter { it.userId == member.id }
                    FriendAccountabilityCard(
                        member = member,
                        memberTasks = memberTasks,
                        isCurrentUser = false,
                        onNudge = onNudgeMember,
                        onClick = onSelectFriend
                    )
                }
            }
        }

        // Floating Action Button to Add Detailed Task
        FloatingActionButton(
            onClick = onOpenAddTask,
            containerColor = Emerald400,
            contentColor = Color(0xFF022C22),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}
