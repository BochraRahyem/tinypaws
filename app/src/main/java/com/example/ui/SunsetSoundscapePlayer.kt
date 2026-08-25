package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlin.concurrent.thread
import kotlin.math.sin

object SunsetSoundscapePlayer {
    private const val SAMPLE_RATE = 22050
    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isPlaying = false
    private var focusRequest: Any? = null
    private var playerThread: Thread? = null
    private val lock = Any()

    private fun requestFocus(context: android.content.Context): Boolean {
        return try {
            val targetCtx = context
            val audioManager = targetCtx.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setOnAudioFocusChangeListener(
                        { focusChange ->
                            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                stop()
                            }
                        },
                        android.os.Handler(android.os.Looper.getMainLooper())
                    )
                    .build()
                focusRequest = request
                audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            stop()
                        }
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Throwable) {
            Log.e("SunsetSoundscape", "AudioFocus request warning: ${e.message}")
            true
        }
    }

    private fun abandonFocus(context: android.content.Context) {
        try {
            val targetCtx = context
            val audioManager = targetCtx.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (focusRequest as? android.media.AudioFocusRequest)?.let {
                    audioManager.abandonAudioFocusRequest(it)
                    focusRequest = null
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus { }
            }
        } catch (e: Throwable) {
            Log.e("SunsetSoundscape", "AudioFocus abandon warning: ${e.message}")
        }
    }

    fun start(context: android.content.Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        if (!soundEnabled) {
            stop()
            return
        }

        synchronized(lock) {
            if (isPlaying && playerThread?.isAlive == true) return

            // Stop any existing thread gracefully
            isPlaying = false
            playerThread?.let {
                try {
                    it.interrupt()
                    it.join(300)
                } catch (e: Exception) {
                    // ignore
                }
            }

            isPlaying = true
            
            playerThread = thread(start = true, name = "SunsetSoundscapeThread") {
                var localTrack: AudioTrack? = null
                try {
                    requestFocus(appContext)

                    val minBufferSize = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    
                    if (!isPlaying) return@thread

                    val createdTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val builder = AudioTrack.Builder()
                        
                        builder.setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build()
                            )
                            .setBufferSizeInBytes(minBufferSize * 2)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        AudioTrack.Builder()
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build()
                            )
                            .setBufferSizeInBytes(minBufferSize * 2)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize * 2,
                            AudioTrack.MODE_STREAM
                        )
                    }

                    localTrack = createdTrack
                    synchronized(lock) {
                        if (!isPlaying) {
                            try {
                                localTrack.release()
                            } catch (e: Exception) {
                                // ignore
                            }
                            return@thread
                        }
                        audioTrack = localTrack
                    }

                    localTrack.play()

                    // Dreamy Sunset chord progression (Cmaj9 -> Am9 -> Fmaj7 -> G6/9)
                    val chords = listOf(
                        listOf(130.81f, 261.63f, 329.63f, 392.00f, 493.88f), // Cmaj9 (C3 bass, C4, E4, G4, B4)
                        listOf(110.00f, 220.00f, 261.63f, 329.63f, 392.00f), // Am9 (A2 bass, A3, C4, E4, G4)
                        listOf(87.31f, 174.61f, 220.00f, 261.63f, 349.23f),  // Fmaj7 (F2 bass, F3, A3, C4, F4)
                        listOf(98.00f, 196.00f, 246.94f, 293.66f, 392.00f)   // G6/9 (G2 bass, G3, B3, D4, G4)
                    )

                    var chordIndex = 0
                    val secondsPerChord = 6.0
                    val totalSamples = (SAMPLE_RATE * secondsPerChord).toInt()
                    val bufferSize = 2048
                    val buffer = ShortArray(bufferSize)

                    while (isPlaying && !Thread.currentThread().isInterrupted) {
                        val frequencies = chords[chordIndex]
                        var sampleIdx = 0
                        
                        while (sampleIdx < totalSamples && isPlaying && !Thread.currentThread().isInterrupted) {
                            val numToWrite = minOf(bufferSize, totalSamples - sampleIdx)
                            
                            for (i in 0 until numToWrite) {
                                val t = (sampleIdx + i).toDouble() / SAMPLE_RATE
                                
                                // Generate combined sine waves for chord notes
                                var value = 0.0
                                for (f in frequencies) {
                                    value += sin(2.0 * Math.PI * f * t)
                                }
                                value /= frequencies.size
                                
                                // Very low volume scale to make it a subtle background soundscape
                                val baseVolume = 0.08
                                
                                // Gentle linear fade-in and fade-out envelope per chord for smooth transitions
                                val progress = (sampleIdx + i).toDouble() / totalSamples
                                val envelope = when {
                                    progress < 0.20 -> progress / 0.20 // Fade in over 20% of time
                                    progress > 0.80 -> (1.0 - progress) / 0.20 // Fade out over final 20%
                                    else -> 1.0
                                }
                                
                                val sample = (value * baseVolume * envelope * 32767.0).toInt().coerceIn(-32768, 32767)
                                buffer[i] = sample.toShort()
                            }
                            
                            if (isPlaying && localTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                                localTrack.write(buffer, 0, numToWrite)
                            }
                            sampleIdx += numToWrite
                        }
                        chordIndex = (chordIndex + 1) % chords.size
                    }
                } catch (e: Exception) {
                    Log.e("SunsetSoundscape", "Error in synthesizer thread: ${e.message}")
                } finally {
                    synchronized(lock) {
                        if (audioTrack === localTrack) {
                            audioTrack = null
                        }
                    }
                    try {
                        localTrack?.stop()
                        localTrack?.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    abandonFocus(appContext)
                }
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            isPlaying = false
            val trackToStop = audioTrack
            audioTrack = null
            try {
                trackToStop?.pause()
                trackToStop?.flush()
                trackToStop?.stop()
                trackToStop?.release()
            } catch (e: Exception) {
                // ignore
            }
            try {
                playerThread?.interrupt()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
