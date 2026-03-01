package com.example.parentalcontrolapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {

    var status by remember { mutableStateOf("Checking...") }
    var summary by remember { mutableStateOf("No incidents today") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(
                text = "Parental Control Dashboard",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            StatusCard(status)

            Spacer(modifier = Modifier.height(16.dp))

            SummaryCard(summary)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // TODO: Call your API here
                    status = "🟢 Active"
                    summary = "1 suspicious activity detected"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Status")
            }
        }
    }
}