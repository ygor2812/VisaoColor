package com.visaocolor.controllers
import com.visaocolor.models.ColorRecord
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.services.ColorAnalyzer

class ColorIdentificationController(
    private val analisador: ColorAnalyzer,
    private val historico: SessionColorRepository
) {

    // identifica a cor de um pixel e salva no historico
    fun identificar(r: Int, g: Int, b: Int): ColorRecord {
        val nome = analisador.identificarCor(r, g, b)
        historico.adicionarCor(nome, r, g, b)
        return ColorRecord(nome, r, g, b)
    }

    fun obterHistorico(): List<ColorRecord> = historico.obterTodas()

    fun limparHistorico() = historico.limpar()
}
