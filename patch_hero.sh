sed -i '/fun HeroHeader(/a\
    streakCount: Int,' app/src/main/java/com/example/MainActivity.kt

sed -i 's/HeroHeader(userName = userName, onLogout = onLogout, onMenuClick = onMenuClick)/HeroHeader(streakCount = streakCount, userName = userName, onLogout = onLogout, onMenuClick = onMenuClick)/' app/src/main/java/com/example/MainActivity.kt
