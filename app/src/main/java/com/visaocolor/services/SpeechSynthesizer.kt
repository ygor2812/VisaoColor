package com.visaocolor.services

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

// Le em voz alta os nomes dos objetos e cores, usando o TTS nativo do Android.
class SpeechSynthesizer(private val contexto: Context) {

    private var tts: TextToSpeech? = null
    private var pronto = false
    private var ativo = false

    // inicia o mecanismo de voz em portugues do Brasil
    fun iniciar() {
        tts = TextToSpeech(contexto) { status ->
            pronto = status == TextToSpeech.SUCCESS
            if (pronto) {
                tts?.language = Locale("pt", "BR")
            }
        }
    }

    // fala um texto (so se estiver pronto e ativado)
    fun falar(texto: String) {
        if (!pronto || !ativo) return
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "visaocolor")
    }

    fun definirAtivo(valor: Boolean) {
        ativo = valor
        if (!valor) tts?.stop()
    }

    // libera o recurso quando o app fecha
    fun encerrar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        pronto = false
    }
}