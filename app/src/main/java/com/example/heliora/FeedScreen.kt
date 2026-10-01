package com.example.heliora

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.heliora.models.FeedPost
import com.example.heliora.models.Hospital
import com.example.heliora.models.Lab
import com.example.heliora.viewmodels.FeedViewModel
import com.example.heliora.viewmodels.HealthcareViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedScreen(loggedInEmail: String, initialName: String = "User", onLogout: () -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var selectedNewsIndex by remember { mutableIntStateOf(0) }
    var selectedDoctorName by remember { mutableStateOf("Dr. Rahul Sharma") }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }
    var selectedLab by remember { mutableStateOf<Lab?>(null) }
    
    val items = listOf("Home", "Explore", "Add", "Chat", "Pharmacy")
    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Add, Icons.AutoMirrored.Filled.Chat, Icons.Default.LocalPharmacy)
    
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val firestore = remember { FirebaseFirestore.getInstance() }
    var currentUserName by remember { mutableStateOf(initialName) }
    var currentUserRole by remember { mutableStateOf("Healthcare Enthusiast") }
    var currentUserPhoto by remember { mutableStateOf<String?>(null) }

    // Fetch user details from Firestore
    LaunchedEffect(loggedInEmail) {
        if (loggedInEmail.isNotBlank()) {
            firestore.collection("users")
                .document(loggedInEmail)
                .addSnapshotListener { document, error ->
                    if (error != null) return@addSnapshotListener
                    if (document != null && document.exists()) {
                        currentUserName = document.getString("name") ?: initialName
                        currentUserRole = document.getString("role") ?: "Healthcare Enthusiast"
                        currentUserPhoto = document.getString("profileImageUrl")
                    }
                }
        }
    }

    // Screen Switching Logic
    when (currentSubScreen) {
        "health_news" -> {
            val newsList = listOf(
                HealthNews(
                    title = "Immunity Boosting Foods You Should Include in Your Diet",
                    category = "Nutrition News",
                    imageRes = R.drawable.card1,
                    readTime = "3 min read"
                ),
                HealthNews(
                    title = "Daily Habits for a Healthy Heart",
                    category = "Doctor's Corner",
                    imageRes = R.drawable.card_2,
                    readTime = "4 min read"
                ),
                HealthNews(
                    title = "Leading Surgeon Performs Rare Life-Saving Procedure",
                    category = "Doctor News",
                    imageRes = R.drawable.card_3,
                    readTime = "5 min read"
                ),
                HealthNews(
                    title = "CityCare Hospital Opens Advanced Cardiac Centre",
                    category = "Hospital News",
                    imageRes = R.drawable.card_4,
                    readTime = "2 min read"
                )
            )
            HealthNewsScreen(
                newsList = newsList,
                initialPage = selectedNewsIndex,
                onBack = { currentSubScreen = null }
            )
        }
        "user_profile" -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { currentSubScreen = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                ProfileScreenContent(
                    currentUserName = currentUserName,
                    currentUserRole = currentUserRole,
                    profileImageUrl = currentUserPhoto,
                    onLogout = onLogout,
                    onUpdateProfile = { newName, newRole ->
                        currentUserName = newName
                        currentUserRole = newRole
                        if (loggedInEmail.isNotBlank()) {
                            firestore.collection("users").document(loggedInEmail)
                                .update(
                                    mapOf(
                                        "name" to newName,
                                        "role" to newRole
                                    )
                                )
                        }
                        scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                    },
                    onUpdateProfileImage = { uri ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Uploading profile photo...")
                            val authRepo = com.example.heliora.auth.AuthRepository()
                            val result = authRepo.uploadProfileImageForUser(loggedInEmail, uri)
                            if (result.isSuccess) {
                                val newUrl = result.getOrNull() ?: ""
                                currentUserPhoto = newUrl
                                firestore.collection("users").document(loggedInEmail)
                                    .update("profileImageUrl", newUrl)
                                snackbarHostState.showSnackbar("Profile photo updated!")
                            } else {
                                snackbarHostState.showSnackbar("Upload failed: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    onLaunchCamera = { currentSubScreen = "camera_capture" }
                )
            }
        }
        "doctor_profile" -> DoctorProfileScreen(onBack = { currentSubScreen = null }, onBookClick = { 
            selectedDoctorName = "Dr. Rahul Sharma"
            currentSubScreen = "appointment" 
        })
        "hospital_profile" -> {
            selectedHospital?.let { hospital ->
                HospitalProfileScreen(
                    hospital = hospital,
                    onBack = { currentSubScreen = null }, 
                    onBookClick = { currentSubScreen = "appointment" }
                )
            } ?: run { currentSubScreen = null }
        }
        "appointment" -> AppointmentScreen(
            doctorName = selectedDoctorName,
            onBack = { currentSubScreen = null }, 
            onConfirm = { 
                currentSubScreen = null
                scope.launch { snackbarHostState.showSnackbar("Appointment Confirmed!") }
            }
        )
        "notifications" -> NotificationsScreen(onBack = { currentSubScreen = null })
        "jobs" -> JobsScreen(onBack = { currentSubScreen = null })
        "pharmacy" -> PharmacyScreen(onBack = { currentSubScreen = null })
        "communities" -> CommunitiesScreen(onBack = { currentSubScreen = null })
        "emergency" -> EmergencyScreen(onBack = { currentSubScreen = null })
        "ai_chat" -> AiAssistantScreen(onBack = { currentSubScreen = null })
        "nearby_healthcare" -> NearbyHealthcareScreen(onBack = { currentSubScreen = null })
        "doctor_search" -> DoctorSearchScreen(onBack = { currentSubScreen = null }, onDoctorClick = { currentSubScreen = "doctor_profile" })
        "hospital_search" -> HospitalSearchScreen(onBack = { currentSubScreen = null }, onHospitalClick = { hospital -> 
            selectedHospital = hospital
            currentSubScreen = "hospital_profile" 
        })
        "lab_search" -> LabSearchScreen(onBack = { currentSubScreen = null }, onLabClick = { lab ->
            selectedLab = lab
            currentSubScreen = "lab_profile"
        })
        "lab_profile" -> {
            selectedLab?.let { lab ->
                LabProfileScreen(lab = lab, onBack = { currentSubScreen = "lab_search" })
            } ?: run { currentSubScreen = null }
        }
        "camera_capture" -> CameraCaptureScreen(
            onImageCaptured = { uri ->
                currentSubScreen = null
                // Trigger upload logic
                scope.launch {
                    val authRepo = com.example.heliora.auth.AuthRepository()
                    val result = authRepo.uploadProfileImageForUser(loggedInEmail, uri)
                    if (result.isSuccess) {
                        val newUrl = result.getOrNull() ?: ""
                        currentUserPhoto = newUrl
                        firestore.collection("users").document(loggedInEmail)
                            .update("profileImageUrl", newUrl)
                        snackbarHostState.showSnackbar("Profile photo updated!")
                    } else {
                        snackbarHostState.showSnackbar("Upload failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            },
            onBack = { currentSubScreen = null }
        )
        "create_post" -> CreatePostScreen(
            onBack = { currentSubScreen = null },
            currentUserName = currentUserName,
            currentUserRole = currentUserRole,
            currentUserPhoto = currentUserPhoto ?: "",
            onAction = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )
        else -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = Color.White,
                        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
                            Row(
                                modifier = Modifier.padding(vertical = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!currentUserPhoto.isNullOrBlank()) {
                                        AsyncImage(
                                            model = currentUserPhoto,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = if (currentUserName.isNotBlank()) currentUserName.take(1).uppercase() else "U",
                                            color = Color(0xFF2563EB),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentUserName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                                    Text(currentUserRole, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFFF1F5F9))
                            
                            DrawerMenuItem(Icons.Default.Person, "My Profile") { 
                                scope.launch { drawerState.close() }
                                currentSubScreen = "user_profile"
                            }
                            DrawerMenuItem(Icons.Default.Description, "Medical Records") { scope.launch { drawerState.close() } }
                            DrawerMenuItem(Icons.Default.Settings, "Settings") { scope.launch { drawerState.close() } }
                            DrawerMenuItem(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support") { scope.launch { drawerState.close() } }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
                            
                            DrawerMenuItem(Icons.AutoMirrored.Filled.Logout, "Logout", textColor = Color.Red) { 
                                scope.launch { drawerState.close() }
                                onLogout()
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { 
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(horizontal = 8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Search", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                                    }
                                }
                            },
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .clickable { scope.launch { drawerState.open() } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!currentUserPhoto.isNullOrBlank()) {
                                        AsyncImage(
                                            model = currentUserPhoto,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        if (currentUserName == "Loading..." || currentUserName.isBlank()) {
                                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(24.dp))
                                        } else {
                                            Text(
                                                text = currentUserName.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { currentSubScreen = "notifications" }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF2563EB)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = { Icon(icons[index], contentDescription = item) },
                                    label = { if (index != 2) Text(item) },
                                    selected = selectedItem == index,
                                    onClick = { 
                                        if (index == 2) {
                                            currentSubScreen = "create_post"
                                        } else {
                                            selectedItem = index 
                                        }
                                    }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "sos_blink")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "sos_alpha"
                            )

                            FloatingActionButton(
                                onClick = { currentSubScreen = "emergency" },
                                containerColor = Color(0xFFEF4444).copy(alpha = alpha),
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Emergency SOS")
                            }

                            FloatingActionButton(
                                onClick = { currentSubScreen = "ai_chat" },
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = "AI Assistant")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFEBF3FE))) {
                        when (selectedItem) {
                            0 -> FeedContent(
                                onAction = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                onSeeAllNews = { index ->
                                    selectedNewsIndex = index
                                    currentSubScreen = "health_news"
                                }
                            )
                            1 -> ExploreScreenContent(
                                onAction = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                onDoctorClick = { currentSubScreen = "doctor_profile" },
                                onHospitalClick = { hospital ->
                                    selectedHospital = hospital
                                    currentSubScreen = "hospital_profile"
                                },
                                onJobsClick = { selectedItem = 4 },
                                onCommunitiesClick = { currentSubScreen = "communities" },
                                onNearbyClick = { currentSubScreen = "nearby_healthcare" },
                                onDoctorSearchClick = { currentSubScreen = "doctor_search" },
                                onHospitalSearchClick = { currentSubScreen = "hospital_search" },
                                onLabSearchClick = { currentSubScreen = "lab_search" }
                            )
                            3 -> ChatScreenContent()
                            4 -> PharmacyContent(onAction = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    textColor: Color = Color(0xFF1E293B),
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (textColor == Color.Red) Color.Red else Color(0xFF2563EB)) },
        label = { Text(label, color = textColor, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}

data class HealthNews(
    val title: String,
    val category: String,
    val imageRes: Int,
    val readTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthNewsScreen(newsList: List<HealthNews>, initialPage: Int = 0, onBack: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { newsList.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Latest Health News", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFEBF3FE))
                .padding(vertical = 16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 16.dp
            ) { page ->
                val news = newsList[page]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Image(
                                painter = painterResource(id = news.imageRes),
                                contentDescription = news.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopStart),
                                color = Color(0xFF2563EB),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = news.category,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = news.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(news.readTime, fontSize = 13.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Stay informed with the latest updates from verified healthcare professionals, leading hospitals, and nutrition experts. Swipe horizontally to read more medical news updates.",
                                fontSize = 15.sp,
                                color = Color(0xFF475569),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page Indicator dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(newsList.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1))
                    )
                }
            }
        }
    }
}

@Composable
fun FeedContent(
    onAction: (String) -> Unit,
    onSeeAllNews: (Int) -> Unit = {},
    viewModel: FeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val newsList = remember {
        listOf(
            HealthNews(
                title = "Immunity Boosting Foods You Should Include in Your Diet",
                category = "Nutrition News",
                imageRes = R.drawable.card1,
                readTime = "3 min read"
            ),
            HealthNews(
                title = "Daily Habits for a Healthy Heart",
                category = "Doctor's Corner",
                imageRes = R.drawable.card_2,
                readTime = "4 min read"
            ),
            HealthNews(
                title = "Leading Surgeon Performs Rare Life-Saving Procedure",
                category = "Doctor News",
                imageRes = R.drawable.card_3,
                readTime = "5 min read"
            ),
            HealthNews(
                title = "CityCare Hospital Opens Advanced Cardiac Centre",
                category = "Hospital News",
                imageRes = R.drawable.card_4,
                readTime = "2 min read"
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Latest Health News",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                Text(
                                    "See All",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.clickable { onSeeAllNews(0) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(newsList) { news ->
                                    Card(
                                        modifier = Modifier
                                            .width(260.dp)
                                            .clickable { onSeeAllNews(newsList.indexOf(news)) },
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = news.imageRes),
                                                    contentDescription = news.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Surface(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .align(Alignment.TopStart),
                                                    color = Color(0xFF2563EB),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = news.category,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = news.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    lineHeight = 18.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = news.readTime,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                items(posts) { post ->
                    FeedPostItem(post, onAction, onLike = { viewModel.likePost(post) })
                }
            }
        }
    }
}

@Composable
fun PostImageGrid(images: List<Int>) {
    if (images.isEmpty()) return
    
    val modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(vertical = 4.dp)
        
    when (images.size) {
        1 -> {
            Image(
                painter = painterResource(id = images[0]),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            Row(modifier = modifier) {
                Image(
                    painter = painterResource(id = images[0]),
                    contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = images[1]),
                    contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        3 -> {
            Row(modifier = modifier) {
                Image(
                    painter = painterResource(id = images[0]),
                    contentDescription = null,
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(2.dp))
                Column(modifier = Modifier.weight(0.8f).fillMaxHeight()) {
                    Image(
                        painter = painterResource(id = images[1]),
                        contentDescription = null,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Image(
                        painter = painterResource(id = images[2]),
                        contentDescription = null,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        else -> {
            val pagerState = rememberPagerState(pageCount = { images.size })
            Box(modifier = modifier) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    Image(
                        painter = painterResource(id = images[page]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${images.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedPostItem(post: FeedPost, onAction: (String) -> Unit, onLike: () -> Unit) {
    var isLiked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                    if (post.authorImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = post.authorImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            if (post.authorName.isNotBlank()) post.authorName.take(1).uppercase() else "U",
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Text(post.authorRole, fontSize = 12.sp, color = Color.Gray)
                    Text("2h • 🌐", fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray) }
            }
            
            Text(
                text = post.content,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )
            
            PostImageGrid(post.imageResList)

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likesCount + if(isLiked) 1 else 0} likes", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text("${post.commentsCount} comments", fontSize = 12.sp, color = Color.Gray)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color(0xFFF1F5F9))

            Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceAround) {
                SocialActionButton(
                    icon = if(isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                    label = "Like",
                    color = if(isLiked) Color(0xFF2563EB) else Color.Gray,
                    onClick = { 
                        isLiked = !isLiked
                        if (isLiked) onLike()
                    }
                )
                SocialActionButton(icon = Icons.Outlined.ChatBubbleOutline, label = "Comment") { onAction("Opening Comments...") }
                SocialActionButton(icon = Icons.Default.Repeat, label = "Repost") { onAction("Reposting...") }
                SocialActionButton(icon = Icons.AutoMirrored.Filled.Send, label = "Send") { onAction("Sending post...") }
            }
        }
    }
}

@Composable
fun SocialActionButton(icon: ImageVector, label: String, color: Color = Color.Gray, onClick: () -> Unit = {}) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ExploreScreenContent(
    onAction: (String) -> Unit,
    onDoctorClick: () -> Unit,
    onHospitalClick: (Hospital) -> Unit,
    onJobsClick: () -> Unit,
    onCommunitiesClick: () -> Unit,
    onNearbyClick: () -> Unit,
    onDoctorSearchClick: () -> Unit,
    onHospitalSearchClick: () -> Unit,
    onLabSearchClick: () -> Unit,
    viewModel: HealthcareViewModel = viewModel()
) {
    val hospitals by viewModel.hospitals.collectAsState()
    val labs by viewModel.labs.collectAsState()
    
    val specialties = listOf(
        "Cardiology" to Icons.Default.Favorite,
        "Dentist" to Icons.Default.Mood,
        "Neurology" to Icons.Default.Psychology,
        "Orthopedic" to Icons.Default.Accessibility,
        "Pediatrics" to Icons.Default.ChildCare,
        "Dermatology" to Icons.Default.Face
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Search doctors, hospitals, specialties...") },
            modifier = Modifier.fillMaxWidth().clickable { onDoctorSearchClick() },
            enabled = false,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB)) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                disabledBorderColor = Color.Transparent,
                disabledPlaceholderColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categories = listOf("Doctors", "Hospitals", "Labs", "Nearby", "Pharmacy", "Communities")
            items(categories) { category ->
                FilterChip(
                    selected = category == "Doctors",
                    onClick = { 
                        when (category) {
                            "Doctors" -> onDoctorSearchClick()
                            "Hospitals" -> onHospitalSearchClick()
                            "Labs" -> onLabSearchClick()
                            "Nearby" -> onNearbyClick()
                            "Pharmacy" -> onJobsClick()
                            "Communities" -> onCommunitiesClick()
                        }
                    },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Text("Popular Specialties", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(specialties) { specialty ->
                SpecialtyCard(specialty.first, specialty.second) {
                    onDoctorSearchClick()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onNearbyClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFF10B981), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Find Nearby Healthcare", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Doctors, hospitals & pharmacies near you", fontSize = 13.sp, color = Color.Gray)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Top Specialists", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
            TextButton(onClick = onDoctorSearchClick) { Text("See All", color = Color(0xFF2563EB)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        DoctorCard(onAction, onDoctorClick)

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recommended Hospitals", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
            TextButton(onClick = onHospitalSearchClick) { Text("See All", color = Color(0xFF2563EB)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (hospitals.isNotEmpty()) {
            HospitalCard(hospitals[0], onHospitalClick = { onHospitalClick(hospitals[0]) })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recommended Labs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
            TextButton(onClick = onLabSearchClick) { Text("See All", color = Color(0xFF2563EB)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (labs.isNotEmpty()) {
            LabSearchCard(labs[0], onClick = { onLabSearchClick() })
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SpecialtyCard(name: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = name, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun DoctorCard(
    onAction: (String) -> Unit,
    onDoctorClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onDoctorClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.doctor),
                contentDescription = "Doctor Image",
                modifier = Modifier.size(60.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dr. Rahul Sharma", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Cardiologist", color = Color.Gray, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                    Text(" 4.9", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
            Button(
                onClick = { onAction("Booking Appointment...") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Book")
            }
        }
    }
}

@Composable
fun HospitalCard(hospital: Hospital, onHospitalClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onHospitalClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.hospital),
                contentDescription = "Hospital Image",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${hospital.type} • ${hospital.location}", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ChatScreenContent() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(5) {
                ChatItem()
            }
        }
    }
}

@Composable
fun ChatItem() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF2563EB)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Doctor Name", fontWeight = FontWeight.Bold)
            Text("Hello, how are you?", color = Color.Gray, maxLines = 1)
        }
        Text("10:45 AM", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileScreenContent(
    currentUserName: String, 
    currentUserRole: String, 
    profileImageUrl: String? = null,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onUpdateProfileImage: (Uri) -> Unit = {},
    onLaunchCamera: () -> Unit = {}
) {
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> 
            if (uri != null) {
                onUpdateProfileImage(uri)
            }
        }
    )

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Profile Photo") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Gallery") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Camera") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            onLaunchCamera()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(currentUserName) }
        var role by remember { mutableStateOf(currentUserRole) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role / Specialty") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onUpdateProfile(name.trim(), role.trim())
                            showEditProfileDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Cover Banner & Overlapping Profile Avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF2563EB))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp)
                    .clickable { showImageSourceDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (currentUserName.isNotBlank()) currentUserName.take(1).uppercase() else "U",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
                
                // Edit Overlay Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentUserName,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = currentUserRole,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                Text("250", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text("Followers", fontSize = 12.sp, color = Color.Gray) 
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                Text("120", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text("Posts", fontSize = 12.sp, color = Color.Gray) 
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = { showEditProfileDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile") 
            }

            OutlinedButton(
                modifier = Modifier.height(48.dp),
                onClick = { showImageSourceDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(48.dp),
            onClick = onLogout,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) { 
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", color = Color.Red, fontWeight = FontWeight.Medium) 
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    currentUserName: String,
    currentUserRole: String,
    currentUserPhoto: String,
    onAction: (String) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    var postText by remember { mutableStateOf("") }
    var attachPhotos by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (postText.isNotBlank()) {
                                if (attachPhotos) {
                                    val photos = listOf(R.drawable.img_1, R.drawable.doctor, R.drawable.hospital)
                                    photos.forEach { resId ->
                                        val p = FeedPost(
                                            authorName = currentUserName,
                                            authorRole = currentUserRole,
                                            authorImageUrl = currentUserPhoto,
                                            content = postText,
                                            timestamp = System.currentTimeMillis(),
                                            imageResList = listOf(resId)
                                        )
                                        viewModel.createPost(p)
                                    }
                                    onAction("3 separate posts created!")
                                } else {
                                    val newPost = FeedPost(
                                        authorName = currentUserName,
                                        authorRole = currentUserRole,
                                        authorImageUrl = currentUserPhoto,
                                        content = postText,
                                        timestamp = System.currentTimeMillis(),
                                        imageResList = emptyList()
                                    )
                                    viewModel.createPost(newPost)
                                    onAction("Post successful")
                                }
                                onBack()
                            }
                        },
                        enabled = postText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Post")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                    if (currentUserPhoto.isNotBlank()) {
                        AsyncImage(
                            model = currentUserPhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            if (currentUserName.isNotBlank()) currentUserName.take(1).uppercase() else "U",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(currentUserName, fontWeight = FontWeight.Bold)
                    Text(currentUserRole, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            TextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("What do you want to talk about?", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
            )

            if (attachPhotos) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(R.drawable.img_1, R.drawable.doctor, R.drawable.hospital).forEach { resId ->
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { attachPhotos = !attachPhotos }) {
                    Icon(Icons.Default.Image, contentDescription = "Add Media", tint = if(attachPhotos) Color(0xFF2563EB) else Color.Gray)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Gray) }
                IconButton(onClick = { }) { Icon(Icons.Default.Event, contentDescription = "Event", tint = Color.Gray) }
                IconButton(onClick = { }) { Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color.Gray) }
            }
        }
    }
}
