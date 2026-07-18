sed -i 's/import android.media.AudioManager//g' app/src/main/java/com/example/ui/GameModule.kt
sed -i 's/import android.media.ToneGenerator//g' app/src/main/java/com/example/ui/GameModule.kt
sed -i '/val toneGenerator = remember {/,/null/d' app/src/main/java/com/example/ui/GameModule.kt
sed -i '/} catch (e: Exception) {/,/null/d' app/src/main/java/com/example/ui/GameModule.kt
sed -i 's/toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)//g' app/src/main/java/com/example/ui/GameModule.kt
sed -i 's/toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 400)//g' app/src/main/java/com/example/ui/GameModule.kt
