package com.example.heliora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(onBack: () -> Unit) {
    val communities = listOf(
        CommunityData("AI in Healthcare", "4500 Members", "Latest trends and research in AI medical tech."),
        CommunityData("Cardiology Group", "1200 Members", "Heart health discussion for professionals."),
        CommunityData("Medical Students", "8000 Members", "Study materials and exam tips."),
        CommunityData("Yoga & Wellness", "3000 Members", "Holistic health and lifestyle.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFEBF3FE))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(communities) { community ->
                CommunityItem(community)
            }
        }
    }
}

@Composable
fun CommunityItem(data: CommunityData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = data.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = data.members, color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = data.description, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Join")
            }
        }
    }
}

data class CommunityData(val name: String, val members: String, val description: String)
