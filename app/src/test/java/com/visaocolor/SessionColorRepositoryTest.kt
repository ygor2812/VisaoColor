package com.visaocolor
import com.visaocolor.repositories.SessionColorRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionColorRepositoryTest {

    private lateinit var repo: SessionColorRepository

    @Before
    fun setup() {
        repo = SessionColorRepository()
    }

    @Test
    fun deveComecarVazio() {
        assertTrue(repo.getAll().isEmpty())
    }

    @Test
    fun deveAdicionarCorAoHistorico() {
        repo.addColor("Vermelho", 255, 0, 0)
        assertEquals(1, repo.getAll().size)
        assertEquals("Vermelho", repo.getAll()[0].name)
    }

    @Test
    fun clearDeveEsvaziarOHistorico() {
        repo.addColor("Verde", 0, 255, 0)
        repo.addColor("Azul", 0, 0, 255)
        repo.clear()
        assertTrue(repo.getAll().isEmpty())
    }
}
