package com.example.heliora

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heliora.viewmodels.HealthcareViewModel
import com.example.heliora.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(onBack: () -> Unit, onDoctorClick: () -> Unit, viewModel: HealthcareViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val doctors by viewModel.doctors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Doctors", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                FilterSheetContent(onApply = { showFilters = false })
            }
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFEBF3FE))) {
            // Search & Filters
            Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = true, onClick = {}, label = { Text("Specialty") }) }
                    item { FilterChip(selected = false, onClick = {}, label = { Text("Availability") }) }
                    item { FilterChip(selected = false, onClick = {}, label = { Text("Experience") }) }
                    item { FilterChip(selected = false, onClick = {}, label = { Text("Rating") }) }
                }
            }
            
            // Results
            if (isLoading && doctors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filteredDoctors = doctors.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || it.specialty.contains(searchQuery, ignoreCase = true)
                    }
                    
                    if (filteredDoctors.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No doctors found matching your search.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredDoctors) { doctor ->
                            DoctorSearchCard(doctor, onDoctorClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorSearchCard(doctor: Doctor, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.doctor),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${doctor.specialty} • ${doctor.experience} Exp", fontSize = 14.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                        Text(" ${doctor.rating} (${doctor.reviewsCount} Reviews)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Consultation Fee", fontSize = 12.sp, color = Color.Gray)
                    Text(doctor.fee, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Book Now")
                }
            }
        }
    }
}

