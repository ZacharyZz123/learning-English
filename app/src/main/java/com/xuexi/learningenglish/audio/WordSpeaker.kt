package com.xuexi.learningenglish.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class WordSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private companion object {
        const val EMPTY_AUDIO_URL = "__EMPTY_AUDIO_URL__"
    }

    private val appContext = context.applicationContext
    private val textToSpeech = TextToSpeech(appContext, this)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val audioUrlCache = ConcurrentHashMap<String, String>()

    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            isTtsReady = false
            return
        }
        isTtsReady = tryEnableEnglish()
        if (isTtsReady) {
            textToSpeech.setPitch(1.0f)
            textToSpeech.setSpeechRate(1.0f)
        }
    }

    fun speak(word: String, speechRate: Float, onUnavailable: () -> Unit) {
        val normalizedWord = word.trim()
        if (normalizedWord.isEmpty()) {
            onUnavailable()
            return
        }
        scope.launch {
            val audioUrl = withContext(Dispatchers.IO) {
                fetchPronunciationAudioUrl(normalizedWord)
            }
            if (!audioUrl.isNullOrBlank()) {
                val started = playOnlineAudio(
                    audioUrl = audioUrl,
                    speechRate = speechRate,
                    onFallback = {
                        if (!speakWithTts(normalizedWord, speechRate)) {
                            onUnavailable()
                        }
                    }
                )
                if (started) {
                    return@launch
                }
            }
            if (!speakWithTts(normalizedWord, speechRate)) {
                onUnavailable()
            }
        }
    }

    fun shutdown() {
        mediaPlayer?.release()
        mediaPlayer = null
        textToSpeech.stop()
        textToSpeech.shutdown()
        scope.cancel()
    }

    private fun tryEnableEnglish(): Boolean {
        val locales = listOf(Locale.US, Locale.UK, Locale.ENGLISH)
        for (locale in locales) {
            val result = textToSpeech.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                return true
            }
        }
        return false
    }

    private fun speakWithTts(word: String, speechRate: Float): Boolean {
        if (!isTtsReady) {
            return false
        }
        textToSpeech.stop()
        textToSpeech.setSpeechRate(speechRate)
        val result = textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, word)
        return result != TextToSpeech.ERROR
    }

    private fun playOnlineAudio(audioUrl: String, speechRate: Float, onFallback: () -> Unit): Boolean {
        return try {
            releaseMediaPlayer()
            textToSpeech.stop()
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(audioUrl)
            player.setOnPreparedListener {
                try {
                    it.playbackParams = PlaybackParams().setSpeed(speechRate)
                } catch (_: Exception) {
                }
                it.start()
            }
            player.setOnCompletionListener {
                releaseMediaPlayer()
            }
            player.setOnErrorListener { _, _, _ ->
                releaseMediaPlayer()
                onFallback()
                true
            }
            mediaPlayer = player
            player.prepareAsync()
            true
        } catch (_: Exception) {
            releaseMediaPlayer()
            false
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.run {
            try {
                stop()
            } catch (_: Exception) {
            }
            release()
        }
        mediaPlayer = null
    }

    private fun fetchPronunciationAudioUrl(word: String): String? {
        val cacheKey = word.lowercase(Locale.US)
        val cached = audioUrlCache[cacheKey]
        if (cached != null) {
            return cached.takeUnless { it == EMPTY_AUDIO_URL }
        }
        val encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8.toString())
        val requestUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/$encodedWord"
        val response = httpGet(requestUrl) ?: return cacheAndReturn(cacheKey, null)
        val audioUrl = extractAudioUrl(response)
        return cacheAndReturn(cacheKey, audioUrl)
    }

    private fun cacheAndReturn(cacheKey: String, audioUrl: String?): String? {
        audioUrlCache[cacheKey] = audioUrl ?: EMPTY_AUDIO_URL
        return audioUrl
    }

    private fun extractAudioUrl(payload: String): String? {
        return try {
            val entries = JSONArray(payload)
            val candidates = mutableListOf<String>()
            for (entryIndex in 0 until entries.length()) {
                val phonetics = entries.optJSONObject(entryIndex)?.optJSONArray("phonetics") ?: continue
                collectAudioCandidates(phonetics, candidates)
            }
            val preferred = candidates.firstOrNull { it.contains("-us.", ignoreCase = true) }
                ?: candidates.firstOrNull { it.contains("-uk.", ignoreCase = true) }
                ?: candidates.firstOrNull()
            preferred?.let(::normalizeAudioUrl)
        } catch (_: Exception) {
            null
        }
    }

    private fun collectAudioCandidates(phonetics: JSONArray, target: MutableList<String>) {
        for (index in 0 until phonetics.length()) {
            val audio = phonetics.optJSONObject(index)?.optString("audio").orEmpty()
            if (audio.isNotBlank()) {
                target += audio
            }
        }
    }

    private fun normalizeAudioUrl(audioUrl: String): String {
        return if (audioUrl.startsWith("//")) {
            "https:$audioUrl"
        } else {
            audioUrl
        }
    }

    private fun httpGet(requestUrl: String): String? {
        val connection = (URL(requestUrl).openConnection() as? HttpURLConnection) ?: return null
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/json")
            if (connection.responseCode !in 200..299) {
                return null
            }
            BufferedReader(connection.inputStream.reader()).use { reader ->
                reader.readText()
            }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
