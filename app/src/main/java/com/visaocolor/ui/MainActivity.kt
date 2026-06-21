package com.visaocolor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.visaocolor.controllers.StartupController
import com.visaocolor.databinding.ActivityMainBinding
import com.visaocolor.repositories.LocalStorageRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var inicializacao: StartupController

    // pede permissao de camera
    private val permissaoCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            iniciarCamera()
        } else {
            Toast.makeText(this, "Sem permissao de camera o app nao funciona", Toast.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val armazenamento = LocalStorageRepository(applicationContext)
        inicializacao = StartupController(armazenamento)

        verificarInicializacao()
        solicitarPermissaoCamera()
    }

    private fun verificarInicializacao() = lifecycleScope.launch {
        val primeiroAcesso = inicializacao.ehPrimeiroAcesso()
        val termosOk = inicializacao.aceitouTermos()

        binding.statusText.text = when {
            primeiroAcesso && !termosOk -> "Primeiro acesso - mostrar termos e tutorial"
            !termosOk -> "Falta aceitar os termos"
            else -> "VisaoColor pronto"
        }
    }
    private fun solicitarPermissaoCamera() {
        val verificacao = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (verificacao == PackageManager.PERMISSION_GRANTED) {
            iniciarCamera()
        } else {
            permissaoCamera.launch(Manifest.permission.CAMERA)
        }
    }
    private fun iniciarCamera() {
        // A fazer ainda: integrar CameraX nessa parte
        binding.cameraStatus.text = "Camera autorizada"
    }
}
