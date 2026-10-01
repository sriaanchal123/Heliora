package com.example.heliora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heliora.models.Lab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabProfileScreen(lab: Lab, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lab Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(lab.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(lab.type, fontSize = 16.sp, color = Color.Gray)
            Text(lab.location, fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Book Lab Test")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Available Services", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            lab.services.forEach { service ->
                ListItem(
                    headlineContent = { Text(service) },
                    leadingContent = { Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFF2563EB)) },
                    modifier = Modifier.background(Color.White)
                )
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
