package com.example.heliora.auth

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val TAG = "AuthRepository"

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun registerUser(name: String, email: String, password: String, role: String, imageUri: Uri? = null): Result<FirebaseUser?> {
        return try {
            val cleanEmail = email.trim().lowercase()
            val cleanName = name.trim()
            Log.d(TAG, "Attempting to create user: $cleanEmail")
            
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user
            Log.d(TAG, "User created successfully: ${user?.uid}")
            
            var profileImageUrl = ""
            if (imageUri != null && user != null) {
                try {
                    Log.d(TAG, "Attempting to upload profile image")
                    profileImageUrl = uploadProfileImage(user.uid, imageUri)
                    Log.d(TAG, "Image uploaded: $profileImageUrl")
                } catch (e: Exception) {
                    Log.e(TAG, "Image upload failed, but continuing registration: ${e.message}")
                }
            }

            // Update Firebase Auth Profile
            val profileUpdates = userProfileChangeRequest {
                displayName = cleanName
                if (profileImageUrl.isNotEmpty()) {
                    photoUri = Uri.parse(profileImageUrl)
                }
            }
            user?.updateProfile(profileUpdates)?.await()
            Log.d(TAG, "Firebase profile updated")
            
            val userMap = hashMapOf(
                "name" to cleanName,
                "email" to cleanEmail,
                "role" to role,
                "uid" to user?.uid,
                "profileImageUrl" to profileImageUrl
            )
            
            Log.d(TAG, "Saving user to Firestore")
            try {
                firestore.collection("users")
                    .document(cleanEmail)
                    .set(userMap)
                    .await()
                Log.d(TAG, "User saved to Firestore by email successfully")
            } catch (fsError: Exception) {
                Log.e(TAG, "Firestore email save error: ${fsError.message}")
            }

            if (user?.uid != null) {
                try {
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userMap)
                        .await()
                    Log.d(TAG, "User saved to Firestore by UID successfully")
                } catch (fsError2: Exception) {
                    Log.e(TAG, "Firestore UID save error: ${fsError2.message}")
                }
            }
                
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Pair<FirebaseUser?, String>> {
        return try {
            val cleanEmail = email.trim().lowercase()
            Log.d(TAG, "Attempting login for: $cleanEmail")
            
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user
            
            var name = user?.displayName
            try {
                val doc = firestore.collection("users").document(cleanEmail).get().await()
                if (doc.exists()) {
                    name = doc.getString("name") ?: name
                } else if (user?.uid != null) {
                    val uidDoc = firestore.collection("users").document(user.uid).get().await()
                    name = uidDoc.getString("name") ?: name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not fetch name from Firestore: ${e.message}")
            }
            
            val finalName = if (!name.isNullOrBlank()) name else "User"
            Log.d(TAG, "Login successful: $finalName")
            Result.success(Pair(user, finalName))
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImageForUser(email: String, uri: Uri): Result<String> {
        return try {
            val user = auth.currentUser
            if (user == null) return Result.failure(Exception("User not logged in"))
            
            val downloadUrl = uploadProfileImage(user.uid, uri)
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadProfileImage(uid: String, uri: Uri): String {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    fun logout() {
        auth.signOut()
    }
}
