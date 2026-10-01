package com.example.heliora.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Patient",
    val profileImageUrl: String = "",
    val phone: String = "",
    val location: String = ""
)

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
    val status: String = "Pending", // Pending, Confirmed, Cancelled, Completed
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
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class BloodRequest(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val bloodType: String = "",
    val units: Int = 1,
    val hospitalName: String = "",
    val urgency: String = "Normal", // Normal, Urgent, Emergency
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)

data class Lab(
    val id: String = "",
    val name: String = "",
    val type: String = "Diagnostic Center",
    val location: String = "",
    val distance: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val contact: String = "",
    val services: List<String> = emptyList(), // Blood Test, MRI, X-Ray, etc.
    val isHomeCollectionAvailable: Boolean = true,
    val openTime: String = "08:00 AM",
    val closeTime: String = "08:00 PM"
)
