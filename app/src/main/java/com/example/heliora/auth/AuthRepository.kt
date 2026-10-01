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
            Log.d(TAG, "Attempting to create user: $email")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
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

            // Update Firebase Profile with name and photo
            val profileUpdates = userProfileChangeRequest {
                displayName = name
                if (profileImageUrl.isNotEmpty()) {
                    photoUri = Uri.parse(profileImageUrl)
                }
            }
            user?.updateProfile(profileUpdates)?.await()
            Log.d(TAG, "Firebase profile updated")
            
            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
                "role" to role,
                "uid" to user?.uid,
                "profileImageUrl" to profileImageUrl
            )
            
            Log.d(TAG, "Saving user to Firestore")
            firestore.collection("users")
                .document(email)
                .set(userMap)
                .await()
            Log.d(TAG, "User saved to Firestore successfully")
                
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Pair<FirebaseUser?, String>> {
        return try {
            Log.d(TAG, "Attempting login for: $email")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            // Fetch name from Firestore
            val name = firestore.collection("users")
                .document(email)
                .get()
                .await()
                .getString("name") ?: user?.displayName ?: "User"
            
            Log.d(TAG, "Login successful: $name")
            Result.success(Pair(user, name))
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