@Composable
fun FilterSheetContent(onApply: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        FilterSectionTitle("Experience")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text("5+ Yrs") })
            FilterChip(selected = false, onClick = {}, label = { Text("10+ Yrs") })
            FilterChip(selected = false, onClick = {}, label = { Text("15+ Yrs") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FilterSectionTitle("Rating")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = {}, label = { Text("4.0+") })
            FilterChip(selected = true, onClick = {}, label = { Text("4.5+") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FilterSectionTitle("Price Range")
        RangeSlider(value = 200f..1500f, onValueChange = {}, valueRange = 0f..3000f)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("₹0", fontSize = 12.sp, color = Color.Gray)
            Text("₹3000", fontSize = 12.sp, color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply Filters")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalSearchScreen(onBack: () -> Unit, onHospitalClick: (Hospital) -> Unit, viewModel: HealthcareViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var showTypeSubRow by remember { mutableStateOf(false) }
    
    // 4 Category Filter States
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var isRatingFilterActive by remember { mutableStateOf(false) }
    var isDistanceFilterActive by remember { mutableStateOf(false) }
    var isEmergencyFilterActive by remember { mutableStateOf(false) }

    val typeOptions = listOf("All", "Multi-Specialty", "Super-Specialty", "General Hospital", "Charitable Trust")

    val hospitals by viewModel.hospitals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isAnyFilterActive = selectedTypeFilter != "All" || isRatingFilterActive || isDistanceFilterActive || isEmergencyFilterActive
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Hospitals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAnyFilterActive) {
                        TextButton(onClick = {
                            selectedTypeFilter = "All"
                            isRatingFilterActive = false
                            isDistanceFilterActive = false
                            isEmergencyFilterActive = false
                            showTypeSubRow = false
                        }) {
                            Text("Reset", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFEBF3FE))) {
            // Search & Category Filters Header
            Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by hospital name, location or department") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 4 Category Filter Chips Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Category 1: Type
                    item {
                        FilterChip(
                            selected = selectedTypeFilter != "All" || showTypeSubRow,
                            onClick = { showTypeSubRow = !showTypeSubRow },
                            label = { Text(if (selectedTypeFilter == "All") "Type" else "Type: $selectedTypeFilter") },
                            leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = { Icon(if (showTypeSubRow) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Category 2: Rating (4.5+)
                    item {
                        FilterChip(
                            selected = isRatingFilterActive,
                            onClick = { isRatingFilterActive = !isRatingFilterActive },
                            label = { Text(if (isRatingFilterActive) "Rating: 4.5+ ★" else "Rating 4.5+") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Category 3: Distance (< 5 km)
                    item {
                        FilterChip(
                            selected = isDistanceFilterActive,
                            onClick = { isDistanceFilterActive = !isDistanceFilterActive },
                            label = { Text(if (isDistanceFilterActive) "Distance: < 5 km" else "Distance < 5km") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Category 4: Emergency (24/7 Open)
                    item {
                        FilterChip(
                            selected = isEmergencyFilterActive,
                            onClick = { isEmergencyFilterActive = !isEmergencyFilterActive },
                            label = { Text(if (isEmergencyFilterActive) "Emergency 24/7" else "Emergency 24/7") },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.1f),
                                labelColor = Color(0xFFEF4444)
                            )
                        )
                    }
                }

                // Sub-row for Type options when Type chip is clicked
                if (showTypeSubRow) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(typeOptions) { option ->
                            FilterChip(
                                selected = selectedTypeFilter == option,
                                onClick = { 
                                    selectedTypeFilter = option
                                },
                                label = { Text(option, fontSize = 12.sp) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
            
            // Filter Execution Logic
            val filteredHospitals = hospitals.filter { hospital ->
                val matchesSearch = searchQuery.isBlank() || 
                    hospital.name.contains(searchQuery, ignoreCase = true) || 
                    hospital.location.contains(searchQuery, ignoreCase = true) ||
                    hospital.type.contains(searchQuery, ignoreCase = true) ||
                    hospital.departments.any { it.contains(searchQuery, ignoreCase = true) }

                val matchesType = selectedTypeFilter == "All" || hospital.type.contains(selectedTypeFilter, ignoreCase = true)
                val matchesRating = !isRatingFilterActive || hospital.rating >= 4.5
                val matchesDistance = !isDistanceFilterActive || (hospital.distance.replace(" km", "").toDoubleOrNull() ?: 10.0) <= 5.0
                val matchesEmergency = !isEmergencyFilterActive || hospital.isEmergencyOpen

                matchesSearch && matchesType && matchesRating && matchesDistance && matchesEmergency
            }

            // Results Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAnyFilterActive) "Suggested Hospitals (${filteredHospitals.size})" else "All Hospitals (${filteredHospitals.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )
                if (isAnyFilterActive) {
                    Surface(
                        color = Color(0xFF2563EB).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Filter Active",
                            color = Color(0xFF2563EB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Results List
            if (isLoading && hospitals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredHospitals.isEmpty()) {
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
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No hospitals match the selected filter.", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Try clearing or changing your category filters.", fontSize = 13.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            selectedTypeFilter = "All"
                                            isRatingFilterActive = false
                                            isDistanceFilterActive = false
                                            isEmergencyFilterActive = false
                                            showTypeSubRow = false
                                            searchQuery = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Show All Hospitals")
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredHospitals) { hospital ->
                            HospitalSearchCard(hospital, onClick = { onHospitalClick(hospital) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalSearchCard(hospital: Hospital, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.hospital),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${hospital.location} • ${hospital.distance}", fontSize = 14.sp, color = Color.Gray)
                    }
                    Surface(
                        color = (if (hospital.isEmergencyOpen) Color(0xFF10B981) else Color.Red).copy(alpha = 0.1f), 
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (hospital.isEmergencyOpen) "OPEN" else "CLOSED", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                            color = if (hospital.isEmergencyOpen) Color(0xFF10B981) else Color.Red, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                    Text(" ${hospital.rating} (${hospital.reviewsCount} Reviews)", fontSize = 14.sp, color = Color.Gray)
                }
                
                if (hospital.departments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Departments: ${hospital.departments.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabSearchScreen(onBack: () -> Unit, onLabClick: (Lab) -> Unit, viewModel: HealthcareViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val labs by viewModel.labs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Diagnostic Labs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFEBF3FE))) {
            // Search Bar
            Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search labs or tests (Blood, MRI...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            
            // Results
            if (isLoading && labs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filteredLabs = labs.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || it.services.any { s -> s.contains(searchQuery, ignoreCase = true) }
                    }
                    
                    if (filteredLabs.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No labs found.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredLabs) { lab ->
                            LabSearchCard(lab, onClick = { onLabClick(lab) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabSearchCard(lab: Lab, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF2563EB))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(lab.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${lab.type} • ${lab.distance}", fontSize = 14.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                    Text(" ${lab.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            if (lab.isHomeCollectionAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home Collection Available", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Services: ${lab.services.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.1f), contentColor = Color(0xFF2563EB)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("View Details & Book Test", fontWeight = FontWeight.Bold)
            }
        }
    }
}
