package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentAppUI(viewModel: TournamentViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val selectedTournament by viewModel.selectedTournament.collectAsStateWithLifecycle()
    val transMessage by viewModel.transactionMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNotificationDrawer by remember { mutableStateOf(false) }

    // Theme values override for beautiful esports gaming style
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1782573571721),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "BATTLE 2.0",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    if (currentScreen != Screen.DASHBOARD) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            showNotificationDrawer = true
                            viewModel.clearNotificationCount()
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = AccentOrange) {
                                            Text(text = unreadCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.TOURNAMENTS_LIST -> TournamentsListScreen(viewModel)
                    Screen.TOURNAMENT_DETAILS -> TournamentDetailsScreen(viewModel, selectedTournament)
                    Screen.DEPOSIT_SCREEN -> DepositScreen(viewModel, transMessage)
                    Screen.WITHDRAW_SCREEN -> WithdrawScreen(viewModel, transMessage)
                    Screen.LEADERBOARD_SCREEN -> LeaderboardScreen(viewModel)
                    Screen.NOTIFICATIONS_SCREEN -> {} // Handled via drawer
                }
            }

            // Notification drawer bottom sheet or modal overlays
            if (showNotificationDrawer) {
                NotificationDrawer(
                    notifications = notifications,
                    onClose = { showNotificationDrawer = false }
                )
            }
        }
    }
}

// ==========================================
// 1. Dashboard Screen
// ==========================================
@Composable
fun DashboardScreen(viewModel: TournamentViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tournaments by viewModel.tournaments.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Item
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1782573587844),
                    contentDescription = "Esports Stadium Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Linear Gradient Overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 80f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "ELITE TOURNAMENTS",
                        color = GoldYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Register. Win Matches. Claim UPI Prizes instantly.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Profile Wallet Card
        item {
            userProfile?.let { profile ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Profile Summary Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.linearGradient(listOf(AccentOrange, GoldYellow)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = profile.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "FF ID: ${profile.ffId} | LVL ${profile.level}",
                                    color = GreyText,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Edit Profile Dialog anchor
                            var showEditProfileDialog by remember { mutableStateOf(false) }
                            IconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = GoldYellow)
                            }

                            if (showEditProfileDialog) {
                                EditProfileDialog(
                                    profile = profile,
                                    onDismiss = { showEditProfileDialog = false },
                                    onSave = { name, ffId, level ->
                                        viewModel.updateProfile(name, ffId, level)
                                        showEditProfileDialog = false
                                    }
                                )
                            }
                        }

                        Divider(color = CardBorder, thickness = 1.dp)

                        // Balances
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "DEPOSITED BALANCE", color = GreyText, fontSize = 11.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${profile.balance.toInt()}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Deposit Min ₹10", color = LiveGreen, fontSize = 10.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "WINNINGS (WITHDRAWABLE)", color = GreyText, fontSize = 11.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${profile.winnings.toInt()}",
                                        color = GoldYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Min ₹50", color = AccentOrange, fontSize = 10.sp)
                                }
                            }
                        }

                        // Wallet Actions Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.DEPOSIT_SCREEN) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("deposit_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Deposit ₹", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.navigateTo(Screen.WITHDRAW_SCREEN) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .border(1.dp, GoldYellow, RoundedCornerShape(10.dp))
                                    .testTag("withdraw_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Wallet", tint = GoldYellow)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Withdraw UPI", fontWeight = FontWeight.Bold, color = GoldYellow)
                            }
                        }
                    }
                }
            }
        }

        // Stats summary row
        item {
            userProfile?.let { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(title = "Matches", value = profile.matchesPlayed.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Kills", value = profile.kills.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Headshots", value = profile.headshots.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Wins", value = profile.wins.toString(), modifier = Modifier.weight(1f))
                }
            }
        }

        // Menu Hub Grid Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // View Tournaments Card
                MenuHubCard(
                    title = "Tournaments",
                    subtitle = "Join customs matches",
                    icon = Icons.Default.PlayArrow,
                    color = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TOURNAMENTS_LIST) }
                )

                // Leaderboard & Invites Card
                MenuHubCard(
                    title = "Leaderboards",
                    subtitle = "Rewards & Invites",
                    icon = Icons.Default.Star,
                    color = GoldYellow,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.LEADERBOARD_SCREEN) }
                )
            }
        }

        // Live Matches / Spectate Quick Access
        item {
            Text(
                text = "🔴 LIVE MATCHES & LOBBIES",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Filter for active/live tournaments
        val liveTournaments = tournaments.filter { it.status == "LIVE" }
        if (liveTournaments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "No matches", tint = GreyText, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Live Matches currently.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Upcoming tournaments will show details & rooms here when they go live.",
                            color = GreyText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(liveTournaments) { liveMatch ->
                LiveMatchCard(liveMatch = liveMatch, viewModel = viewModel)
            }
        }

        // Banned Rules Warning Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231010)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF5E1B1B), RoundedCornerShape(12.dp))
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Rules", tint = AccentOrange, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "⚠️ MAG-7 & RYDEN CHARACTER BANNED",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Double Vector is not allowed in any room. Late players will not get entry or refunds. Strict action will lead to permanent customs ban.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Stat Card Composable
// ==========================================
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title.uppercase(), color = GreyText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

// ==========================================
// Menu Hub Card
// ==========================================
@Composable
fun MenuHubCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color)
            }
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, color = GreyText, fontSize = 10.sp)
            }
        }
    }
}

