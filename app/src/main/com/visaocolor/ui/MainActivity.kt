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
    private lateinit var startup: StartupController
//Pedir permissao pra uso da camera
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(this, "Sem permissao de camera o app nao funciona", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storage = LocalStorageRepository(applicationContext)
        startup = StartupController(storage)

        checkStartup()
        askCameraPermission()
    }

    private fun checkStartup() = lifecycleScope.launch {
        val firstRun = startup.isFirstAccess()
        val termsOk = startup.didAcceptTerms()

        binding.statusText.text = when {
            firstRun && !termsOk -> "Primeiro acesso - mostrar termos e tutorial"
            !termsOk -> "Falta aceitar os termos"
            else -> "VisaoColor pronto"
        }
    }

    private fun askCameraPermission() {
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    private fun startCamera() {
        //A fazer Ainda: Integrar CameraX nessa parte
        binding.cameraStatus.text = "Camera autorizada"
    }
}
