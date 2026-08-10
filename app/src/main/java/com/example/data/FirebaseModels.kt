package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val country: String = "",
    val profileInformation: String = "",
    val profilePhoto: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val geohash: String? = null,
    val fcmToken: String? = null,
    val preferredLanguage: String = "en",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val totalReportsCreated: Int = 0,
    val totalCatsReached: Int = 0,
    val totalStationsReached: Int = 0,
    val totalStars: Int = 0,
    val reportedCatsCount: Int = 0,
    val catBadges: Int = 0,
    val rescueStars: Int = 0
)

data class CatReport(
    @DocumentId val id: String = "",
    val catImageUrl: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val description: String = "",
    val needs: String = "",
    val reporterName: String = "",
    val reportedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val status: String = "active", // active | helped | adopted
    val reachedCount: Int = 0,
    val rescued: Boolean = false,
    val rescuedPhotoUrl: String? = null,
    val rescuedDescription: String? = null,
    val rescuedBy: String? = null,
    @ServerTimestamp val rescuedAt: Timestamp? = null,
    val helpedBy: String? = null,
    @ServerTimestamp val helpedAt: Timestamp? = null,
    val adoptedBy: String? = null,
    @ServerTimestamp val adoptedAt: Timestamp? = null
) {
    val displayPhotoUrl: String
        get() = catImageUrl.ifBlank { photoUrl }
}

data class UserAction(
    @DocumentId val id: String = "",
    val actionType: String = "", // feed_cat, vet_visit, fill_station, adopt_cat
    val starsEarned: Int = 0,
    val rewardAmount: Int = 0,
    val description: String = "",
    val userId: String = "",
    val relatedCatId: String? = null,
    val relatedStationId: String? = null,
    val catId: String? = null,
    val stationId: String? = null,
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class FeedingStation(
    @DocumentId val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val foodAvailable: Boolean = true,
    val status: String = "active",
    val reachedCount: Int = 0,
    @ServerTimestamp val lastUpdated: Timestamp? = null
)

data class GlobalStatistics(
    val totalUsers: Int = 0,
    val totalRegisteredUsers: Int = 0,
    val totalCatsReported: Int = 0,
    val totalCatsHelped: Int = 0,
    val totalCatsRescued: Int = 0,
    val totalAdoptedCats: Int = 0,
    val totalFeedingStations: Int = 0,
    val totalFeedingStationsReached: Int = 0,
    @ServerTimestamp val lastUpdated: Timestamp? = null
) {
    val effectiveUsersCount: Int get() = maxOf(totalUsers, totalRegisteredUsers)
    val effectiveHelpedCount: Int get() = maxOf(totalCatsHelped, totalCatsRescued)
}

data class DiaryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val catId: Int = 1,
    val date: Long = 0,
    val mood: String = "",
    val notes: String = "",
    val weight: Float? = null,
    val photos: String = "",
    val diaryEntryType: String = "general",
    @ServerTimestamp val createdAt: Timestamp? = null
)
