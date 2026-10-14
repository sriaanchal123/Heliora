package com.example.heliora.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val experience: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val fee: String = "",
    val hospital: String = "",
    val about: String = "",
    val qualifications: List<String> = emptyList(),
    val availability: Map<String, String> = emptyMap(),
    val imageUrl: String = ""
)

data class Hospital(
    val id: String = "",
    val name: String = "",
    val type: String = "Multi-Specialty",
    val location: String = "",
    val distance: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val imageUrl: String = "",
    val contact: String = "",
    val departments: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val isEmergencyOpen: Boolean = true
)

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Pending",
    @ServerTimestamp val createdAt: Date? = null
)

data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val authorImageUrl: String = "",
    val content: String = "",
    val imageResList: List<Int> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Lab(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val location: String = "",
    val distance: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val imageUrl: String = "",
    val services: List<String> = emptyList(),
    val isHomeCollectionAvailable: Boolean = false
)

data class BloodRequest(
    val id: String = "",
    val patientName: String = "",
    val bloodGroup: String = "",
    val hospitalName: String = "",
    val contactNumber: String = "",
    val unitsNeeded: Int = 1,
    val isUrgent: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
