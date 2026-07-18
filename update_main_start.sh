sed -i '/viewModel.setLanguage(savedLang)/a\
\
        val today = System.currentTimeMillis() / 86400000L\
        val lastOpened = sharedPrefs.getLong("last_opened_date", 0L)\
        var streak = sharedPrefs.getInt("streak_count", 0)\
\
        if (lastOpened != today) {\
            if (lastOpened == today - 1L) {\
                streak += 1\
            } else if (lastOpened < today - 1L) {\
                streak = 1\
            }\
            sharedPrefs.edit()\
                .putLong("last_opened_date", today)\
                .putInt("streak_count", streak)\
                .apply()\
        } else if (streak == 0) {\
            streak = 1\
            sharedPrefs.edit()\
                .putLong("last_opened_date", today)\
                .putInt("streak_count", streak)\
                .apply()\
        }\
        viewModel.setStreak(streak)\
\
        val savedDarkMode = sharedPrefs.getBoolean("user_dark_mode", false)\
        viewModel.setDarkMode(savedDarkMode)' app/src/main/java/com/example/MainActivity.kt
