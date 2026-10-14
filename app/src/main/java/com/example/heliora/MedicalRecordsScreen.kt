package com.example.heliora

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.heliora.models.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(loggedInEmail: String, onBack: () -> Unit) {
    var records by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var previewRecordUrl by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    val categories = listOf("All", "Prescription", "Lab Report", "Bill", "Vaccine")

    // Fetch user medical records real-time from Firestore
    LaunchedEffect(loggedInEmail) {
        if (loggedInEmail.isNotBlank()) {
            firestore.collection("medical_records")
                .whereEqualTo("userId", loggedInEmail.trim().lowercase())
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error != null) {
                        Log.e("MedicalRecords", "Error fetching records: ${error.message}")
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { it.toObject(MedicalRecord::class.java)?.copy(id = it.id) } ?: emptyList()
                    records = if (list.isEmpty()) getSampleRecords(loggedInEmail) else list
                }
        } else {
            records = getSampleRecords("demo@heliora.com")
            isLoading = false
        }
    }

    val filteredRecords = records.filter {
        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Medical Records", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Upload Record", fontWeight = FontWeight.Bold) },
                shape = CircleShape
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFEBF3FE))
                .padding(16.dp)
        ) {
            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (filteredRecords.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No medical records found.", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Tap the '+' button below to upload your first prescription or report.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(filteredRecords) { record ->
                            MedicalRecordCard(
                                record = record,
                                onClick = { previewRecordUrl = record.fileUrl }
                            )
                        }
                    }
                }
            }
        }
    }

    // Upload Medical Record Dialog
    if (showUploadDialog) {
        UploadRecordDialog(
            loggedInEmail = loggedInEmail,
            onDismiss = { showUploadDialog = false },
            onSuccess = { msg ->
                showUploadDialog = false
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    // Image Full Preview Dialog
    if (previewRecordUrl != null) {
        AlertDialog(
            onDismissRequest = { previewRecordUrl = null },
            confirmButton = {
                TextButton(onClick = { previewRecordUrl = null }) { Text("Close") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    AsyncImage(
                        model = previewRecordUrl,
                        contentDescription = "Medical Document Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        )
    }
}

@Composable
fun MedicalRecordCard(record: MedicalRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2563EB).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (record.fileUrl.isNotBlank()) {
                    AsyncImage(
                        model = record.fileUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when (record.category) {
                            "Lab Report" -> Icons.Default.Biotech
                            "Bill" -> Icons.Default.Receipt
                            "Vaccine" -> Icons.Default.Vaccines
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = Color(0xFF2563EB).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = record.category,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (record.doctorName.isNotBlank()) {
                    Text(
                        text = "Doctor: ${record.doctorName}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.date.ifBlank { "Recent" },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFF2563EB))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadRecordDialog(
    loggedInEmail: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Prescription") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val categories = listOf("Prescription", "Lab Report", "Bill", "Vaccine")
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) selectedImageUri = uri
        }
    )

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Upload Medical Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record Title") },
                    placeholder = { Text("e.g. Blood Test Report, Rx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor / Lab Name") },
                    placeholder = { Text("e.g. Dr. Rahul Sharma") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // File Attachment Button / Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Document Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap to select image or document", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF2563EB))
            } else {
                Button(
                    onClick = {
                        if (title.isNotBlank() && selectedImageUri != null) {
                            isUploading = true
                            scope.launch {
                                try {
                                    val storage = FirebaseStorage.getInstance()
                                    val firestore = FirebaseFirestore.getInstance()
                                    val recordId = UUID.randomUUID().toString()
                                    val ref = storage.reference.child("medical_records/${loggedInEmail}_$recordId.jpg")
                                    
                                    ref.putFile(selectedImageUri!!).await()
                                    val downloadUrl = ref.downloadUrl.await().toString()

                                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    val formattedDate = dateFormat.format(Date())

                                    val newRecord = MedicalRecord(
                                        id = recordId,
                                        userId = loggedInEmail.trim().lowercase(),
                                        title = title.trim(),
                                        category = selectedCategory,
                                        doctorName = doctorName.trim(),
                                        date = formattedDate,
                                        fileUrl = downloadUrl,
                                        timestamp = System.currentTimeMillis()
                                    )

                                    firestore.collection("medical_records")
                                        .document(recordId)
                                        .set(newRecord)
                                        .await()

                                    onSuccess("Medical record uploaded successfully!")
                                } catch (e: Exception) {
                                    onSuccess("Upload failed: ${e.message}")
                                } finally {
                                    isUploading = false
                                }
                            }
                        }
                    },
                    enabled = title.isNotBlank() && selectedImageUri != null && !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            if (!isUploading) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

private fun getSampleRecords(email: String): List<MedicalRecord> {
    return listOf(
        MedicalRecord(
            id = "s1",
            userId = email,
            title = "Complete Blood Count (CBC)",
            category = "Lab Report",
            doctorName = "Dr. Lal PathLabs",
            date = "12 Oct 2026"
        ),
        MedicalRecord(
            id = "s2",
            userId = email,
            title = "Cardiology Consultation Rx",
            category = "Prescription",
            doctorName = "Dr. Rahul Sharma",
            date = "05 Oct 2026"
        ),
        MedicalRecord(
            id = "s3",
            userId = email,
            title = "COVID-19 Vaccination Certificate",
            category = "Vaccine",
            doctorName = "City Health Center",
            date = "15 Aug 2024"
        )
    )
}
