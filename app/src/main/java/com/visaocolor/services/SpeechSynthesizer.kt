package com.visaocolor.services

import android.content.Context
import android.speech.tts.TextToSpeech
import com.visaocolor.models.VoiceSettings
import java.util.Locale

// usa o TTS nativo do Android pra falar os nomes das cores e objetos
class SpeechSynthesizer(private val contexto: Context) {

    private var tts: TextToSpeech? = null
    private var configuracoes = VoiceSettings()
    private var pronto = false

    fun iniciar(aoFicarPronto: (Boolean) -> Unit = {}) {
        tts = TextToSpeech(contexto) { status ->
            pronto = status == TextToSpeech.SUCCESS
            if (pronto) {
                tts?.language = Locale("pt", "BR")
                tts?.setSpeechRate(configuracoes.velocidade)
            }
            aoFicarPronto(pronto)
        }
    }

    fun falar(texto: String) {
        if (!pronto || !configuracoes.ativo) return
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "visaocolor")
    }

    fun parar() {
        tts?.stop()
    }

    fun definirAtivo(valor: Boolean) {
        configuracoes = configuracoes.copy(ativo = valor)
        if (!valor) parar()
    }

    fun definirVelocidade(velocidade: Float) {
        configuracoes = configuracoes.copy(velocidade = velocidade)
        tts?.setSpeechRate(velocidade)
    }

    fun encerrar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        pronto = false
    }
}