// ==========================================
// Live Match Card
// ==========================================
@Composable
fun LiveMatchCard(liveMatch: Tournament, viewModel: TournamentViewModel) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LiveGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(LiveGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "LIVE SPECTATE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = liveMatch.category, color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Info, contentDescription = "Live count", tint = LiveGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Spectators Live", color = LiveGreen, fontSize = 11.sp)
            }

            Text(text = liveMatch.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "ROOM ID", color = GreyText, fontSize = 10.sp)
                    Text(text = liveMatch.roomId ?: "REVEALING SOON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PASSWORD", color = GreyText, fontSize = 10.sp)
                    Text(text = liveMatch.roomPassword ?: "REVEALING SOON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val link = liveMatch.spectatorLink ?: "https://youtube.com"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Spectate Live Stream", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.selectTournament(liveMatch) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Text(text = "Match Details", color = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 2. Tournaments List Screen
// ==========================================
@Composable
fun TournamentsListScreen(viewModel: TournamentViewModel) {
    val tournaments by viewModel.tournaments.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf("ALL", "BR Custom", "CS Custom", "Lone Wolf Custom", "Headshot Custom")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Available Tournaments",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Horizontal Category Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GoldYellow else CardBg)
                        .border(1.dp, if (isSelected) GoldYellow else CardBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setCategory(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Tournament List
        val filteredTournaments = if (selectedCategory == "ALL") {
            tournaments
        } else {
            tournaments.filter { it.category == selectedCategory }
        }

        if (filteredTournaments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No tournaments in this category yet.", color = GreyText)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTournaments) { item ->
                    TournamentItemCard(tournament = item, onClick = { viewModel.selectTournament(item) })
                }
            }
        }
    }
}

