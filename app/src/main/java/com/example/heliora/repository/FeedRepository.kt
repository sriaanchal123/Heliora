package com.example.heliora.repository

import android.net.Uri
import android.util.Log
import com.example.heliora.models.FeedPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FeedRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val TAG = "FeedRepository"

    fun getPosts(): Flow<List<FeedPost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching posts from Firestore: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { it.toObject(FeedPost::class.java)?.copy(id = it.id) } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: FeedPost, imageUris: List<Uri> = emptyList()): Result<String> {
        return try {
            val uploadedUrls = mutableListOf<String>()
            
            // Upload selected photos to Firebase Storage
            for (uri in imageUris) {
                try {
                    val photoId = UUID.randomUUID().toString()
                    val ref = storage.reference.child("post_images/$photoId.jpg")
                    ref.putFile(uri).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    uploadedUrls.add(downloadUrl)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload post image: ${e.message}")
                }
            }

            val finalPost = post.copy(imageUrls = uploadedUrls)
            val docRef = firestore.collection("posts").add(finalPost).await()
            Log.d(TAG, "Post created successfully in Firestore: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create post: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun likePost(postId: String, currentLikes: Int): Result<Unit> {
        return try {
            firestore.collection("posts").document(postId)
                .update("likesCount", currentLikes + 1).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
