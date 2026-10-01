package com.example.heliora.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heliora.models.FeedPost
import com.example.heliora.repository.FeedRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: FeedRepository = FeedRepository()
) : ViewModel() {

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        repository.getPosts()
            .onStart { _isLoading.value = true }
            .onEach { fetchedPosts ->
                if (fetchedPosts.isEmpty()) {
                    _posts.value = getSamplePosts()
                } else {
                    _posts.value = fetchedPosts
                }
                _isLoading.value = false 
            }
            .launchIn(viewModelScope)
    }

    private fun getSamplePosts(): List<FeedPost> {
        val names = listOf("Dr. Rahul Sharma", "City Hospital", "Priya Verma", "Health Insights", "Dr. Amit Kumar")
        val roles = listOf("Cardiologist", "Medical Center", "Healthcare Student", "Wellness Blog", "Surgeon")
        val contents = listOf(
            "Transforming healthcare connectivity with HelioRa. 🏥 #HealthTech #Innovation",
            "Experience the future of medical networking where doctors and hospitals interact seamlessly.",
            "Just attended a great seminar on AI in medical diagnosis. Fascinating stuff!",
            "Health Tip: Stay hydrated and get at least 7 hours of sleep for better mental clarity.",
            "Proud to be part of the HelioRa network, making healthcare accessible to all."
        )
        // Using existing drawables
        val drawables = listOf(
            com.example.heliora.R.drawable.img_1,
            com.example.heliora.R.drawable.doctor,
            com.example.heliora.R.drawable.hospital,
            com.example.heliora.R.drawable.healthcare,
            com.example.heliora.R.drawable.clinics
        )

        return List(5) { i ->
            FeedPost(
                id = "sample_$i",
                authorName = names[i],
                authorRole = roles[i],
                content = contents[i],
                imageResList = listOf(drawables[i]),
                likesCount = 10 + i * 5,
                commentsCount = 2 + i,
                timestamp = System.currentTimeMillis() - (i * 3600000)
            )
        }
    }

    fun createPost(post: FeedPost) {
        viewModelScope.launch {
            repository.createPost(post)
        }
    }

    fun likePost(post: FeedPost) {
        viewModelScope.launch {
            repository.likePost(post.id, post.likesCount)
        }
    }
}