@Composable
fun TournamentItemCard(tournament: Tournament, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AccentOrange.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = tournament.category.uppercase(), color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Time", tint = GoldYellow, modifier = Modifier.size(14.dp))
                    Text(text = tournament.matchTime, color = LightText, fontSize = 11.sp)
                }
            }

            Text(
                text = tournament.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Divider(color = CardBorder, thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "PRIZE POOL", color = GreyText, fontSize = 10.sp)
                    Text(text = "₹${tournament.prizePool.toInt()}", color = GoldYellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ENTRY FEE", color = GreyText, fontSize = 10.sp)
                    Text(text = "₹${tournament.fee.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "STATUS", color = GreyText, fontSize = 10.sp)
                    val color = when (tournament.status) {
                        "LIVE" -> LiveGreen
                        "UPCOMING" -> Color.White
                        else -> GreyText
                    }
                    Text(text = tournament.status, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ==========================================
// 3. Tournament Details Screen
// ==========================================
@Composable
fun TournamentDetailsScreen(viewModel: TournamentViewModel, tournament: Tournament?) {
    if (tournament == null) return

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val registrations by viewModel.getRegistrationsForSelectedTournament().collectAsStateWithLifecycle(emptyList())
    val transMessage by viewModel.transactionMessage.collectAsStateWithLifecycle()

    var showRegisterDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(Screen.TOURNAMENTS_LIST) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Match Details",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .background(GoldYellow.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = tournament.category.uppercase(), color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Text(text = "Status: ${tournament.status}", color = if (tournament.status == "LIVE") LiveGreen else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text(text = tournament.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Divider(color = CardBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "TOTAL PRIZE", color = GreyText, fontSize = 10.sp)
                            Text(text = "₹${tournament.prizePool.toInt()}", color = GoldYellow, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                        Column {
                            Text(text = "ENTRY FEE", color = GreyText, fontSize = 10.sp)
                            Text(text = "₹${tournament.fee.toInt()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                        Column {
                            Text(text = "TIME", color = GreyText, fontSize = 10.sp)
                            Text(text = tournament.matchTime, color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Slots Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                val registeredCount = registrations.size
                val maxSlots = if (tournament.category == "CS Custom") 8 else if (tournament.category == "Lone Wolf Custom") 2 else 48
                val fraction = registeredCount.toFloat() / maxSlots.toFloat()

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Lobby Slots Filler", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "$registeredCount / $maxSlots Slots", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    LinearProgressIndicator(
                        progress = fraction.coerceIn(0f, 1f),
                        color = AccentOrange,
                        trackColor = CardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Room credentials if LIVE or Registered
        item {
            if (tournament.status == "LIVE") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A1E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LiveGreen, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🔑 LIVE CUSTOM ROOM INFO", color = LiveGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "ROOM ID", color = GreyText, fontSize = 10.sp)
                                Text(text = tournament.roomId ?: "REVEALING", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text(text = "PASSWORD", color = GreyText, fontSize = 10.sp)
                                Text(text = tournament.roomPassword ?: "REVEALING", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Description Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Match Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = tournament.details, color = LightText, fontSize = 12.sp)
                }
            }
        }

        // Banned items & Rules Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1111)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF421D1D), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "📜 STRICT TOURNAMENT RULES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = tournament.rules, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 20.sp)
                }
            }
        }

        // Action Buttons Row
        item {
            val alreadyRegistered = registrations.any { it.playerFfId == profile?.ffId }

            if (alreadyRegistered) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF152A20)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, LiveGreen, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Checked", tint = LiveGreen, modifier = Modifier.size(24.dp))
                        Column {
                            Text(text = "REGISTERED SUCCESSFULLY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Room ID & Password will display here when match is live.", color = GreyText, fontSize = 11.sp)
                        }
                    }
                }
            } else if (tournament.status == "COMPLETED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "MATCH COMPLETED", color = GreyText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Winner: ${tournament.winnerName ?: "TBD"}", color = GoldYellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            } else {
                Button(
                    onClick = { showRegisterDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Register for ₹${tournament.fee.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }

        // Participants header
        item {
            Text(
                text = "Registered Players (${registrations.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (registrations.isEmpty()) {
            item {
                Text(text = "No participants registered yet. Be the first to join!", color = GreyText, fontSize = 12.sp)
            }
        } else {
            items(registrations) { reg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = reg.playerName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = reg.playerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "FF ID: ${reg.playerFfId}", color = GreyText, fontSize = 11.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Level ${reg.playerLevel}", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (!reg.teamName.isNullOrBlank()) {
                                Text(text = "Team: ${reg.teamName}", color = GreyText, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Register dialog
    if (showRegisterDialog && profile != null) {
        RegisterMatchDialog(
            tournament = tournament,
            profile = profile!!,
            transMessage = transMessage,
            onDismiss = {
                viewModel.clearTransactionMessage()
                showRegisterDialog = false
            },
            onConfirm = { ffId, name, level, teamName ->
                viewModel.registerPlayer(tournament.id, ffId, name, level, teamName)
            }
        )
    }
}

// ==========================================
// 4. Deposit QR Screen (matches user photo)
// ==========================================
@Composable
fun DepositScreen(viewModel: TournamentViewModel, transMessage: String?) {
    var depositAmountText by remember { mutableStateOf("10") }
    var upiIdText by remember { mutableStateOf("wahidkhan7860@fam") }
    var referenceText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(transMessage) {
        if (transMessage == "SUCCESS") {
            viewModel.navigateTo(Screen.DASHBOARD)
            viewModel.clearTransactionMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Instant Deposit via QR",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Visual reproduction of the User's uploaded QR Card!
        Box(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(LightYellow)
                .border(2.dp, GoldYellow, RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name displayed above the card details
                Text(
                    text = "wahidkhan",
                    color = Color(0xFFC78401),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // UPI Address title
                Text(
                    text = "wahidkhan7860@fam",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // The stylized Canvas QR code! Reproducing the photo perfectly
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    val scale = size.width / 21f

                    // 1. Draw top-left Finder pattern
                    drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(7 * scale, 7 * scale))
                    drawRect(color = Color.White, topLeft = Offset(scale, scale), size = Size(5 * scale, 5 * scale))
                    drawRect(color = Color.Black, topLeft = Offset(2 * scale, 2 * scale), size = Size(3 * scale, 3 * scale))

                    // 2. Draw top-right Finder pattern
                    drawRect(color = Color.Black, topLeft = Offset(14 * scale, 0f), size = Size(7 * scale, 7 * scale))
                    drawRect(color = Color.White, topLeft = Offset(15 * scale, scale), size = Size(5 * scale, 5 * scale))
                    drawRect(color = Color.Black, topLeft = Offset(16 * scale, 2 * scale), size = Size(3 * scale, 3 * scale))

                    // 3. Draw bottom-left Finder pattern
                    drawRect(color = Color.Black, topLeft = Offset(0f, 14 * scale), size = Size(7 * scale, 7 * scale))
                    drawRect(color = Color.White, topLeft = Offset(scale, 15 * scale), size = Size(5 * scale, 5 * scale))
                    drawRect(color = Color.Black, topLeft = Offset(2 * scale, 16 * scale), size = Size(3 * scale, 3 * scale))

                    // 4. Draw random QR Code pixels (simulated robustly)
                    val randomPixelPoints = listOf(
                        Pair(8, 2), Pair(9, 3), Pair(10, 4), Pair(11, 2), Pair(12, 5),
                        Pair(1, 9), Pair(2, 8), Pair(4, 9), Pair(5, 10), Pair(6, 8),
                        Pair(9, 8), Pair(10, 9), Pair(12, 10), Pair(11, 11),
                        Pair(16, 8), Pair(18, 9), Pair(19, 10), Pair(17, 12),
                        Pair(9, 14), Pair(10, 15), Pair(12, 16), Pair(13, 15),
                        Pair(15, 15), Pair(16, 17), Pair(18, 14), Pair(19, 18)
                    )
                    for (pt in randomPixelPoints) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(pt.first * scale, pt.second * scale),
                            size = Size(scale, scale)
                        )
                    }

                    // 5. Center Logo circular "X" (from user photo yellow/black X logo!)
                    val centerPx = size.width / 2
                    val radius = 2.5f * scale
                    drawCircle(color = Color.Black, radius = radius, center = Offset(centerPx, centerPx))
                    drawCircle(color = Color(0xFFEAA11D), radius = radius - 3f, center = Offset(centerPx, centerPx), style = Stroke(width = 2f))
                    // Drawing the 'X' symbol inside
                    val xSize = radius * 0.5f
                    drawLine(
                        color = Color(0xFFEAA11D),
                        start = Offset(centerPx - xSize, centerPx - xSize),
                        end = Offset(centerPx + xSize, centerPx + xSize),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFFEAA11D),
                        start = Offset(centerPx + xSize, centerPx - xSize),
                        end = Offset(centerPx - xSize, centerPx + xSize),
                        strokeWidth = 3f
                    )
                }

                // Trio and UPI Branding logos underneath
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "triö",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    // Custom drawn UPI style indicator
                    Text(
                        text = "UPI▷",
                        color = Color(0xFF0F7A50),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // famapp by trio branding indicator
        Text(
            text = "famapp by triö",
            color = GreyText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Enter Deposit Amount
        OutlinedTextField(
            value = depositAmountText,
            onValueChange = { depositAmountText = it },
            label = { Text("Deposit Amount (₹)", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldYellow,
                unfocusedBorderColor = CardBorder
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deposit_amount_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Enter Sender UPI ID
        OutlinedTextField(
            value = upiIdText,
            onValueChange = { upiIdText = it },
            label = { Text("Your UPI ID for Verification", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldYellow,
                unfocusedBorderColor = CardBorder
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (transMessage != null && transMessage != "SUCCESS") {
            Text(text = transMessage, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Simulate Action Instructions
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "💡 HOW TO DEPOSIT:", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "1. Scan the QR code card above or take a screenshot to open in PhonePe/GPay/Paytm.", color = LightText, fontSize = 11.sp)
                Text(text = "2. Send payment (minimum ₹10 is required).", color = LightText, fontSize = 11.sp)
                Text(text = "3. Input the amount paid and your UPI ID below, then click 'Confirm Deposit' for instant verification.", color = LightText, fontSize = 11.sp)
            }
        }

        Button(
            onClick = {
                val amount = depositAmountText.toDoubleOrNull() ?: 0.0
                viewModel.deposit(amount, upiIdText)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_deposit_button")
        ) {
            Text(text = "Confirm Paid Amount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        }
    }
}

// ==========================================
// 5. Withdraw Screen
// ==========================================
@Composable
fun WithdrawScreen(viewModel: TournamentViewModel, transMessage: String?) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var withdrawAmountText by remember { mutableStateOf("50") }
    var upiIdText by remember { mutableStateOf("") }

    LaunchedEffect(transMessage) {
        if (transMessage == "SUCCESS") {
            viewModel.navigateTo(Screen.DASHBOARD)
            viewModel.clearTransactionMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Withdraw Winnings",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Balance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "WITHDRAWABLE WINNINGS BALANCE", color = GreyText, fontSize = 11.sp)
                Text(text = "₹${userProfile?.winnings?.toInt() ?: 0}", color = GoldYellow, fontWeight = FontWeight.Black, fontSize = 32.sp)
                Text(text = "Verified active members can withdraw directly to UPI ID.", color = LightText, fontSize = 11.sp)
            }
        }

        // Amount Input
        OutlinedTextField(
            value = withdrawAmountText,
            onValueChange = { withdrawAmountText = it },
            label = { Text("Withdraw Amount (₹)", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldYellow,
                unfocusedBorderColor = CardBorder
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // UPI ID Input
        OutlinedTextField(
            value = upiIdText,
            onValueChange = { upiIdText = it },
            label = { Text("Receiver UPI ID (e.g. name@upi)", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = GoldYellow,
                unfocusedBorderColor = CardBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("withdraw_upi_input"),
            shape = RoundedCornerShape(12.dp)
        )

        if (transMessage != null && transMessage != "SUCCESS") {
            Text(text = transMessage, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2330)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "⚠️ WITHDRAWAL RULES:", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "• Minimum withdrawal limit is ₹50.", color = LightText, fontSize = 11.sp)
                Text(text = "• Winnings are credited to verified UPI account instantly.", color = LightText, fontSize = 11.sp)
                Text(text = "• Incorrect UPI ID may lead to transfer failure. Double check your ID.", color = LightText, fontSize = 11.sp)
            }
        }

        Button(
            onClick = {
                val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                viewModel.withdraw(amount, upiIdText)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_withdraw_button")
        ) {
            Text(text = "Withdraw Winnings", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        }
    }
}

// ==========================================
// 6. Leaderboard Screen
// ==========================================
@Composable
fun LeaderboardScreen(viewModel: TournamentViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transMessage by viewModel.transactionMessage.collectAsStateWithLifecycle()
    var referCodeText by remember { mutableStateOf("") }

    // Hardcoded dummy players leaderboard
    val leaderboardPlayers = listOf(
        Pair("1. STORM_RIDER", "LVL 67 | Wins: 42 | ₹2400 Won"),
        Pair("2. DEAGLE_GOD", "LVL 58 | Wins: 35 | ₹1950 Won"),
        Pair("3. HEADSHOT_KING", "LVL 61 | Wins: 29 | ₹1500 Won"),
        Pair("4. SQUAD_KILLER", "LVL 49 | Wins: 22 | ₹1100 Won"),
        Pair("5. FIGHTER_PRO (You)", "LVL 45 | Wins: 4 | ₹0 Won")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Leaderboard & Referral",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Referral Box
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "🎁 REFER & EARN ₹10 INSTANTLY", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "Invite friends to Battle 2.0. When they register or use your code, you both receive ₹10 in your deposited balance!", color = LightText, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "YOUR REFER CODE", color = GreyText, fontSize = 10.sp)
                        Text(text = profile?.referCode ?: "LOADING", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Button(
                        onClick = {
                            // Copy code simulation or share
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Copy Code", color = Color.White)
                    }
                }

                Divider(color = CardBorder)

                // Input to claim code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = referCodeText,
                        onValueChange = { referCodeText = it },
                        label = { Text("Friend's Code", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.weight(1.0f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = { viewModel.claimReferralCode(referCodeText) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(text = "Claim ₹10", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                val currentMessage = transMessage
                if (currentMessage == "SUCCESS_REFERRAL") {
                    Text(text = "Code claimed! ₹10 added to balance.", color = LiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else if (currentMessage != null && currentMessage.startsWith("Claim Failed")) {
                    Text(text = currentMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Leaderboard Ranking List
        Text(text = "🏆 TOP TOURNAMENT PLAYERS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        leaderboardPlayers.forEach { player ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = player.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = player.second, color = GreyText, fontSize = 11.sp)
                    }

                    Icon(Icons.Default.Star, contentDescription = "Star", tint = GoldYellow)
                }
            }
        }
    }
}

// ==========================================
// 7. Edit Profile Dialog
// ==========================================
@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var nameText by remember { mutableStateOf(profile.name) }
    var ffIdText by remember { mutableStateOf(profile.ffId) }
    var levelText by remember { mutableStateOf(profile.level.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Player Profile", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = CardBg,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Player Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    )
                )

                OutlinedTextField(
                    value = ffIdText,
                    onValueChange = { ffIdText = it },
                    label = { Text("Free Fire ID") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    )
                )

                OutlinedTextField(
                    value = levelText,
                    onValueChange = { levelText = it },
                    label = { Text("Player Level (min 40)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lvl = levelText.toIntOrNull() ?: profile.level
                    onSave(nameText, ffIdText, lvl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow)
            ) {
                Text(text = "Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color.White)
            }
        }
    )
}

// ==========================================
// 8. Register Match Dialog
// ==========================================
@Composable
fun RegisterMatchDialog(
    tournament: Tournament,
    profile: UserProfile,
    transMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String?) -> Unit
) {
    var ffIdText by remember { mutableStateOf(profile.ffId) }
    var playerNameText by remember { mutableStateOf(profile.name) }
    var levelText by remember { mutableStateOf(profile.level.toString()) }
    var teamNameText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Match Registration",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = CardBg,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(text = "Tournament: ${tournament.title}", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Entry Fee: ₹${tournament.fee.toInt()}", color = Color.White, fontSize = 13.sp)

                Divider(color = CardBorder)

                OutlinedTextField(
                    value = playerNameText,
                    onValueChange = { playerNameText = it },
                    label = { Text("Free Fire Username", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ffIdText,
                    onValueChange = { ffIdText = it },
                    label = { Text("Player Free Fire ID", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = levelText,
                    onValueChange = { levelText = it },
                    label = { Text("Player Level (Must be 40+)", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldYellow,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (tournament.category == "CS Custom") {
                    OutlinedTextField(
                        value = teamNameText,
                        onValueChange = { teamNameText = it },
                        label = { Text("Team Name (Optional)", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldYellow,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (transMessage != null && transMessage != "SUCCESS") {
                    Text(text = transMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else if (transMessage == "SUCCESS") {
                    Text(text = "Registration Successful!", color = LiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Brief Rules recap
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261818)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "⚠️ MAG7 & Ryden character BANNED.", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "⚠️ Double Vector banned. Late players get custom ban & no refunds.", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lvl = levelText.toIntOrNull() ?: 0
                    onConfirm(ffIdText, playerNameText, lvl, teamNameText.ifBlank { "Solo Player" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                Text(text = "Confirm & Register", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Color.White)
            }
        }
    )
}

// ==========================================
// 9. Notification Drawer Panel
// ==========================================
@Composable
fun NotificationDrawer(
    notifications: List<LocalNotification>,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(CardBg)
                .border(1.dp, CardBorder)
                .clickable(enabled = false) { }
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications Logs",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Divider(color = CardBorder)

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No notifications yet.", color = GreyText)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = notif.title, color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = notif.message, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
