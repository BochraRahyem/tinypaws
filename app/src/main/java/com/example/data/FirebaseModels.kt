package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val profilePhoto: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val totalReportsCreated: Int = 0,
    val totalCatsReached: Int = 0,
    val totalStationsReached: Int = 0
)

data class CatReport(
    @DocumentId val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val description: String = "",
    val needs: String = "",
    val photoUrl: String = "",
    val reporterName: String = "",
    val reportedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val status: String = "active", // active | rescued | closed
    val reachedCount: Int = 0,
    val rescued: Boolean = false,
    val rescuedPhotoUrl: String? = null,
    val rescuedDescription: String? = null,
    val rescuedBy: String? = null,
    @ServerTimestamp val rescuedAt: Timestamp? = null
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
    val reachedCount: Int = 0,
    @ServerTimestamp val lastUpdated: Timestamp? = null
)

data class GlobalStatistics(
    val totalRegisteredUsers: Int = 0,
    val totalCatsReported: Int = 0,
    val totalCatsRescued: Int = 0,
    val totalFeedingStations: Int = 0,
    val totalFeedingStationsReached: Int = 0,
    @ServerTimestamp val lastUpdated: Timestamp? = null
)

data class DiaryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: Long = 0,
    val mood: String = "",
    val notes: String = "",
    val weight: Float? = null,
    val photos: String = "",
    val diaryEntryType: String = "general",
    @ServerTimestamp val createdAt: Timestamp? = null
)
