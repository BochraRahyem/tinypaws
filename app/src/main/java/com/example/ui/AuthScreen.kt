package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
fun AuthScreen(
    viewModel: TinyPawsViewModel,
    onAuthSuccess: () -> Unit,
    onGuestEntry: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isQuickGuestMode by remember { mutableStateOf(false) }
    var isForgotPassword by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }

    var fullName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var guestName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
                val titleText = when {
                    isQuickGuestMode -> stringResource(R.string.auth_welcome_guest_title)
                    isForgotPassword -> stringResource(R.string.auth_forgot_title)
                    isLogin -> stringResource(R.string.auth_login_title)
                    else -> stringResource(R.string.auth_signup_title)
                }

                val subtitleText = when {
                    isQuickGuestMode -> stringResource(R.string.auth_welcome_guest_desc)
                    isForgotPassword -> stringResource(R.string.auth_forgot_desc)
                    isLogin -> stringResource(R.string.auth_login_desc)
                    else -> stringResource(R.string.auth_signup_desc)
                }

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepBurgundy,
                        fontFamily = FrauncesFontFamily
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Ink.copy(alpha = 0.7f),
                        fontFamily = QuicksandFontFamily
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isQuickGuestMode) {
                    // Quick Entry without account
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it; errorMsg = null },
                        label = { Text(stringResource(R.string.auth_guest_name_label), fontFamily = QuicksandFontFamily) },
                        placeholder = { Text(stringResource(R.string.auth_guest_name_hint), fontFamily = QuicksandFontFamily) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_guest_name_field"),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Mauve) },
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
                            if (guestName.isBlank()) {
                                errorMsg = context.getString(R.string.auth_err_name_required)
                                return@Button
                            }
                            isLoading = true
                            val nameToSave = guestName.trim()
                            onGuestEntry(nameToSave)
                            viewModel.signInAnonymously(nameToSave) { success, error ->
                                isLoading = false
                                if (success) {
                                    onAuthSuccess()
                                } else {
                                    errorMsg = error ?: context.getString(R.string.auth_err_default)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("auth_guest_submit_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Cream)
                        } else {
                            Text(
                                text = stringResource(R.string.auth_guest_submit_btn),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { isQuickGuestMode = false; isForgotPassword = false; isLogin = true; errorMsg = null; infoMsg = null },
                        modifier = Modifier.testTag("auth_switch_to_email_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.auth_switch_to_email_btn),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DeepBurgundy,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuicksandFontFamily
                            )
                        )
                    }

                } else if (isForgotPassword) {
                    // Forgot Password View
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null; infoMsg = null },
                        label = { Text(stringResource(R.string.auth_email_label), fontFamily = QuicksandFontFamily) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Mauve) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBurgundy,
                            unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                        )
                    )

                    infoMsg?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Mauve.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = DeepBurgundy,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

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
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                errorMsg = context.getString(R.string.auth_err_invalid_email)
                                return@Button
                            }
                            isLoading = true
                            viewModel.sendPasswordResetEmail(trimmedEmail, context) { success, msg ->
                                isLoading = false
                                if (success) {
                                    errorMsg = null
                                    infoMsg = msg
                                } else {
                                    infoMsg = null
                                    errorMsg = msg
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("auth_send_reset_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Cream)
                        } else {
                            Text(
                                text = stringResource(R.string.auth_send_reset_btn),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { isForgotPassword = false; isLogin = true; errorMsg = null; infoMsg = null },
                        modifier = Modifier.testTag("auth_back_to_login_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.auth_back_to_login),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DeepBurgundy,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuicksandFontFamily
                            )
                        )
                    }

                } else {
                    // Email & Password Auth (Login or Sign Up)

                    if (!isLogin) {
                        // Full Name for Sign Up
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; errorMsg = null },
                            label = { Text(stringResource(R.string.auth_full_name_label), fontFamily = QuicksandFontFamily) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_full_name_field"),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Mauve) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Country for Sign Up & Community Rankings
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it; errorMsg = null },
                            label = { Text(stringResource(R.string.auth_country_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.auth_country_hint), fontFamily = QuicksandFontFamily) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_country_field"),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = Mauve) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = null },
                        label = { Text(stringResource(R.string.auth_email_label), fontFamily = QuicksandFontFamily) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Mauve) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBurgundy,
                            unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = null },
                        label = { Text(stringResource(R.string.auth_password_label), fontFamily = QuicksandFontFamily) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
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

                    if (!isLogin) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password for Sign Up
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMsg = null },
                            label = { Text(stringResource(R.string.auth_confirm_password_label), fontFamily = QuicksandFontFamily) },
                            modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_field"),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Mauve) },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.auth_toggle_confirm_password),
                                        tint = Mauve
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        // Forgot Password Link for Login
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TextButton(
                                onClick = { isForgotPassword = true; errorMsg = null; infoMsg = null },
                                modifier = Modifier.testTag("auth_forgot_password_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.auth_forgot_link),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepBurgundy,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = QuicksandFontFamily
                                    )
                                )
                            }
                        }
                    }

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
                            val trimmedEmail = email.trim()
                            if (!isLogin) {
                                // Sign Up Validations
                                if (fullName.isBlank()) {
                                    errorMsg = context.getString(R.string.auth_err_name_required)
                                    return@Button
                                }
                                if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                    errorMsg = context.getString(R.string.auth_err_invalid_email)
                                    return@Button
                                }
                                if (country.isBlank()) {
                                    errorMsg = context.getString(R.string.auth_err_country_required)
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMsg = context.getString(R.string.auth_err_weak_password)
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMsg = context.getString(R.string.auth_err_passwords_dont_match)
                                    return@Button
                                }

                                isLoading = true
                                val currentAuthUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                if (currentAuthUser != null && currentAuthUser.isAnonymous) {
                                    viewModel.linkGuestAccount(fullName, trimmedEmail, country, password, context) { success, msg ->
                                        isLoading = false
                                        if (success) onAuthSuccess() else errorMsg = msg
                                    }
                                } else {
                                    viewModel.signUp(fullName, trimmedEmail, country, password, context) { success, msg ->
                                        isLoading = false
                                        if (success) onAuthSuccess() else errorMsg = msg
                                    }
                                }
                            } else {
                                // Login Validations
                                if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                    errorMsg = context.getString(R.string.auth_err_invalid_email)
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMsg = context.getString(R.string.auth_err_invalid_credentials)
                                    return@Button
                                }

                                isLoading = true
                                viewModel.signIn(trimmedEmail, password, context) { success, msg ->
                                    isLoading = false
                                    if (success) onAuthSuccess() else errorMsg = msg
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("auth_submit_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Cream)
                        } else {
                            Text(
                                text = if (isLogin) stringResource(R.string.auth_sign_in_btn) else stringResource(R.string.auth_sign_up_btn),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isLogin = !isLogin; errorMsg = null },
                            modifier = Modifier.testTag("auth_toggle_btn")
                        ) {
                            Text(
                                text = if (isLogin) stringResource(R.string.auth_signup_title) else stringResource(R.string.auth_already_have_acc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DeepBurgundy,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }

                        TextButton(
                            onClick = { isQuickGuestMode = true; errorMsg = null },
                            modifier = Modifier.testTag("auth_switch_to_guest_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.auth_guest_entry_title),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DeepBurgundy,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
