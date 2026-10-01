package com.example.heliora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heliora.models.Hospital

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalProfileScreen(
    hospital: Hospital,
    onBack: () -> Unit, 
    onBookClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hospital.name) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Large Banner
            Image(
                painter = painterResource(id = R.drawable.hospital),
                contentDescription = "Hospital Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hospital.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified Healthcare Provider", fontSize = 14.sp, color = Color(0xFF2563EB))
                        }
                    }
                    if (hospital.isEmergencyOpen) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "24/7 EMERGENCY",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(18.dp))
                    Text(" ${hospital.rating}", fontWeight = FontWeight.Bold)
                    Text(" (${hospital.reviewsCount}+ Reviews)", color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(hospital.distance, color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(hospital.location, color = Color(0xFF64748B), fontSize = 14.sp)
                }
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(hospital.contact.isNotBlank()) hospital.contact else "+91 11 2345 6789", color = Color(0xFF64748B), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onBookClick,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Book Appointment", fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2563EB).copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF2563EB))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (hospital.departments.isNotEmpty()) {
                    HospitalSectionTitle("Departments")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hospital.departments) { dept ->
                            FilterChip(
                                selected = false,
                                onClick = {},
                                label = { Text(dept) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                HospitalSectionTitle("Doctors")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(3) {
                        SmallDoctorCard()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HospitalSectionTitle("Location on Map")
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                }

                if (hospital.facilities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HospitalSectionTitle("Facilities")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        hospital.facilities.forEach { facility ->
                            FacilityItem(facility)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    HospitalSectionTitle("Facilities")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FacilityItem("24/7 Emergency Service")
                        FacilityItem("ICU & Ventilator Support")
                        FacilityItem("Advanced Radiology (MRI/CT)")
                        FacilityItem("In-house Pharmacy & Blood Bank")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HospitalSectionTitle("Reviews")
                repeat(2) {
                    ReviewItem()
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FacilityItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF475569))
    }
}

@Composable
fun HospitalSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SmallDoctorCard() {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.doctor),
                contentDescription = "Doctor Image",
                modifier = Modifier.size(60.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dr. Rahul Sharma", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Cardiologist", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ReviewItem() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF1F5F9)))
            Spacer(modifier = Modifier.width(12.dp))
            Text("User Name", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        Text(
            text = "Great facilities and helpful staff. Highly recommended.",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFF1F5F9))
    }
}
