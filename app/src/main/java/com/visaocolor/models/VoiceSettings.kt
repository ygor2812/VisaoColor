package com.visaocolor.models

// configuracoes do leitor de voz (TTS)
data class VoiceSettings(
    val velocidade: Float = 1.0f,
    val volume: Float = 1.0f,
    val ativo: Boolean = false
)
