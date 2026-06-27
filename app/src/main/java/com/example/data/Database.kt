package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val ffId: String,
    val level: Int,
    val referCode: String,
    val referredBy: String? = null,
    val balance: Double = 0.0,
    val winnings: Double = 0.0,
    val invites: Int = 0,
    val matchesPlayed: Int = 0,
    val kills: Int = 0,
    val headshots: Int = 0,
    val wins: Int = 0
)

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "BR Custom", "CS Custom", "Lone Wolf Custom", "Headshot Custom"
    val fee: Double,
    val prizePool: Double,
    val matchTime: String,
    val details: String,
    val rules: String,
    val status: String, // "UPCOMING", "LIVE", "COMPLETED"
    val roomId: String? = null,
    val roomPassword: String? = null,
    val spectatorLink: String? = null,
    val winnerName: String? = null
)

@Entity(tableName = "registrations")
data class Registration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val playerFfId: String,
    val playerName: String,
    val playerLevel: Int,
    val teamName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DEPOSIT", "WITHDRAWAL"
    val amount: Double,
    val upiId: String?,
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_notifications")
data class LocalNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface TournamentDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    // Tournaments
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Int): Tournament?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: Tournament)

    @Update
    suspend fun updateTournament(tournament: Tournament)

    // Registrations
    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId ORDER BY timestamp DESC")
    fun getRegistrationsForTournament(tournamentId: Int): Flow<List<Registration>>

    @Query("SELECT * FROM registrations WHERE playerFfId = :ffId")
    fun getRegistrationsForPlayer(ffId: String): Flow<List<Registration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: Registration)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // Notifications
    @Query("SELECT * FROM local_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<LocalNotification>>

    @Query("SELECT COUNT(*) FROM local_notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: LocalNotification)

    @Query("UPDATE local_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}

// ==========================================
// 3. Database
// ==========================================

@Database(
    entities = [
        UserProfile::class,
        Tournament::class,
        Registration::class,
        Transaction::class,
        LocalNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TournamentDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao

    companion object {
        @Volatile
        private var INSTANCE: TournamentDatabase? = null

        fun getDatabase(context: Context): TournamentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TournamentDatabase::class.java,
                    "tournament_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. Repository
// ==========================================

class TournamentRepository(private val dao: TournamentDao) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val tournaments: Flow<List<Tournament>> = dao.getAllTournaments()
    val transactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val notifications: Flow<List<LocalNotification>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()

    suspend fun insertUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dao.updateUserProfile(profile)
    }

    suspend fun getTournamentById(id: Int): Tournament? {
        return dao.getTournamentById(id)
    }

    suspend fun insertTournament(tournament: Tournament) {
        dao.insertTournament(tournament)
    }

    suspend fun updateTournament(tournament: Tournament) {
        dao.updateTournament(tournament)
    }

    fun getRegistrationsForTournament(tournamentId: Int): Flow<List<Registration>> {
        return dao.getRegistrationsForTournament(tournamentId)
    }

    fun getRegistrationsForPlayer(ffId: String): Flow<List<Registration>> {
        return dao.getRegistrationsForPlayer(ffId)
    }

    suspend fun registerForTournament(registration: Registration, fee: Double) {
        dao.insertRegistration(registration)
        val profile = dao.getUserProfileSync()
        if (profile != null) {
            val updatedProfile = profile.copy(
                balance = (profile.balance - fee).coerceAtLeast(0.0),
                matchesPlayed = profile.matchesPlayed + 1
            )
            dao.updateUserProfile(updatedProfile)
            // Insert transaction log
            dao.insertTransaction(
                Transaction(
                    type = "ENTRY FEE",
                    amount = fee,
                    upiId = null,
                    status = "COMPLETED"
                )
            )
            // Notify user
            dao.insertNotification(
                LocalNotification(
                    title = "Registration Successful",
                    message = "You have registered for '${registration.teamName ?: "Solo"}' match. ₹${fee.toInt()} deducted."
                )
            )
        }
    }

    suspend fun depositMoney(amount: Double, upiId: String) {
        val profile = dao.getUserProfileSync()
        if (profile != null) {
            val updatedProfile = profile.copy(
                balance = profile.balance + amount
            )
            dao.updateUserProfile(updatedProfile)
            // Log transaction
            dao.insertTransaction(
                Transaction(
                    type = "DEPOSIT",
                    amount = amount,
                    upiId = upiId,
                    status = "COMPLETED"
                )
            )
            // Notify
            dao.insertNotification(
                LocalNotification(
                    title = "Deposit Successful",
                    message = "₹${amount.toInt()} deposited instantly via QR Code. Transaction complete."
                )
            )
        }
    }

    suspend fun withdrawMoney(amount: Double, upiId: String): Boolean {
        val profile = dao.getUserProfileSync() ?: return false
        if (profile.winnings < amount) return false

        val updatedProfile = profile.copy(
            winnings = profile.winnings - amount
        )
        dao.updateUserProfile(updatedProfile)
        
        // Log transaction
        dao.insertTransaction(
            Transaction(
                type = "WITHDRAWAL",
                amount = amount,
                upiId = upiId,
                status = "COMPLETED"
            )
        )
        
        // Notify
        dao.insertNotification(
            LocalNotification(
                title = "Withdrawal Placed",
                message = "₹${amount.toInt()} withdrawn to UPI ID: $upiId. Winnings updated."
            )
        )
        return true
    }

    suspend fun claimReferral(referCode: String): Boolean {
        val profile = dao.getUserProfileSync() ?: return false
        if (profile.referCode == referCode) return false // Cannot refer yourself
        
        // Add invite rewards: Give Rs 10 to user as referral bonus
        val updatedProfile = profile.copy(
            balance = profile.balance + 10.0,
            invites = profile.invites + 1
        )
        dao.updateUserProfile(updatedProfile)
        
        // Log transaction
        dao.insertTransaction(
            Transaction(
                type = "REFERRAL BONUS",
                amount = 10.0,
                upiId = null,
                status = "COMPLETED"
            )
        )
        
        // Notify
        dao.insertNotification(
            LocalNotification(
                title = "Referral Bonus Received",
                message = "You claimed code '$referCode' and received ₹10.00 bonus balance!"
            )
        )
        return true
    }

    suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction)
    }

    suspend fun insertNotification(notification: LocalNotification) {
        dao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }
}
