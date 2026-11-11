package com.visaocolor.services

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale

// Converte texto em fala usando o mecanismo nativo do Android
class SpeechSynthesizer(private val contexto: Context) {

    private var motor: TextToSpeech? = null
    private var pronto = false
    private var ativo = false
    private var velocidade = 1.0f
    private var volume = 1.0f

    fun iniciar() {
        motor = TextToSpeech(contexto) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                motor?.language = Locale("pt", "BR")
                motor?.setSpeechRate(velocidade)
                pronto = true
            }
        }
    }

    fun definirAtivo(valor: Boolean) {
        ativo = valor
        if (!valor) motor?.stop()
    }

    // Velocidade entre 0.5x (lento) e 2.0x (rapido)
    fun definirVelocidade(valor: Float) {
        velocidade = valor.coerceIn(0.5f, 2.0f)
        motor?.setSpeechRate(velocidade)
    }

    // Volume entre 0.0 e 1.0
    fun definirVolume(valor: Float) {
        volume = valor.coerceIn(0f, 1f)
    }

    fun falar(texto: String) {
        if (!ativo || !pronto) return
        val parametros = Bundle()
        parametros.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
        motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, parametros, "visaocolor")
    }

    fun encerrar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
    }
}