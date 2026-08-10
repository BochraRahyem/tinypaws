package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.DiaryEntry
import com.example.data.CatProfile
import com.example.data.CatWeightLog
import com.example.data.DailyCareLog
import com.example.data.CatHistoryEntry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtil {

    fun generateCsvContent(
        profile: CatProfile?,
        careLogs: List<DailyCareLog>,
        weightLogs: List<CatWeightLog>,
        diaryLogs: List<DiaryEntry>,
        historyEntries: List<CatHistoryEntry>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        sb.append("=== TINYPAWS CAT VETERINARY & CARE REPORT ===\n")
        sb.append("Generated Date: ").append(dateFormat.format(Date())).append("\n\n")

        // 1. Cat Profile
        sb.append("--- CAT PROFILE ---\n")
        sb.append("Name,Age (Years),Age (Months),Coat Color,Photo Path\n")
        if (profile != null) {
            sb.append("\"${escapeCsv(profile.name)}\",${profile.ageYears},${profile.ageMonths},\"${escapeCsv(profile.coatColor)}\",\"${escapeCsv(profile.photoUrl ?: "")}\"\n")
        } else {
            sb.append("No Profile Configured,,,,\n")
        }
        sb.append("\n")

        // 2. Weight Logs
        sb.append("--- WEIGHT TRACKING HISTORY ---\n")
        sb.append("Date,Weight (kg)\n")
        if (weightLogs.isEmpty()) {
            sb.append("No weight records found,\n")
        } else {
            weightLogs.sortedBy { it.date }.forEach { log ->
                val dateStr = dateFormat.format(Date(log.date))
                sb.append("\"$dateStr\",${log.weight}\n")
            }
        }
        sb.append("\n")

        // 3. Daily Care Checklist Logs
        sb.append("--- DAILY CARE CHECKLIST LOGS ---\n")
        sb.append("Date,Fed,Fresh Water,Played,Litter Cleaned,Groomed,Medication\n")
        if (careLogs.isEmpty()) {
            sb.append("No care checklist entries found,,,,,,\n")
        } else {
            careLogs.sortedByDescending { it.dateString }.forEach { log ->
                sb.append("${log.dateString},")
                sb.append(if (log.fed) "Yes" else "No").append(",")
                sb.append(if (log.watered) "Yes" else "No").append(",")
                sb.append(if (log.played) "Yes" else "No").append(",")
                sb.append(if (log.litterCleaned) "Yes" else "No").append(",")
                sb.append(if (log.groomed) "Yes" else "No").append(",")
                sb.append(if (log.medicationGiven) "Yes" else "No").append("\n")
            }
        }
        sb.append("\n")

        // 4. Diary & Vet Check-In Logs
        sb.append("--- DIARY & VET CHECK-IN LOGS ---\n")
        sb.append("Date,Category,Mood,Health & Vet Notes\n")
        if (diaryLogs.isEmpty()) {
            sb.append("No diary or vet entries found,,,\n")
        } else {
            diaryLogs.sortedByDescending { it.date }.forEach { log ->
                val dateStr = dateFormat.format(Date(log.date))
                sb.append("\"$dateStr\",\"${escapeCsv(log.diaryEntryType)}\",\"${escapeCsv(log.mood)}\",\"${escapeCsv(log.notes)}\"\n")
            }
        }
        sb.append("\n")

        // 5. Cat History Logs
        sb.append("--- CAT HISTORY LOGS ---\n")
        sb.append("Date,Category,Title,Notes\n")
        if (historyEntries.isEmpty()) {
            sb.append("No history entries found,,,\n")
        } else {
            historyEntries.sortedByDescending { it.date }.forEach { log ->
                val dateStr = dateFormat.format(Date(log.date))
                sb.append("\"$dateStr\",\"${escapeCsv(log.category)}\",\"${escapeCsv(log.title)}\",\"${escapeCsv(log.notes)}\"\n")
            }
        }

        return sb.toString()
    }

    private fun escapeCsv(str: String): String {
        return str.replace("\"", "\"\"")
    }

    fun exportAndShareCsv(
        context: Context,
        profile: CatProfile?,
        careLogs: List<DailyCareLog>,
        weightLogs: List<CatWeightLog>,
        diaryLogs: List<DiaryEntry>,
        historyEntries: List<CatHistoryEntry>
    ): File? {
        return try {
            val content = generateCsvContent(profile, careLogs, weightLogs, diaryLogs, historyEntries)
            val fileName = "TinyPaws_Care_Report_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(content)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "TinyPaws Care & Health Report for ${profile?.name ?: "Cat"}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the care, weight tracking, history, and health logs report generated from TinyPaws.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Cat Care Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            file
        } catch (e: Exception) {
            android.util.Log.e("CsvExportUtil", "Failed to export CSV", e)
            null
        }
    }
}
