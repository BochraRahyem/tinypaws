package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CatReport
import com.example.data.UserProfile
import com.example.ui.theme.*
import com.example.util.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val userProfile by viewModel.firebaseUserProfile.collectAsStateWithLifecycle()
    val activeReports by viewModel.activeReports.collectAsStateWithLifecycle()
    val rescueStories by viewModel.rescueStories.collectAsStateWithLifecycle()

    val myUid = user?.uid ?: ""
    // Guests (empty uid) must not match anonymous reports with reportedBy == "".
    val myReports = if (myUid.isBlank()) emptyList() else activeReports.filter { it.reportedBy == myUid }
    val myRescues = if (myUid.isBlank()) emptyList() else rescueStories.filter { it.rescuedBy == myUid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rescue_profile_title), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.chat_back), tint = DeepBurgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (userProfile?.profilePhoto != null) {
                            AsyncImage(
                                model = userProfile?.profilePhoto,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(BlushPink.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = DeepBurgundy,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val guardianText = stringResource(R.string.profile_community_guardian)
                        val anonymousText = stringResource(R.string.profile_anonymous_angel)

                        Text(
                            text = userProfile?.displayName?.ifEmpty { user?.email?.substringBefore("@") ?: guardianText } ?: guardianText,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = DeepBurgundy
                        )

                        Text(
                            text = user?.email ?: anonymousText,
                            fontFamily = QuicksandFontFamily,
                            fontSize = 14.sp,
                            color = TextMuted
                        )

                        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        val isVerified = firebaseUser?.isEmailVerified == true
                        val isAnonUser = firebaseUser?.isAnonymous == true
                        val context = androidx.compose.ui.platform.LocalContext.current
                        var resendMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                        val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsStateWithLifecycle()

                        if (!isAnonUser && firebaseUser != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isVerified) Color(0xFF81C784) else Color(0xFFFFB74D))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isVerified) stringResource(com.example.R.string.auth_email_verified_status)
                                               else stringResource(com.example.R.string.auth_email_unverified_status),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFD84315),
                                            fontFamily = QuicksandFontFamily
                                        )
                                    )
                                    if (!isVerified) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.checkEmailVerificationStatus(context) { _, msg ->
                                                        resendMessage = msg
                                                    }
                                                },
                                                border = BorderStroke(1.dp, DeepBurgundy),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(com.example.R.string.auth_check_verification),
                                                    fontSize = 11.sp,
                                                    color = DeepBurgundy,
                                                    fontFamily = QuicksandFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.resendVerificationEmail(context) { _, msg ->
                                                        resendMessage = msg
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                                shape = RoundedCornerShape(8.dp),
                                                enabled = resendCooldownSeconds == 0
                                            ) {
                                                Text(
                                                    text = if (resendCooldownSeconds > 0)
                                                        stringResource(com.example.R.string.auth_resend_cooldown, resendCooldownSeconds)
                                                    else
                                                        stringResource(com.example.R.string.auth_resend_verification),
                                                    fontSize = 11.sp,
                                                    color = Cream,
                                                    fontFamily = QuicksandFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    resendMessage?.let { msg ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg,
                                            fontSize = 11.sp,
                                            color = DeepBurgundy,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatBadge(
                                icon = Icons.Default.Pets,
                                count = userProfile?.totalReportsCreated ?: myReports.size,
                                label = stringResource(R.string.profile_stat_reported)
                            )
                            ProfileStatBadge(
                                icon = Icons.Default.Favorite,
                                count = myRescues.size,
                                label = stringResource(R.string.profile_stat_rescued)
                            )
                            ProfileStatBadge(
                                icon = Icons.Default.Star,
                                count = userProfile?.totalStars ?: 0,
                                label = stringResource(R.string.profile_stat_stars)
                            )
                        }
                    }
                }
            }

            // 5. User Profile Update (Reporting Champion, Rescue Champion, Impact Summary)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_champions_impact_title),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DeepBurgundy
                        )

                        // Reporting Champion section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.profile_reporting_champion),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = stringResource(R.string.profile_cat_badges_earned, userProfile?.catBadges ?: 0),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink
                            )
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Mauve.copy(alpha = 0.3f))

                        // Rescue Champion section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.profile_rescue_champion),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = stringResource(R.string.profile_rescue_stars_count, userProfile?.rescueStars ?: 0),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink
                            )
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Mauve.copy(alpha = 0.3f))

                        // Impact Summary Section
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.profile_impact_summary),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = stringResource(R.string.profile_cats_discovered, userProfile?.reportedCatsCount ?: 0),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            Text(
                                text = stringResource(R.string.profile_cats_helped, userProfile?.totalCatsReached ?: 0),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            Text(
                                text = stringResource(R.string.profile_cats_rescued, myRescues.size),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink
                            )
                        }
                    }
                }
            }

            // My Reported Cats Section
            item {
                Text(
                    text = stringResource(R.string.profile_active_reports_title),
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DeepBurgundy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            if (myReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = stringResource(R.string.profile_no_reports_empty),
                            fontFamily = QuicksandFontFamily,
                            color = TextMuted,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(myReports) { report ->
                    MyReportCard(report = report)
                }
            }

            // My Rescued Stories Section
            item {
                Text(
                    text = stringResource(R.string.profile_hall_of_fame_title),
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DeepBurgundy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            if (myRescues.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = stringResource(R.string.profile_no_rescues_empty),
                            fontFamily = QuicksandFontFamily,
                            color = TextMuted,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(myRescues) { story ->
                    MyRescueCard(report = story)
                }
            }
        }
    }
}

@Composable
fun ProfileStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = BlushPink.copy(alpha = 0.3f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            fontFamily = QuicksandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = DeepBurgundy
        )
        Text(
            text = label,
            fontFamily = QuicksandFontFamily,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
fun MyReportCard(report: CatReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (report.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = report.photoUrl,
                    contentDescription = "Cat Photo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Cream, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = DeepBurgundy)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val defaultReportTitle = stringResource(R.string.profile_stray_report_default)
                Text(
                    text = report.description.ifEmpty { defaultReportTitle },
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy,
                    fontSize = 15.sp
                )
                if (report.needs.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.profile_needs_prefix, report.needs),
                        fontFamily = QuicksandFontFamily,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = stringResource(R.string.profile_people_reached_out, report.reachedCount),
                    fontFamily = QuicksandFontFamily,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun MyRescueCard(report: CatReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PastelMint),
        border = BorderStroke(1.dp, Color(0xFF81C784))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                val defaultRescuedTitle = stringResource(R.string.profile_rescued_cat_default)
                Text(
                    text = report.rescuedDescription ?: report.description.ifEmpty { defaultRescuedTitle },
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.profile_status_safe_rescued),
                    fontFamily = QuicksandFontFamily,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
