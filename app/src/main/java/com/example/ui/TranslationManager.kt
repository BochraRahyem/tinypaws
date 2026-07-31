package com.example.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

val LocalLanguage = staticCompositionLocalOf { "en" }

@Composable
fun stringResource(id: Int): String {
    val context = LocalContext.current
    val lang = LocalLanguage.current
    return TranslationManager.getString(lang, context, id)
}

@Composable
fun stringResource(id: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val lang = LocalLanguage.current
    return TranslationManager.getString(lang, context, id, *formatArgs)
}

@Composable
fun stringArrayResource(id: Int): Array<String> {
    val context = LocalContext.current
    val lang = LocalLanguage.current
    return TranslationManager.getStringArray(lang, context, id)
}

object TranslationManager {
    private var translations: Map<String, Map<String, Map<String, Any>>> = emptyMap()

    fun load(context: Context) {
        try {
            val jsonString = context.assets.open("translations.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val tempMap = mutableMapOf<String, Map<String, Map<String, Any>>>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val lang = keys.next()
                val langObj = root.getJSONObject(lang)
                val langMap = mutableMapOf<String, Map<String, Any>>()
                val screens = langObj.keys()
                while (screens.hasNext()) {
                    val screen = screens.next()
                    val screenObj = langObj.getJSONObject(screen)
                    val screenMap = mutableMapOf<String, Any>()
                    val items = screenObj.keys()
                    while (items.hasNext()) {
                        val key = items.next()
                        val valueObj = screenObj.get(key)
                        if (valueObj is org.json.JSONArray) {
                            val list = mutableListOf<String>()
                            for (i in 0 until valueObj.length()) {
                                list.add(valueObj.getString(i))
                            }
                            screenMap[key] = list
                        } else {
                            screenMap[key] = valueObj.toString()
                        }
                    }
                    langMap[screen] = screenMap
                }
                tempMap[lang] = langMap
            }
            translations = tempMap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getScreenFromKey(key: String): String {
        val lower = key.lowercase()
        return when {
            lower.startsWith("onboarding_") -> "onboarding"
            lower.startsWith("chat_") -> "chat"
            lower.startsWith("tracker_") -> "tracker"
            lower.startsWith("triage_") -> "triage"
            lower.startsWith("footer_") -> "footer"
            lower.startsWith("guide_") -> "guide"
            lower.startsWith("diy_") -> "diy"
            lower.startsWith("recipe_") || lower.startsWith("cook_") -> "cook"
            lower.startsWith("location_") || lower.startsWith("cats_near_me_") || lower.startsWith("map_") -> "location"
            lower.startsWith("quiz_") -> "quiz"
            lower.startsWith("hub_") -> "hub"
            lower.startsWith("reminder_") || lower.startsWith("reminders_") -> "reminder"
            lower.startsWith("main_") || lower.startsWith("settings_") -> "main"
            lower.startsWith("nav_") -> "nav"
            lower.startsWith("lang_") || lower == "menu_language" -> "language"
            else -> "common"
        }
    }

    fun getString(lang: String, screen: String, key: String, vararg formatArgs: Any): String {
        val rawValue = translations[lang]?.get(screen)?.get(key)
            ?: translations["en"]?.get(screen)?.get(key)
            ?: translations[lang]?.get("common")?.get(key)
            ?: translations["en"]?.get("common")?.get(key)
            ?: key
        
        val value = if (rawValue is List<*>) {
            rawValue.firstOrNull()?.toString() ?: ""
        } else {
            rawValue.toString()
        }

        return if (formatArgs.isNotEmpty()) {
            try {
                String.format(value, *formatArgs)
            } catch (e: Exception) {
                value
            }
        } else {
            value
        }
    }

    fun getString(lang: String, context: Context, resId: Int, vararg formatArgs: Any): String {
        val key = try {
            context.resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            return try {
                context.getString(resId, *formatArgs)
            } catch (e: Exception) {
                try {
                    context.getString(resId)
                } catch (ex: Exception) {
                    ""
                }
            }
        }
        val screen = getScreenFromKey(key)
        val value = getString(lang, screen, key, *formatArgs)
        if (value == key) {
            return try {
                context.getString(resId, *formatArgs)
            } catch (e: Exception) {
                try {
                    context.getString(resId)
                } catch (ex: Exception) {
                    key
                }
            }
        }
        return value
    }

    fun getStringArray(lang: String, screen: String, key: String): Array<String> {
        val rawValue = translations[lang]?.get(screen)?.get(key)
            ?: translations["en"]?.get(screen)?.get(key)
            ?: translations[lang]?.get("common")?.get(key)
            ?: translations["en"]?.get("common")?.get(key)
        
        return when (rawValue) {
            is List<*> -> rawValue.map { it?.toString() ?: "" }.toTypedArray()
            is String -> arrayOf(rawValue)
            else -> emptyArray()
        }
    }

    fun getStringArray(lang: String, context: Context, resId: Int): Array<String> {
        val key = try {
            context.resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            return try {
                context.resources.getStringArray(resId)
            } catch (ex: Exception) {
                emptyArray()
            }
        }
        val screen = getScreenFromKey(key)
        val value = getStringArray(lang, screen, key)
        if (value.isEmpty()) {
            return try {
                context.resources.getStringArray(resId)
            } catch (ex: Exception) {
                emptyArray()
            }
        }
        return value
    }
}
