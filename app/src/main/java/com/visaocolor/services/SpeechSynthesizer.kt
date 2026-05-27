package com.visaocolor.services
import android.content.Context
import android.speech.tts.TextToSpeech
import com.visaocolor.models.VoiceSettings
import java.util.Locale
//Ira usar o TTS nativo do android para falar os nomes das cores e objetos
class SpeechSynthesizer(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var settings = VoiceSettings()
    private var ready = false

    fun start(onReady: (Boolean) -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.language = Locale("pt", "BR")
                tts?.setSpeechRate(settings.speed)
            }
            onReady(ready)
        }
    }

    fun speak(text: String) {
        if (!ready || !settings.active) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "visaocolor")
    }

    fun stop() {
        tts?.stop()
    }

    fun setActive(value: Boolean) {
        settings = settings.copy(active = value)
        if (!value) stop()
    }

    fun setSpeed(speed: Float) {
        settings = settings.copy(speed = speed)
        tts?.setSpeechRate(speed)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
