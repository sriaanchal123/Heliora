package com.example.heliora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHealthcareScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf("All") }
    val categories = listOf("All", "Doctors", "Hospitals", "Labs", "Pharmacies", "Clinics")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Healthcare", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFEBF3FE))) {
            // Mock Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Interactive Map View", color = Color.Gray)
                    Text("Current Location: New Delhi, India", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Category Chips
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedType),
                containerColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { category ->
                    val selected = selectedType == category
                    FilterChip(
                        selected = selected,
                        onClick = { selectedType = category },
                        label = { Text(category) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nearbyData.filter { (selectedType == "All") || (it.type == selectedType) }) { data ->
                    NearbyCard(data)
                }
            }
        }
    }
}

@Composable
fun NearbyCard(data: NearbyHealthcareData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(data.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = null, tint = data.iconColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(data.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${data.type} • ${data.distance}", fontSize = 14.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                    Text(" ${data.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (data.isOpen) "Open Now" else "Closed", color = if (data.isOpen) Color(0xFF10B981) else Color.Red, fontSize = 12.sp)
                }
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB).copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Navigation, contentDescription = "Directions", tint = Color(0xFF2563EB))
            }
        }
    }
}

data class NearbyHealthcareData(
    val name: String,
    val type: String,
    val distance: String,
    val rating: Double,
    val isOpen: Boolean,
    val icon: ImageVector,
    val iconColor: Color
)

val nearbyData = listOf(
    NearbyHealthcareData("City Hospital", "Hospitals", "1.2 km", 4.8, true, Icons.Default.LocalHospital, Color(0xFFEF4444)),
    NearbyHealthcareData("Dr. Lal PathLabs", "Labs", "0.8 km", 4.6, true, Icons.Default.Science, Color(0xFF2563EB)),
    NearbyHealthcareData("Apollo Pharmacy", "Pharmacies", "0.5 km", 4.5, true, Icons.Default.Medication, Color(0xFF2563EB)),
    NearbyHealthcareData("Dr. Khanna's Clinic", "Clinics", "2.1 km", 4.9, false, Icons.Default.MedicalServices, Color(0xFF10B981)),
    NearbyHealthcareData("Metropolis Lab", "Labs", "1.5 km", 4.7, true, Icons.Default.Science, Color(0xFF8B5CF6)),
    NearbyHealthcareData("Wellness Lab", "Clinics", "3.0 km", 4.2, true, Icons.Default.Science, Color(0xFF8B5CF6)),
    NearbyHealthcareData("Max Healthcare", "Hospitals", "4.5 km", 4.7, true, Icons.Default.LocalHospital, Color(0xFFEF4444))
)
