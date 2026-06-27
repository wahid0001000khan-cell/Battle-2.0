package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    TOURNAMENTS_LIST,
    TOURNAMENT_DETAILS,
    DEPOSIT_SCREEN,
    WITHDRAW_SCREEN,
    LEADERBOARD_SCREEN,
    NOTIFICATIONS_SCREEN
}

class TournamentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TournamentDatabase.getDatabase(application)
    private val repo = TournamentRepository(db.tournamentDao())

    // UI state flows
    val userProfile: StateFlow<UserProfile?> = repo.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val tournaments: StateFlow<List<Tournament>> = repo.tournaments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<Transaction>> = repo.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<LocalNotification>> = repo.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = repo.unreadNotificationsCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Selection & Navigation
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTournament = MutableStateFlow<Tournament?>(null)
    val selectedTournament: StateFlow<Tournament?> = _selectedTournament.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Transaction status states
    private val _transactionMessage = MutableStateFlow<String?>(null)
    val transactionMessage: StateFlow<String?> = _transactionMessage.asStateFlow()

    init {
        // Initialize user profile and default tournaments if they do not exist
        viewModelScope.launch {
            // Check if profile exists, if not insert default
            val profile = db.tournamentDao().getUserProfileSync()
            if (profile == null) {
                db.tournamentDao().insertUserProfile(
                    UserProfile(
                        name = "FIGHTER_PRO",
                        ffId = "559281729",
                        level = 45, // Must be >= 40!
                        referCode = "FFB20_Z6Y",
                        balance = 100.0, // Starting bonus balance
                        winnings = 0.0,
                        invites = 0,
                        matchesPlayed = 12,
                        kills = 38,
                        headshots = 19,
                        wins = 4
                    )
                )
            }

            // Check if tournaments exist, if not populate with a diverse set of tournaments
            db.tournamentDao().getAllTournaments().firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    populateDefaultTournaments()
                }
            } ?: populateDefaultTournaments()
        }
    }

    private suspend fun populateDefaultTournaments() {
        val rulesText = """
            🚫 MAG-7 Gun is strictly banned!
            🚫 Ryden Character is strictly banned!
            🚫 Double Vector is not allowed!
            ⚠️ FF Level minimum 40 above required.
            ⏰ If you do not arrive by match time, the match will start and NO REFUND is given.
            ❌ Failure to follow these rules results in instant Custom Ban!
        """.trimIndent()

        val sampleTournaments = listOf(
            Tournament(
                title = "Elite BR Custom Clash",
                category = "BR Custom",
                fee = 10.0,
                prizePool = 250.0,
                matchTime = "Today, 06:30 PM",
                details = "Map: Bermuda (Solo). Classic Survival. Survival points & Kill bonuses apply.",
                rules = rulesText,
                status = "UPCOMING",
                roomId = null,
                roomPassword = null
            ),
            Tournament(
                title = "CS Master 4v4 Showdown",
                category = "CS Custom",
                fee = 20.0,
                prizePool = 500.0,
                matchTime = "Today, 08:00 PM",
                details = "Map: Kalahari (Squad). Best of 7 rounds. High competitive tier.",
                rules = rulesText,
                status = "UPCOMING",
                roomId = null,
                roomPassword = null
            ),
            Tournament(
                title = "Lone Wolf Sniper 1v1",
                category = "Lone Wolf Custom",
                fee = 10.0,
                prizePool = 150.0,
                matchTime = "Tomorrow, 04:00 PM",
                details = "Map: Science Museum. Snipers only (AWM/M82B). Showcase pure skills.",
                rules = rulesText,
                status = "UPCOMING",
                roomId = null,
                roomPassword = null
            ),
            Tournament(
                title = "Deagle Headshot Championship",
                category = "Headshot Custom",
                fee = 15.0,
                prizePool = 350.0,
                matchTime = "Today, 05:00 PM",
                details = "Map: Decimator. Desert Eagle ONLY. Only headshot kills are counted!",
                rules = rulesText,
                status = "LIVE",
                roomId = "8920194",
                roomPassword = "123",
                spectatorLink = "https://www.youtube.com/live/ff_championship"
            ),
            Tournament(
                title = "Grand BR Custom Royale",
                category = "BR Custom",
                fee = 30.0,
                prizePool = 1000.0,
                matchTime = "Tomorrow, 09:00 PM",
                details = "Map: Purgatory (Duo/Solo). Full survival map with double prize pool.",
                rules = rulesText,
                status = "UPCOMING",
                roomId = null,
                roomPassword = null
            )
        )

        for (tournament in sampleTournaments) {
            db.tournamentDao().insertTournament(tournament)
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _transactionMessage.value = null
    }

    fun selectTournament(tournament: Tournament) {
        _selectedTournament.value = tournament
        navigateTo(Screen.TOURNAMENT_DETAILS)
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateProfile(name: String, ffId: String, level: Int) {
        viewModelScope.launch {
            val current = db.tournamentDao().getUserProfileSync()
            if (current != null) {
                db.tournamentDao().updateUserProfile(
                    current.copy(name = name, ffId = ffId, level = level)
                )
                repo.insertNotification(
                    LocalNotification(
                        title = "Profile Updated",
                        message = "Your Free Fire Player details have been successfully updated."
                    )
                )
            }
        }
    }

    fun registerPlayer(tournamentId: Int, playerFfId: String, playerName: String, playerLevel: Int, teamName: String?) {
        viewModelScope.launch {
            val t = repo.getTournamentById(tournamentId) ?: return@launch
            val profile = db.tournamentDao().getUserProfileSync() ?: return@launch

            // Check conditions
            if (playerLevel < 40) {
                _transactionMessage.value = "Registration Failed: Free Fire level must be 40 or above."
                return@launch
            }
            if (profile.balance < t.fee) {
                _transactionMessage.value = "Registration Failed: Insufficient balance. Please deposit minimum ₹10."
                return@launch
            }

            val reg = Registration(
                tournamentId = tournamentId,
                playerFfId = playerFfId,
                playerName = playerName,
                playerLevel = playerLevel,
                teamName = teamName
            )
            repo.registerForTournament(reg, t.fee)

            // Auto refresh selected tournament details
            val updatedT = repo.getTournamentById(tournamentId)
            _selectedTournament.value = updatedT
            _transactionMessage.value = "SUCCESS"
        }
    }

    fun deposit(amount: Double, upiId: String) {
        viewModelScope.launch {
            if (amount < 10.0) {
                _transactionMessage.value = "Deposit Failed: Minimum deposit is ₹10."
                return@launch
            }
            repo.depositMoney(amount, upiId)
            _transactionMessage.value = "SUCCESS"
        }
    }

    fun withdraw(amount: Double, upiId: String) {
        viewModelScope.launch {
            if (amount < 50.0) {
                _transactionMessage.value = "Withdrawal Failed: Minimum withdrawal is ₹50."
                return@launch
            }
            val success = repo.withdrawMoney(amount, upiId)
            if (success) {
                _transactionMessage.value = "SUCCESS"
            } else {
                _transactionMessage.value = "Withdrawal Failed: Insufficient winnings balance."
            }
        }
    }

    fun claimReferralCode(code: String) {
        viewModelScope.launch {
            if (code.isBlank()) {
                _transactionMessage.value = "Claim Failed: Code cannot be empty."
                return@launch
            }
            val success = repo.claimReferral(code)
            if (success) {
                _transactionMessage.value = "SUCCESS_REFERRAL"
            } else {
                _transactionMessage.value = "Claim Failed: Invalid code or self-referral."
            }
        }
    }

    fun clearNotificationCount() {
        viewModelScope.launch {
            repo.markAllNotificationsAsRead()
        }
    }

    fun clearTransactionMessage() {
        _transactionMessage.value = null
    }

    // Helper to get active registrations of selected tournament
    fun getRegistrationsForSelectedTournament(): Flow<List<Registration>> {
        val currentT = _selectedTournament.value ?: return flowOf(emptyList())
        return repo.getRegistrationsForTournament(currentT.id)
    }
}
