package com.visaocolor.repositories

import com.visaocolor.models.ColorRecord

// guarda o historico de cores so durante a sessao atual
// (quando o app fecha, esse historico some)
class SessionColorRepository {

    private val cores = mutableListOf<ColorRecord>()
    private val tamanhoMaximo = 100

    fun adicionarCor(nome: String, r: Int, g: Int, b: Int) {
        cores.add(ColorRecord(nome, r, g, b))

        // se passar do limite, tira os mais antigos
        if (cores.size > tamanhoMaximo) {
            val quantidadeRemover = cores.size - tamanhoMaximo
            repeat(quantidadeRemover) { cores.removeAt(0) }
        }
    }

    fun obterTodas(): List<ColorRecord> = cores.toList()

    fun limpar() {
        cores.clear()
    }
}
