package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SquadMember
import com.example.data.model.TaskItem
import com.example.ui.theme.Emerald400
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RoseNudge

@Composable
fun FriendAccountabilityCard(
    member: SquadMember,
    memberTasks: List<TaskItem>,
    isCurrentUser: Boolean,
    onNudge: (SquadMember) -> Unit,
    onClick: (SquadMember) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = memberTasks.count { it.isCompleted }
    val totalCount = memberTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "friendProgress"
    )

    val pendingTasks = memberTasks.filter { !it.isCompleted }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("friend_card_${member.id}")
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(member) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Avatar with online status dot
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = member.avatar, fontSize = 20.sp)
                    }
                }
                if (member.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                            .border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info & Progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrentUser) {
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
                        text = if (totalCount == 0) "No tasks" else "$completedCount/$totalCount done",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (progress >= 1f) Emerald400 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Slim Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = if (progress >= 1f) Emerald400 else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                if (pendingTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Working on: ${pendingTasks.first().title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }

            // Nudge button (if friend)
            if (!isCurrentUser) {
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = { onNudge(member) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseNudge),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseNudge.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("nudge_button_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Nudge",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Nudge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
