package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun ResetPasswordScreen(
    oobCode: String,
    viewModel: TinyPawsViewModel,
    onDone: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var accountEmail by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var linkInvalid by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.verifyPasswordResetCode(oobCode) { email, err ->
            if (email != null) {
                accountEmail = email
            } else {
                linkInvalid = true
                errorMsg = err ?: context.getString(R.string.auth_reset_invalid_link)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (LocalIsDarkMode.current) OmbreGradientBrushDark else OmbreGradientBrushLight)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PixelCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            emblemType = "paw"
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.auth_reset_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepBurgundy,
                        fontFamily = FrauncesFontFamily
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_reset_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Ink.copy(alpha = 0.7f),
                        fontFamily = QuicksandFontFamily
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (linkInvalid) {
                    errorMsg?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackToLogin,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_back_to_login),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Cream,
                                fontFamily = QuicksandFontFamily
                            )
                        )
                    }
                } else if (accountEmail != null) {
                    Text(
                        text = stringResource(R.string.auth_reset_email_hint, accountEmail!!),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Mauve,
                            fontFamily = QuicksandFontFamily
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (successMsg != null) {
                        Text(
                            text = successMsg!!,
                            color = DeepBurgundy,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDone,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy)
                        ) {
                            Text(
                                text = stringResource(R.string.auth_back_to_login),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; errorMsg = null },
                            label = { Text(stringResource(R.string.auth_new_password_label), fontFamily = QuicksandFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Mauve) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.auth_toggle_password),
                                        tint = Mauve
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMsg = null },
                            label = { Text(stringResource(R.string.auth_confirm_new_password_label), fontFamily = QuicksandFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Mauve) },
                            trailingIcon = {
                                IconButton(onClick = { showConfirm = !showConfirm }) {
                                    Icon(
                                        if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.auth_toggle_confirm_password),
                                        tint = Mauve
                                    )
                                }
                            },
                            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                            )
                        )

                        errorMsg?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (newPassword.length < 6) {
                                    errorMsg = context.getString(R.string.auth_err_weak_password)
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    errorMsg = context.getString(R.string.auth_err_passwords_dont_match)
                                    return@Button
                                }
                                isLoading = true
                                viewModel.confirmPasswordReset(oobCode, newPassword) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        errorMsg = null
                                        successMsg = msg ?: context.getString(R.string.auth_reset_success_msg)
                                    } else {
                                        errorMsg = msg ?: context.getString(R.string.auth_err_default)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Cream)
                            } else {
                                Text(
                                    text = stringResource(R.string.auth_reset_submit_btn),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Cream,
                                        fontFamily = QuicksandFontFamily
                                    )
                                )
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), color = DeepBurgundy)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.auth_reset_verifying),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Ink.copy(alpha = 0.7f),
                            fontFamily = QuicksandFontFamily
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
