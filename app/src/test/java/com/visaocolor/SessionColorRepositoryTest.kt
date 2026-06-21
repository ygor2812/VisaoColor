package com.visaocolor

import com.visaocolor.repositories.SessionColorRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionColorRepositoryTest {

    private lateinit var repo: SessionColorRepository

    @Before
    fun configurar() {
        repo = SessionColorRepository()
    }

    @Test
    fun deveComecarVazio() {
        assertTrue(repo.obterTodas().isEmpty())
    }

    @Test
    fun deveAdicionarCorAoHistorico() {
        repo.adicionarCor("Vermelho", 255, 0, 0)
        assertEquals(1, repo.obterTodas().size)
        assertEquals("Vermelho", repo.obterTodas()[0].nome)
    }

    @Test
    fun limparDeveEsvaziarOHistorico() {
        repo.adicionarCor("Verde", 0, 255, 0)
        repo.adicionarCor("Azul", 0, 0, 255)
        repo.limpar()
        assertTrue(repo.obterTodas().isEmpty())
    }
}
