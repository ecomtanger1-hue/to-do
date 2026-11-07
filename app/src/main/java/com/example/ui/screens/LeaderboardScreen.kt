package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SquadUiState
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.Emerald400

@Composable
fun LeaderboardScreen(
    state: SquadUiState,
    onOpenFindFriends: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedCode by remember { mutableStateOf(false) }

    val sortedMembers = state.members.sortedWith(
        compareByDescending<com.example.data.model.SquadMember> { member ->
            state.tasks.count { it.userId == member.id && it.isCompleted }
        }.thenByDescending { it.streakDays }
    )

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize().testTag("leaderboard_screen")
    ) {
        // Top Card: Squad Code & Find Friends
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Squad Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SQUAD-7788",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Emerald400
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("SQUAD-7788"))
                                    copiedCode = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Emerald400, modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenFindFriends,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald400),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald400.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("button_open_find_friends")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Find Friends", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Leaderboard Section Header
        item {
            Text(
                text = "Accountability Rankings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Leaderboard List
        itemsIndexed(sortedMembers) { index, member ->
            val memberTasks = state.tasks.filter { it.userId == member.id }
            val completed = memberTasks.count { it.isCompleted }
            val total = memberTasks.size
            val rate = if (total > 0) completed.toFloat() / total else 0f
            val isUser = member.id == state.currentUserId

            val rankLabel = when (index) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "#${index + 1}"
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isUser) Emerald400.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Rank label
                        Text(
                            text = rankLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Avatar
                        Text(text = member.avatar, fontSize = 22.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isUser) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(You)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Emerald400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "$completed/$total done (${(rate * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberFlame.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "🔥 ${member.streakDays}d",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberFlame,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
