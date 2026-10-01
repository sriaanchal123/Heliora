package com.example.heliora.repository

import com.example.heliora.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HealthcareRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getDoctors(): Flow<List<Doctor>> = callbackFlow {
        val listener = firestore.collection("doctors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doctors = snapshot?.documents?.mapNotNull { it.toObject(Doctor::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(doctors)
            }
        awaitClose { listener.remove() }
    }

    fun getHospitals(): Flow<List<Hospital>> = callbackFlow {
        val listener = firestore.collection("hospitals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val hospitals = snapshot?.documents?.mapNotNull { it.toObject(Hospital::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(hospitals)
            }
        awaitClose { listener.remove() }
    }

    suspend fun bookAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = firestore.collection("appointments").add(appointment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAppointments(userId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = firestore.collection("appointments")
            .whereEqualTo("patientId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { it.toObject(Appointment::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.collection("appointments").document(appointmentId)
                .update("status", "Cancelled").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBloodRequest(request: BloodRequest): Result<String> {
        return try {
            val docRef = firestore.collection("blood_requests").add(request).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
