package com.example.ui

import androidx.compose.ui.layout.ContentScale


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.launch

sealed interface PreEvalResult {
    data class Success(
        val safeQuestionIdx: Int,
        val question: com.example.ui.QuizQuestion,
        val totalQuestions: Int,
        val progress: Float
    ) : PreEvalResult
    data class Error(val throwable: Throwable) : PreEvalResult
    object Empty : PreEvalResult
}

@Composable
fun GameModuleScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    onClaimCertificate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevelIndex by viewModel.currentIsland.collectAsStateWithLifecycle()
    val quizCompleted by viewModel.quizCompletedOnCurrentIsland.collectAsStateWithLifecycle()
    val savedScore by viewModel.scoreOnCurrentIsland.collectAsStateWithLifecycle()

    // Retrieve name to display "Hi, [Name]!"
    val onboardedName by viewModel.onboardedName.collectAsStateWithLifecycle()

    val allQuizzesCompleted by viewModel.allQuizzesCompleted.collectAsStateWithLifecycle()

    if (allQuizzesCompleted) {
        CertificateScreen(
            userName = onboardedName,
            onClose = {
                onBack()
            }
        )
        return
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Safely coerce level index to avoid out of bounds in maps/lists
    val safeLevelIndex = currentLevelIndex.coerceIn(0, 4)
    val levelQuestions = try {
        QuizData.questionsByLevel[safeLevelIndex] ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    var activeQuestionIndices by rememberSaveable(safeLevelIndex, quizCompleted) {
        mutableStateOf(levelQuestions.indices.toList())
    }
    var wrongQuestionIndices by rememberSaveable(safeLevelIndex, quizCompleted) {
        mutableStateOf(emptySet<Int>())
    }
    var currentQuestionIdx by rememberSaveable(safeLevelIndex, quizCompleted, activeQuestionIndices) {
        mutableIntStateOf(0)
    }
    var selectedOptionIdx by rememberSaveable(safeLevelIndex, quizCompleted, activeQuestionIndices, currentQuestionIdx) {
        mutableStateOf<Int?>(null)
    }
    var correctAnswersCount by rememberSaveable(safeLevelIndex, quizCompleted) { mutableIntStateOf(0) }
    var showRetryRoundScreen by rememberSaveable(safeLevelIndex, quizCompleted) { mutableStateOf(false) }
    var showPreviewDialog by rememberSaveable { mutableStateOf(false) }

    if (showPreviewDialog) {
        CertificatePreviewDialog(onDismiss = { showPreviewDialog = false })
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val animatedFlashColor by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = 300),
        finishedListener = { if (it != Color.Transparent) flashColor = Color.Transparent },
        label = "flashColor"
    )
    var isJumping by remember { mutableStateOf(false) }
    var targetIslandIndex by remember { mutableStateOf(safeLevelIndex) }
    var animationProgress by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Beautiful Header Row with clear Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                    modifier = Modifier.testTag("back_to_dashboard")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_btn),
                        tint = DeepBurgundy
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.hi_user, onboardedName),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "TinyPaws Care Academy 🎓",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                    )
                }
                if (!allQuizzesCompleted) {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { showPreviewDialog = true },
                        modifier = Modifier
                            .background(PastelPinkAccent.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Text("🏆", fontSize = 24.sp)
                    }
                }
            }

            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!quizCompleted) {
                    if (showRetryRoundScreen) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.5.dp, PastelPinkDark.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Review Needed 🐾",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBurgundy
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "You got some questions incorrect. Let's go back and answer them correctly to complete the level and unlock the next one!",
                                    fontSize = 14.sp,
                                    color = TextDark,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                                        activeQuestionIndices = wrongQuestionIndices.toList()
                                        wrongQuestionIndices = emptySet()
                                        showRetryRoundScreen = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "Retry Incorrect Questions (${wrongQuestionIndices.size})",
                                        fontWeight = FontWeight.Bold,
                                        color = White
                                    )
                                }
                            }
                        }
                    } else {
                        // Play Mode
                        var renderingError by remember { mutableStateOf<Throwable?>(null) }

                        // Pre-evaluate indices and data outside Composable function calls for safety
                        val currentError = renderingError
                        val preEvaluationResult = remember(levelQuestions, activeQuestionIndices, currentQuestionIdx, currentError) {
                            try {
                                if (currentError != null) {
                                    PreEvalResult.Error(currentError)
                                } else if (levelQuestions.isEmpty() || activeQuestionIndices.isEmpty()) {
                                    PreEvalResult.Empty
                                } else {
                                    val safeQuestionIdx = currentQuestionIdx.coerceIn(0, activeQuestionIndices.size - 1)
                                    val actualQuestionIndexInLevel = activeQuestionIndices[safeQuestionIdx]
                                    val question = levelQuestions[actualQuestionIndexInLevel]
                                    val totalQuestions = activeQuestionIndices.size
                                    val progress = (safeQuestionIdx + 1).toFloat() / totalQuestions

                                    // Access fields early to trigger potential exceptions in non-composable code
                                    question.questionRes
                                    question.optionsRes
                                    question.correctIndex
                                    question.correctFeedbackRes
                                    question.wrongFeedbackRes

                                    PreEvalResult.Success(
                                        safeQuestionIdx = safeQuestionIdx,
                                        question = question,
                                        totalQuestions = totalQuestions,
                                        progress = progress
                                    )
                                }
                            } catch (e: Throwable) {
                                android.util.Log.e("GameModule", "Exception during quiz pre-evaluation", e)
                                PreEvalResult.Error(e)
                            }
                        }

                when (preEvaluationResult) {
                    is PreEvalResult.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.5.dp, RedError)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = RedError,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.quiz_error_loading),
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = preEvaluationResult.throwable.localizedMessage ?: stringResource(R.string.quiz_error_loading),
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    PreEvalResult.Empty -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.5.dp, RedError)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = RedError,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.quiz_error_loading),
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.quiz_error_details, safeLevelIndex + 1),
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is PreEvalResult.Success -> {
                        val safeQuestionIdx = preEvaluationResult.safeQuestionIdx
                        val question = preEvaluationResult.question
                        val totalQuestions = preEvaluationResult.totalQuestions
                        val progress = preEvaluationResult.progress

                        // Clean modern progress bar
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.5.dp, SoftGray)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.quiz_question_progress, safeQuestionIdx + 1, totalQuestions),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = stringResource(QuizData.levelResIds[safeLevelIndex]),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelPurpleDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .testTag("quiz_progress_bar"),
                                    color = PastelPurpleDark,
                                    trackColor = SoftGray,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }

                        // Active Question Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.5.dp, PastelPurplePrimary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // The Question Text
                                Text(
                                    text = stringResource(question.questionRes),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        lineHeight = 22.sp
                                    ),
                                    modifier = Modifier.testTag("question_text")
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // List of Options
                                val options = question.optionsRes
                                options.forEachIndexed { optIdx, optionTextRes ->
                                    val optionText = stringResource(id = optionTextRes)
                                    val isSelected = selectedOptionIdx == optIdx
                                    val isAnswered = selectedOptionIdx != null
                                    val isCorrectOption = optIdx == question.correctIndex

                                    // Beautiful feedback styling colors
                                    val borderStrokeColor = when {
                                        isAnswered && isCorrectOption -> GreenSuccess
                                        isSelected -> RedError
                                        else -> SoftGray
                                    }

                                    val containerColor = when {
                                        isAnswered && isCorrectOption -> GreenSuccess.copy(alpha = 0.12f)
                                        isSelected -> RedError.copy(alpha = 0.12f)
                                        else -> LightPurpleBg.copy(alpha = 0.4f)
                                    }

                                    val textColor = when {
                                        isAnswered && isCorrectOption -> GreenSuccess
                                        isSelected -> RedError
                                        else -> TextDark
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .testTag("quiz_option_$optIdx")
                                            .clickable(enabled = !isAnswered) {
                                                selectedOptionIdx = optIdx
                                                val originalQuestionIdx = activeQuestionIndices[safeQuestionIdx]
                                                if (optIdx == question.correctIndex) {
                                                    correctAnswersCount++
                                                    wrongQuestionIndices = wrongQuestionIndices - originalQuestionIdx
                                                    playQuizSound(context, isCorrect = true)
                                                    flashColor = GreenSuccess.copy(alpha = 0.3f)
                                                } else {
                                                    wrongQuestionIndices = wrongQuestionIndices + originalQuestionIdx
                                                    playQuizSound(context, isCorrect = false)
                                                    flashColor = RedError.copy(alpha = 0.3f)
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = containerColor),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, borderStrokeColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor,
                                                    lineHeight = 20.sp
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isAnswered) {
                                                if (isCorrectOption) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Correct",
                                                        tint = GreenSuccess,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                } else if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Incorrect",
                                                        tint = RedError,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isAnswered && isSelected,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    val isCorrect = selectedOptionIdx == question.correctIndex
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .fillMaxWidth()
                                            .background(
                                                color = if (isCorrect) GreenSuccess.copy(alpha = 0.08f) else RedError.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = if (isCorrect) stringResource(R.string.quiz_correct_title) else stringResource(R.string.quiz_wrong_title),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) GreenSuccess else RedError,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(if (isCorrect) question.correctFeedbackRes else question.wrongFeedbackRes),
                                            fontSize = 12.sp,
                                            color = TextDark,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Next Button
                                        Button(
                                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                                if (safeQuestionIdx + 1 < activeQuestionIndices.size) {
                                                    currentQuestionIdx++
                                                } else {
                                                    // Completed the round!
                                                    if (wrongQuestionIndices.isEmpty()) {
                                                        // Completed the level! Submit to viewModel
                                                        viewModel.submitQuizForCurrentIsland(levelQuestions.size)
                                                    } else {
                                                        showRetryRoundScreen = true
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("next_question_button")
                                        ) {
                                            Text(
                                                text = if (safeQuestionIdx + 1 < activeQuestionIndices.size) stringResource(R.string.quiz_next_btn) else stringResource(R.string.quiz_finish_btn),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            } else {
                // Completed State Screen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(2.dp, GreenSuccess)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_complete_title),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelPurpleDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.quiz_score_msg, savedScore, levelQuestions.size),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (savedScore >= 8) stringResource(R.string.quiz_guru_msg) else stringResource(R.string.quiz_effort_msg),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PastelPinkDark
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Advance to next level or show all complete banner
                        if (currentLevelIndex < 4) {
                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick { 
                                    isJumping = true
                                    targetIslandIndex = currentLevelIndex + 1
                                    coroutineScope.launch {
                                        val duration = 1800L
                                        val startTime = System.currentTimeMillis()
                                        while (System.currentTimeMillis() - startTime < duration) {
                                            val elapsed = System.currentTimeMillis() - startTime
                                            animationProgress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                                            kotlinx.coroutines.delay(16)
                                        }
                                        animationProgress = 1f
                                        kotlinx.coroutines.delay(100)
                                        viewModel.nextIslandWithAnimation { }
                                        isJumping = false
                                        animationProgress = 0f
                                        currentQuestionIdx = 0
                                        selectedOptionIdx = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("travel_next_island"),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isJumping
                            ) {
                                Text(
                                    text = if (isJumping) stringResource(R.string.quiz_jumping_msg) else stringResource(R.string.quiz_advance_btn, currentLevelIndex + 2),
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.quiz_all_complete),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.resetGameProgress() },
                            modifier = Modifier.testTag("reset_all_progress_button")
                        ) {
                            Text(
                                text = stringResource(R.string.quiz_reset_replay),
                                color = RedError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

        // Screen Flash Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedFlashColor)
        )
    }
}

data class IslandData(
    val levelIndex: Int,
    val nameRes: Int,
    val fraction: Float, // horizontal position fraction (non-linear for increasing distance)
    val sizeDp: Int,     // island diameter grows with progression
    val altTextRes: Int,
    val descriptionRes: Int
)

@Composable
fun IslandGrowthJourneyVisualizer(
    currentLevelIndex: Int,
    isJumping: Boolean,
    animationProgress: Float,
    targetIslandIndex: Int,
    onSelectIsland: (Int) -> Unit
) {
    val islands = listOf(
        IslandData(
            levelIndex = 0,
            nameRes = R.string.island_0_name,
            fraction = 0.05f,
            sizeDp = 40,
            altTextRes = R.string.island_0_alt,
            descriptionRes = R.string.island_0_desc
        ),
        IslandData(
            levelIndex = 1,
            nameRes = R.string.island_1_name,
            fraction = 0.23f, // gap = 18%
            sizeDp = 48,
            altTextRes = R.string.island_1_alt,
            descriptionRes = R.string.island_1_desc
        ),
        IslandData(
            levelIndex = 2,
            nameRes = R.string.island_2_name,
            fraction = 0.47f, // gap = 24%
            sizeDp = 56,
            altTextRes = R.string.island_2_alt,
            descriptionRes = R.string.island_2_desc
        ),
        IslandData(
            levelIndex = 3,
            nameRes = R.string.island_3_name,
            fraction = 0.74f, // gap = 27%
            sizeDp = 64,
            altTextRes = R.string.island_3_alt,
            descriptionRes = R.string.island_3_desc
        ),
        IslandData(
            levelIndex = 4,
            nameRes = R.string.island_4_name,
            fraction = 1.00f, // gap = 26%
            sizeDp = 72,
            altTextRes = R.string.island_4_alt,
            descriptionRes = R.string.island_4_desc
        )
    )

    var selectedIslandIndex by remember(currentLevelIndex) { mutableIntStateOf(currentLevelIndex) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.2.dp, PastelPurplePrimary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.island_journey_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelPurpleDark
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.island_journey_subtitle),
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2D Island Map Container (Dashed Line & Growing Islands)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(LightPurpleBg.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val startMargin = 12.dp
                // Since the last island size is 72.dp, we subtract it from usable width
                val usableWidthDp = maxWidth - startMargin - startMargin - 72.dp

                // Connecting solid gradient line in background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = startMargin + 20.dp)
                        .align(Alignment.CenterStart)
                        .background(PastelPurplePrimary.copy(alpha = 0.35f))
                )

                // Render Islands
                islands.forEach { island ->
                    val isCurrent = island.levelIndex == currentLevelIndex
                    val isSelected = island.levelIndex == selectedIslandIndex
                    val isPassed = island.levelIndex < currentLevelIndex
                    val islandX = startMargin + (usableWidthDp * island.fraction)
                    val size = island.sizeDp.dp

                    Box(
                        modifier = Modifier
                            .offset(x = islandX)
                            .size(size)
                            .background(
                                color = when {
                                    isCurrent -> PastelPurpleDark
                                    isSelected -> PastelPurpleDark.copy(alpha = 0.8f)
                                    isPassed -> PastelPurplePrimary.copy(alpha = 0.4f)
                                    else -> SoftGray.copy(alpha = 0.8f)
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = if (isCurrent) 2.5.dp else if (isSelected) 1.5.dp else 0.dp,
                                color = if (isCurrent) PastelPinkDark else if (isSelected) PastelPurplePrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedIslandIndex = island.levelIndex
                                onSelectIsland(island.levelIndex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lvl ${island.levelIndex + 1}",
                                fontSize = (size.value * 0.22f).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent || isSelected) White else TextDark
                            )
                        }
                    }
                }

                // Render the Jumping Kitten (Pip) with safe index coercion
                val safeCurrentLevelIndex = currentLevelIndex.coerceIn(0, islands.lastIndex)
                val safeTargetIslandIndex = targetIslandIndex.coerceIn(0, islands.lastIndex)

                val currentIslandSize = islands[safeCurrentLevelIndex].sizeDp.dp
                val targetIslandSize = islands[safeTargetIslandIndex].sizeDp.dp
                val kittenSize = 34.dp

                // Centers kitten exactly over the island circle
                val startKittenX = startMargin + (usableWidthDp * islands[safeCurrentLevelIndex].fraction) + (currentIslandSize / 2f) - (kittenSize / 2f)
                val endKittenX = startMargin + (usableWidthDp * islands[safeTargetIslandIndex].fraction) + (targetIslandSize / 2f) - (kittenSize / 2f)

                val kittenX = if (isJumping) {
                    startKittenX + (endKittenX - startKittenX) * animationProgress
                } else {
                    startKittenX
                }

                // Parabolic arc height
                val arcHeight = 45.dp
                val kittenY = if (isJumping) {
                    48.dp - (arcHeight * kotlin.math.sin(animationProgress * kotlin.math.PI).toFloat())
                } else {
                    48.dp
                }

                // Gold glitter particles when jumping
                if (isJumping) {
                    val particleOffsets = listOf(
                        -15f to -15f,
                        15f to -25f,
                        -12f to 12f,
                        22f to 4f
                    )
                    particleOffsets.forEachIndexed { i, (dx, dy) ->
                        Box(
                            modifier = Modifier
                                .offset(x = kittenX + dx.dp, y = kittenY + dy.dp)
                                .size(if (i % 2 == 0) 6.dp else 4.dp)
                                .background(Color(0xFFFFD700), CircleShape) // Gold glitter
                        )
                    }
                }

                // Draw Kitten Character (Pip)
                Box(
                    modifier = Modifier
                        .offset(x = kittenX, y = kittenY)
                        .size(kittenSize)
                        .background(PastelPinkAccent, CircleShape)
                        .border(1.5.dp, PastelPinkDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pip",
                        fontSize = (kittenSize.value * 0.35f).sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPinkDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Storyboard Card for selected Island safely coerced
            val safeSelectedIslandIndex = selectedIslandIndex.coerceIn(0, islands.lastIndex)
            val selectedIsland = islands[safeSelectedIslandIndex]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightPurpleBg.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.island_details_header, selectedIsland.levelIndex + 1, stringResource(selectedIsland.nameRes)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PastelPurpleDark
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    selectedIsland.levelIndex == currentLevelIndex -> PastelPinkAccent.copy(alpha = 0.3f)
                                    selectedIsland.levelIndex < currentLevelIndex -> GreenSuccess.copy(alpha = 0.2f)
                                    else -> SoftGray
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, PastelPurplePrimary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = when {
                                    selectedIsland.levelIndex == currentLevelIndex -> stringResource(R.string.island_status_current)
                                    selectedIsland.levelIndex < currentLevelIndex -> stringResource(R.string.island_status_completed)
                                    else -> stringResource(R.string.island_status_locked)
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    selectedIsland.levelIndex == currentLevelIndex -> PastelPinkDark
                                    selectedIsland.levelIndex < currentLevelIndex -> GreenSuccess
                                    else -> TextMuted
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(selectedIsland.descriptionRes),
                        fontSize = 11.sp,
                        color = TextDark,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actual, High-Quality Concept Image for selected module
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.dp, SoftGray)
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.img_quiz_concept_fixed),
                                contentDescription = stringResource(selectedIsland.altTextRes),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.storyboard_narrative_header),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPurpleDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(selectedIsland.altTextRes),
                                    fontSize = 11.sp,
                                    color = TextDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun playQuizSound(context: android.content.Context, isCorrect: Boolean) {
    kotlin.concurrent.thread(start = true, name = "QuizSoundPlayer") {
        try {
            val sampleRate = 22050
            val durationSeconds = if (isCorrect) 0.3 else 0.4
            val numSamples = (sampleRate * durationSeconds).toInt()
            val samples = ShortArray(numSamples)

            if (isCorrect) {
                // Ascending beep triad
                val noteDuration = (sampleRate * 0.1).toInt()
                for (i in 0 until numSamples) {
                    val freq = when {
                        i < noteDuration -> 523.25 // C5
                        i < noteDuration * 2 -> 659.25 // E5
                        else -> 783.99 // G5
                    }
                    val t = i.toDouble() / sampleRate
                    val value = kotlin.math.sin(2.0 * kotlin.math.PI * freq * t)
                    
                    // Smooth decay envelope for each segment
                    val localIndex = i % noteDuration
                    val envelope = 1.0 - (localIndex.toDouble() / noteDuration)
                    samples[i] = (value * 32767.0 * 0.25 * envelope).toInt().toShort()
                }
            } else {
                // Gentle sliding descending note
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 349.23 - (progress * 120.0) // F4 down to A3 (229 Hz)
                    val t = i.toDouble() / sampleRate
                    val value = kotlin.math.sin(2.0 * kotlin.math.PI * freq * t)
                    val envelope = 1.0 - progress
                    samples[i] = (value * 32767.0 * 0.2 * envelope).toInt().toShort()
                }
            }

            val minBufferSize = android.media.AudioTrack.getMinBufferSize(
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )
            
            val audioTrack = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                android.media.AudioTrack(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                    android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    numSamples * 2,
                    android.media.AudioTrack.MODE_STATIC,
                    android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            } else {
                @Suppress("DEPRECATION")
                android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
            }

            audioTrack.write(samples, 0, numSamples)
            audioTrack.play()
            
            Thread.sleep((durationSeconds * 1000 + 50).toLong())
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@Composable
fun CertificatePreviewDialog(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.cert_preview_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepBurgundy)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.certificate_bg),
                        contentDescription = stringResource(R.string.cert_content_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.4f)
                            .background(White, RoundedCornerShape(12.dp))
                            .border(2.dp, PastelPurplePrimary, RoundedCornerShape(12.dp))
                            .blur(8.dp),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = DeepBurgundy,
                        modifier = Modifier.size(48.dp).background(White.copy(alpha=0.7f), androidx.compose.foundation.shape.CircleShape).padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.cert_preview_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, textAlign = TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy)
                ) {
                    Text(stringResource(R.string.cert_preview_btn), color = Cream)
                }
            }
        }
    }
}
