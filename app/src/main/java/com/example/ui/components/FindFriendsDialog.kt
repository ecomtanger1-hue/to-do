package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FriendInvitation
import com.example.data.model.UserSearchResult
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400

@Composable
fun FindFriendsDialog(
    userHandle: String,
    squadCode: String,
    searchResults: List<UserSearchResult>,
    isSearching: Boolean,
    pendingInvitations: List<FriendInvitation>,
    onSearch: (String) -> Unit,
    onInviteFriend: (UserSearchResult) -> Unit,
    onAcceptInvite: (FriendInvitation) -> Unit,
    onJoinSquadCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var enterSquadCode by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedCode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("find_friends_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Find & Connect Friends",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hold each other accountable to your tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                val tabs = listOf("Search Directory", "Join Code", "Share Invite")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Emerald400
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Emerald400 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    // TAB 0: Search by Handle or Name
                    0 -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                onSearch(it)
                            },
                            placeholder = { Text("Search by @handle or name (e.g. @elena, @david)") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Emerald400)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_friend")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Emerald400)
                            }
                        } else if (searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matches found. Try searching by @handle!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                items(searchResults) { user ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.background,
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(text = user.avatar, fontSize = 20.sp)
                                                    }
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = user.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = user.handle,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Cyan400
                                                        )
                                                    }
                                                    Text(
                                                        text = user.focusArea,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            if (user.isAlreadyInSquad) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Emerald400.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "In Squad",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Emerald400,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = { onInviteFriend(user) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.testTag("button_invite_${user.id}")
                                                ) {
                                                    Text(
                                                        text = "+ Invite",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF022C22)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: Join Squad by Code
                    1 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Join Friend's Accountability Squad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ask your friends for their 6-digit squad code",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = enterSquadCode,
                                onValueChange = { enterSquadCode = it.uppercase() },
                                label = { Text("Squad Code") },
                                placeholder = { Text("e.g. SQUAD-7788") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_squad_code")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onJoinSquadCode(enterSquadCode) },
                                enabled = enterSquadCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_join_squad_code")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF022C22))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Join Squad & Sync Tasks ⚡",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF022C22)
                                )
                            }
                        }
                    }

                    // TAB 2: Share My Invite Link
                    2 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Invite Friends to Your Squad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Share your unique handle or squad code with your accountability partners",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Share Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(text = "Your Handle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = userHandle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Cyan400)
                                        }

                                        Column {
                                            Text(text = "Squad Code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = squadCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Emerald400)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Share via Intent
                            Button(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join my accountability squad on SquadDo! Find me with handle $userHandle or join using squad code $squadCode. Let's finish our daily goals together!"
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Invite Friends to SquadDo")
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_share_invite_intent")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF022C22))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Share Invite Link / Code 🚀",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF022C22)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("$userHandle | Squad Code: $squadCode"))
                                    copiedCode = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (copiedCode) "Copied to clipboard! ✅" else "Copy Handle & Code")
                            }
                        }
                    }
                }
            }
        }
    }
}
