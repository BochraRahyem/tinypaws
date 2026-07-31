package com.example.ui

import com.example.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCatHubScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hub_title), fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy) },
                navigationIcon = {
                    IconButton(onClick = com.example.ui.theme.rememberHapticOnClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.hub_back), tint = DeepBurgundy)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HubButton(title = stringResource(R.string.hub_cat_profile), icon = Icons.Default.Info, onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("my_cat_profile") })
            HubButton(title = stringResource(R.string.hub_weight_tracker), icon = Icons.Default.DateRange, onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("weight_tracker") })
            HubButton(title = stringResource(R.string.hub_daily_checkin), icon = Icons.Default.Edit, onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("daily_checkin") })
            HubButton(title = stringResource(R.string.hub_reminders), icon = Icons.Default.Notifications, onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("reminders") })
        }
    }
}

@Composable
fun HubButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.2f)),
        onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = DeepBurgundy)
            Text(title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepBurgundy)
        }
    }
}
