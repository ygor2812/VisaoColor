package com.visaocolor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.visaocolor.R
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.services.BrettelFilterProcessor
import org.opencv.android.OpenCVLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var executorCamera: ExecutorService
    private val processadorFiltro = BrettelFilterProcessor()

    private lateinit var imagemCamera: ImageView
    private lateinit var textoStatus: TextView

    private var tipoAtual: ColorBlindnessType = ColorBlindnessType.DEUTERANOPIA
    private var filtroLigado = true

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
        setContentView(R.layout.activity_main)

        imagemCamera = findViewById(R.id.imagemCamera)
        textoStatus = findViewById(R.id.textoStatus)

        try {
            System.loadLibrary("opencv_java4")
            Log.d("VisaoColor", "OpenCV carregado com sucesso")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("VisaoColor", "Falha ao carregar OpenCV", e)
            Toast.makeText(this, "Erro ao carregar OpenCV", Toast.LENGTH_LONG).show()
        }

        executorCamera = Executors.newSingleThreadExecutor()

        configurarControles()
        solicitarPermissaoCamera()
    }

    private fun configurarControles() {
        findViewById<Button>(R.id.botaoProtanopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.PROTANOPIA
            atualizarStatus()
        }
        findViewById<Button>(R.id.botaoDeuteranopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.DEUTERANOPIA
            atualizarStatus()
        }
        findViewById<Button>(R.id.botaoTritanopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.TRITANOPIA
            atualizarStatus()
        }

        val botaoLigaDesliga = findViewById<Button>(R.id.botaoLigaDesliga)
        botaoLigaDesliga.setOnClickListener {
            filtroLigado = !filtroLigado
            botaoLigaDesliga.text = if (filtroLigado) "Filtro: LIGADO" else "Filtro: DESLIGADO"
            atualizarStatus()
        }

        atualizarStatus()
    }

    private fun atualizarStatus() {
        val nomeFiltro = if (filtroLigado) tipoAtual.nomeExibicao else "Original"
        textoStatus.text = "Perfil: $nomeFiltro"
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
        val futuroProvedor = ProcessCameraProvider.getInstance(this)

        futuroProvedor.addListener({
            val provedor = futuroProvedor.get()

            val analise = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analise.setAnalyzer(executorCamera) { imagem ->
                processarFrame(imagem)
            }

            val seletorCamera = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provedor.unbindAll()
                provedor.bindToLifecycle(this, seletorCamera, analise)
            } catch (e: Exception) {
                Log.e("VisaoColor", "Erro ao iniciar a camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processarFrame(imagem: ImageProxy) {
        val rotacao = imagem.imageInfo.rotationDegrees
        val bitmap = imagem.toBitmap()

        val bitmapRotacionado = rotacionarBitmap(bitmap, rotacao)

        val bitmapFinal = if (filtroLigado) {
            processadorFiltro.aplicar(bitmapRotacionado, tipoAtual)
        } else {
            bitmapRotacionado
        }

        runOnUiThread {
            imagemCamera.setImageBitmap(bitmapFinal)
        }

        imagem.close()
    }

    private fun rotacionarBitmap(bitmap: android.graphics.Bitmap, graus: Int): android.graphics.Bitmap {
        if (graus == 0) return bitmap
        val matriz = android.graphics.Matrix()
        matriz.postRotate(graus.toFloat())
        return android.graphics.Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        executorCamera.shutdown()
    }
}