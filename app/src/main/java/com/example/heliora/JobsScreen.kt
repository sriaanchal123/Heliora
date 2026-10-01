package com.example.heliora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Jobs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            JobsContent()
        }
    }
}

@Composable
fun JobsContent() {
    var searchQuery by remember { mutableStateOf("") }
    val jobs = listOf(
        JobData("Cardiologist", "Apollo Hospital", "Delhi", "Full-time"),
        JobData("Neurologist", "City General", "Mumbai", "Full-time"),
        JobData("Nurse", "Health Center", "Delhi", "Part-time"),
        JobData("Lab Technician", "City Labs", "Bangalore", "Full-time"),
        JobData("Pediatrician", "Max Healthcare", "Gurugram", "Full-time"),
        JobData("Radiologist", "Fortis Hospital", "Noida", "Contract")
    )

    val filteredJobs = if (searchQuery.isBlank()) jobs else jobs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.hospital.contains(searchQuery, ignoreCase = true) ||
        it.location.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBF3FE))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search jobs, hospitals, locations...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredJobs) { job ->
                JobItem(job)
            }
        }
    }
}

@Composable
fun JobItem(job: JobData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = job.hospital, color = Color.Gray, fontSize = 14.sp)
                Text(text = "${job.location} • ${job.type}", color = Color.Gray, fontSize = 12.sp)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

data class JobData(val title: String, val hospital: String, val location: String, val type: String)
