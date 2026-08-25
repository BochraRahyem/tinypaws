package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    userName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayDate = remember {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
    }
    
    val fallbackName = stringResource(R.string.cert_fallback_name)
    val displayName = remember(userName) {
        if (userName.isNotBlank()) userName.trim() else fallbackName
    }

    var isSaving by remember { mutableStateOf(false) }

    fun downloadCertificate() {
        isSaving = true
        try {
            val bitmap = renderCertificateBitmap(context, displayName, todayDate)
            val success = saveBitmapToGallery(context, bitmap, "TinyPaws_Certificate_${System.currentTimeMillis()}")
            if (success) {
                Toast.makeText(context, context.getString(R.string.cert_saved_success), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context.getString(R.string.cert_save_failed), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificateScreen", "Error rendering certificate", e)
            Toast.makeText(context, context.getString(R.string.cert_gen_error), Toast.LENGTH_SHORT).show()
        } finally {
            isSaving = false
        }
    }

    fun shareCertificate() {
        try {
            val bitmap = renderCertificateBitmap(context, displayName, todayDate)
            val uri = saveBitmapToCacheAndGetUri(context, bitmap)
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.cert_share_text))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.cert_share_chooser)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.cert_share_error), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cert_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = rememberHapticOnClick { onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.main_prev))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cert_congrats),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy
                ),
                textAlign = TextAlign.Center
            )

            // Certificate Display Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD4AF37))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Background Image
                    Image(
                        painter = painterResource(id = R.drawable.certificate_bg),
                        contentDescription = stringResource(R.string.cert_bg_alt),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Title
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.cert_academy_label),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepBurgundy,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.cert_mastery_label),
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Wine,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.cert_presented_to),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        // User Name (Centered)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = displayName.uppercase(),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = DeepBurgundy,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFFD4AF37), Color.Transparent)
                                            )
                                        )
                                )
                            }
                        }

                        // Citation text
                        Text(
                            text = stringResource(R.string.cert_citation),
                            fontFamily = QuicksandFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Bottom Footer: Single Date Line on Left, Gold Seal on Right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.cert_date_label),
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = todayDate,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DeepBurgundy
                                )
                            }

                            // Gold Seal Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.cert_verified_label),
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = DeepBurgundy
                                    )
                                    Text(
                                        text = stringResource(R.string.cert_seal_label),
                                        fontFamily = QuicksandFontFamily,
                                        fontSize = 8.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = rememberHapticOnClick { downloadCertificate() },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBurgundy,
                    contentColor = Cream
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.cert_generating), fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.cert_download), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            OutlinedButton(
                onClick = rememberHapticOnClick { shareCertificate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepBurgundy),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.cert_share), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Bitmap Renderer for High-Quality Certificate Image
private fun renderCertificateBitmap(context: Context, name: String, dateStr: String): Bitmap {
    val width = 1200
    val height = 900
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Try loading background drawable or fill cream background
    val bgDrawable = try {
        BitmapFactory.decodeResource(context.resources, R.drawable.certificate_bg)
    } catch (e: Exception) {
        null
    }

    if (bgDrawable != null) {
        val srcRect = Rect(0, 0, bgDrawable.width, bgDrawable.height)
        val dstRect = Rect(0, 0, width, height)
        canvas.drawBitmap(bgDrawable, srcRect, dstRect, null)
    } else {
        canvas.drawColor(android.graphics.Color.parseColor("#FFFDF5"))
    }

    // Outer double border
    val goldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        style = Paint.Style.STROKE
        strokeWidth = 14f
        isAntiAlias = true
    }
    canvas.drawRect(30f, 30f, width - 30f, height - 30f, goldPaint)

    val innerGoldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawRect(45f, 45f, width - 45f, height - 45f, innerGoldPaint)

    // Paints for text
    val academyPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#4A1525")
        textSize = 34f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#7B1113")
        textSize = 54f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#666666")
        textSize = 28f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    val namePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#4A1525")
        textSize = 64f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val bodyPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#333333")
        textSize = 24f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    val dateLabelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#666666")
        textSize = 22f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    val dateValPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#4A1525")
        textSize = 26f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    val centerX = width / 2f

    // Drawing text lines
    canvas.drawText(context.getString(R.string.cert_academy), centerX, 160f, academyPaint)
    canvas.drawText(context.getString(R.string.cert_mastery), centerX, 235f, titlePaint)
    canvas.drawText(context.getString(R.string.cert_presented), centerX, 310f, subtitlePaint)

    // User Name
    canvas.drawText(name.uppercase(), centerX, 410f, namePaint)
    
    // Line under name
    val linePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        strokeWidth = 4f
    }
    canvas.drawLine(centerX - 250f, 435f, centerX + 250f, 435f, linePaint)

    // Citation
    canvas.drawText(context.getString(R.string.cert_dedication), centerX, 510f, bodyPaint)
    canvas.drawText(context.getString(R.string.cert_winter), centerX, 550f, bodyPaint)

    // Date
    canvas.drawText(context.getString(R.string.cert_date), 100f, 760f, dateLabelPaint)
    canvas.drawText(dateStr, 100f, 795f, dateValPaint)

    // Gold Seal Circle on bottom right
    val sealPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#D4AF37")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(width - 160f, 770f, 55f, sealPaint)

    val sealTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 22f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(context.getString(R.string.cert_seal), width - 160f, 765f, sealTextPaint)
    canvas.drawText(context.getString(R.string.cert_honor), width - 160f, 792f, sealTextPaint)

    return bitmap
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Boolean {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TinyPaws")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    return if (uri != null) {
        try {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    } else {
        false
    }
}

private fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val cachePath = java.io.File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = java.io.FileOutputStream("$cachePath/certificate_share.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val imageFile = java.io.File(cachePath, "certificate_share.png")
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        null
    }
}

