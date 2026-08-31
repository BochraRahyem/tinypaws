package com.example.ui

import com.example.R

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepBurgundy
import com.example.ui.theme.FrauncesFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChecklistScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.checklist_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("checklist_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.loc_back_to_home),
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DailyCareChecklistSection(viewModel = viewModel)
        }
    }
}
